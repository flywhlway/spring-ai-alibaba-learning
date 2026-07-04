package com.flywhl.saa.pgvector;

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PGVector：文档入库、相似度检索与 Metadata Filter（FilterExpressionBuilder）。
 *
 * @author flywhl
 */
@RestController
public class PgVectorController {

    private final VectorStore vectorStore;

    public PgVectorController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/documents")
    public Result<Map<String, Object>> add(@Valid @RequestBody DocumentRequest request) {
        Map<String, Object> metadata = request.metadata() != null ? request.metadata() : Map.of();
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

    /**
     * Metadata Filter：仅通过 {@link FilterExpressionBuilder} 构造表达式，禁止拼接原生 SQL/filter 字符串。
     */
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

    private static Map<String, Object> toSearchPayload(List<Document> results, long costMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("costMs", costMs);
        payload.put("count", results.size());
        payload.put("results", results.stream().map(PgVectorController::toHit).toList());
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
