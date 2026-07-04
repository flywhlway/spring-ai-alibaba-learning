package com.flywhl.saa.eshybrid;

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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch：向量入库/检索、全文检索与 RRF 混合检索。
 *
 * @author flywhl
 */
@RestController
public class EsHybridController {

    private final VectorStore vectorStore;
    private final HybridSearchService hybridSearchService;

    public EsHybridController(VectorStore vectorStore, HybridSearchService hybridSearchService) {
        this.vectorStore = vectorStore;
        this.hybridSearchService = hybridSearchService;
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
        List<Document> results = hybridSearchService.vectorSearch(q, topK);
        return Result.ok(toSearchPayload(results, System.currentTimeMillis() - start, "vector"));
    }

    @GetMapping("/search/fulltext")
    public Result<Map<String, Object>> fullText(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK) throws IOException {
        long start = System.currentTimeMillis();
        List<Document> results = hybridSearchService.fullTextSearch(q, topK);
        return Result.ok(toSearchPayload(results, System.currentTimeMillis() - start, "fulltext"));
    }

    /**
     * 混合检索：向量通道（VectorStore）+ 全文 match（ES RestClient）→ RRF 融合。
     */
    @GetMapping("/search/hybrid")
    public Result<Map<String, Object>> hybrid(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK) throws IOException {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> results = hybridSearchService.hybridSearch(q, topK);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mode", "hybrid-rrf");
        payload.put("costMs", System.currentTimeMillis() - start);
        payload.put("count", results.size());
        payload.put("results", results);
        return Result.ok(payload);
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
        Map<String, Object> payload = toSearchPayload(results, System.currentTimeMillis() - start, "vector+filter");
        payload.put("filter", Map.of("department", department));
        return Result.ok(payload);
    }

    private static Map<String, Object> toSearchPayload(List<Document> results, long costMs, String mode) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mode", mode);
        payload.put("costMs", costMs);
        payload.put("count", results.size());
        payload.put("results", results.stream().map(EsHybridController::toHit).toList());
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
