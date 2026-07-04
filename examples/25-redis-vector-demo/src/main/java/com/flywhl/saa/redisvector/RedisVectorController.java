package com.flywhl.saa.redisvector;

import com.flywhl.saa.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Stack VectorStore：入库、检索与语义缓存（TTL）演示。
 *
 * @author flywhl
 */
@RestController
public class RedisVectorController {

    private static final int DEFAULT_CACHE_TTL_SECONDS = 300;

    private final VectorStore vectorStore;

    public RedisVectorController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/documents")
    public Result<Map<String, Object>> add(@Valid @RequestBody DocumentRequest request) {
        Map<String, Object> metadata = request.metadata() != null
                ? new LinkedHashMap<>(request.metadata())
                : new LinkedHashMap<>();
        metadata.putIfAbsent("type", "document");
        Document document = new Document(request.content(), metadata);
        vectorStore.add(List.of(document));
        return Result.ok(Map.of(
                "id", document.getId(),
                "message", "已入库 1 条文档"));
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK) {
        long start = System.currentTimeMillis();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(topK).build());
        return Result.ok(toSearchPayload(results, System.currentTimeMillis() - start));
    }

    @GetMapping("/search/filter")
    public Result<Map<String, Object>> searchWithFilter(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam String department) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        long start = System.currentTimeMillis();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(q)
                        .topK(topK)
                        .filterExpression(b.eq("department", department).build())
                        .build());
        Map<String, Object> payload = toSearchPayload(results, System.currentTimeMillis() - start);
        payload.put("filter", Map.of("department", department));
        return Result.ok(payload);
    }

    /**
     * 语义缓存写入：query 作为向量文本，answer / ttl 写入 metadata。
     */
    @PostMapping("/cache")
    public Result<Map<String, Object>> putCache(@Valid @RequestBody CacheRequest request) {
        int ttl = request.ttlSeconds() != null ? request.ttlSeconds() : DEFAULT_CACHE_TTL_SECONDS;
        Instant now = Instant.now();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("type", "semantic-cache");
        metadata.put("answer", request.answer());
        metadata.put("cachedAt", now.toString());
        metadata.put("ttlSeconds", ttl);
        metadata.put("expiresAt", now.plusSeconds(ttl).toString());
        Document document = new Document(request.query(), metadata);
        vectorStore.add(List.of(document));
        return Result.ok(Map.of(
                "id", document.getId(),
                "ttlSeconds", ttl,
                "expiresAt", metadata.get("expiresAt"),
                "message", "语义缓存已写入"));
    }

    /**
     * 语义缓存查找：相似度命中后校验 metadata 中的 expiresAt，过期则视为未命中。
     */
    @GetMapping("/cache/lookup")
    public Result<Map<String, Object>> lookupCache(
            @RequestParam String q,
            @RequestParam(defaultValue = "0.8") double similarityThreshold) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(q)
                        .topK(3)
                        .similarityThreshold(similarityThreshold)
                        .filterExpression(b.eq("type", "semantic-cache").build())
                        .build());

        Instant now = Instant.now();
        for (Document doc : results) {
            Object expiresAt = doc.getMetadata().get("expiresAt");
            if (expiresAt == null) {
                continue;
            }
            Instant expiry = Instant.parse(expiresAt.toString());
            if (now.isBefore(expiry)) {
                Map<String, Object> hit = new LinkedHashMap<>();
                hit.put("hit", true);
                hit.put("query", doc.getText());
                hit.put("answer", doc.getMetadata().get("answer"));
                hit.put("score", doc.getScore());
                hit.put("expiresAt", expiresAt);
                return Result.ok(hit);
            }
        }
        return Result.ok(Map.of("hit", false, "message", "未命中有效语义缓存（无相似条目或已过期）"));
    }

    private static Map<String, Object> toSearchPayload(List<Document> results, long costMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("costMs", costMs);
        payload.put("count", results.size());
        payload.put("results", results.stream().map(RedisVectorController::toHit).toList());
        return payload;
    }

    private static Map<String, Object> toHit(Document doc) {
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("id", doc.getId());
        hit.put("content", doc.getText());
        hit.put("metadata", doc.getMetadata());
        if (doc.getScore() != null) {
            hit.put("score", doc.getScore());
        }
        return hit;
    }
}
