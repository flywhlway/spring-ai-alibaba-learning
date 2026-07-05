package com.flywhl.saa.office.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis 短期会话记忆（前缀 {@code office:chat-memory:}）。
 *
 * @author flywhl
 */
@Repository
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    static final String KEY_PREFIX = "office:chat-memory:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisChatMemoryRepository(StringRedisTemplate redis, ObjectMapper objectMapper, OfficeProperties properties) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = properties.memory().shortTermTtl();
    }

    @Override
    public List<String> findConversationIds() {
        Set<String> keys = redis.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return keys.stream().map(k -> k.substring(KEY_PREFIX.length())).sorted().toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<String> jsonList = redis.opsForList().range(key(conversationId), 0, -1);
        if (jsonList == null || jsonList.isEmpty()) {
            return List.of();
        }
        List<Message> messages = new ArrayList<>(jsonList.size());
        for (String json : jsonList) {
            messages.add(read(json).toMessage());
        }
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String redisKey = key(conversationId);
        redis.delete(redisKey);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<String> jsonList = messages.stream().map(message -> write(MessageDto.from(message))).toList();
        redis.opsForList().rightPushAll(redisKey, jsonList);
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            redis.expire(redisKey, ttl);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        redis.delete(key(conversationId));
    }

    private String key(String conversationId) {
        return KEY_PREFIX + conversationId;
    }

    private String write(MessageDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化 MessageDto 失败", e);
        }
    }

    private MessageDto read(String json) {
        try {
            return objectMapper.readValue(json, MessageDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("反序列化 MessageDto 失败", e);
        }
    }
}
