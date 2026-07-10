package com.flywhl.saa.smartcs.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.flywhl.saa.smartcs.config.ScsProperties;

/**
 * FAQ 语义缓存：独立 Redis Stack VectorStore（{@code redisStackVectorStore}，6380）承载问答语义
 * 缓存，命中阈值默认 0.95（宁缺毋滥，避免答非所问），metadata {@code type=semantic-cache} 隔离
 * 普通向量数据；命中条目按 {@code expiresAt}（ISO-8601）校验有效期，过期即视为未命中。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class SemanticCacheService {

    private static final String CACHE_TYPE = "semantic-cache";
    private static final int LOOKUP_TOP_K = 3;

    private final VectorStore redisStackVectorStore;
    private final ScsProperties properties;

    public SemanticCacheService(
            @Qualifier("redisStackVectorStore") VectorStore redisStackVectorStore,
            ScsProperties properties) {
        this.redisStackVectorStore = redisStackVectorStore;
        this.properties = properties;
    }

    /**
     * 相似度检索命中未过期缓存条目即返回；过期或无命中均视为未命中。
     */
    public Optional<CacheHit> lookup(String query) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Document> results = redisStackVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(LOOKUP_TOP_K)
                        .similarityThreshold(properties.cache().similarityThreshold())
                        .filterExpression(b.eq("type", CACHE_TYPE).build())
                        .build());

        Instant now = Instant.now();
        for (Document doc : results) {
            Object expiresAt = doc.getMetadata().get("expiresAt");
            if (expiresAt == null) {
                continue;
            }
            Instant expiry = Instant.parse(expiresAt.toString());
            if (now.isBefore(expiry)) {
                Object answer = doc.getMetadata().get("answer");
                return Optional.of(new CacheHit(answer == null ? "" : answer.toString(), doc.getScore()));
            }
        }
        return Optional.empty();
    }

    /**
     * 写入语义缓存：{@code query} 作为向量文本，{@code answer}/{@code expiresAt} 写入 metadata。
     */
    public void put(String query, String answer, int ttlSeconds) {
        Instant now = Instant.now();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("type", CACHE_TYPE);
        metadata.put("answer", answer);
        metadata.put("cachedAt", now.toString());
        metadata.put("expiresAt", now.plusSeconds(ttlSeconds).toString());
        redisStackVectorStore.add(List.of(new Document(query, metadata)));
    }

    /**
     * 语义缓存命中结果：答案文本与相似度分值。
     */
    public record CacheHit(String answer, Double score) {
    }
}
