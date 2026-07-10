package com.flywhl.saa.smartcs.config;

import java.net.URI;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPooled;

/**
 * FAQ 语义缓存：独立 Redis Stack 连接（{@code scs.cache.redis-uri}，默认 6380），
 * 与会话记忆 Redis（{@code spring.data.redis}，6379）物理隔离（威胁登记 T-06-03）。
 *
 * <p>不复用 {@code spring-ai-autoconfigure-vector-store-redis} 的默认 {@code vectorStore}
 * Bean：该自动装配依赖容器中的 {@link org.springframework.data.redis.connection.jedis.JedisConnectionFactory}，
 * 而项目默认的 {@code spring-boot-starter-data-redis} 走 Lettuce 驱动会话记忆 6379 连接，
 * 两者不能共用同一 {@code JedisConnectionFactory}；此处显式基于 {@code scs.cache.redis-uri}
 * 构建独立 {@link JedisPooled} 指向 6380，metadata filter {@code type=semantic-cache} 可用。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class RedisStackCacheConfig {

    @Bean
    JedisPooled scsCacheJedisPooled(ScsProperties properties) {
        URI uri = URI.create(properties.cache().redisUri());
        return new JedisPooled(uri.getHost(), uri.getPort());
    }

    @Bean
    RedisVectorStore redisStackVectorStore(JedisPooled scsCacheJedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(scsCacheJedisPooled, embeddingModel)
                .indexName("scs-semantic-cache")
                .prefix("scs-cache:")
                .metadataFields(RedisVectorStore.MetadataField.tag("type"))
                .initializeSchema(true)
                .build();
    }
}
