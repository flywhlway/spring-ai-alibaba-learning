package com.flywhl.saa.eshybrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量检索（VectorStore）+ ES 全文 match（RestClient）+ RRF 融合。
 *
 * <p>Spring AI 1.1.2 的 ElasticsearchVectorStore 仅提供向量 similaritySearch；
 * 混合检索在应用层用原生 match 查询补齐全文通道，再做 Reciprocal Rank Fusion。
 *
 * @author flywhl
 */
@Service
public class HybridSearchService {

    private static final int RRF_K = 60;

    private final VectorStore vectorStore;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String indexName;

    public HybridSearchService(
            VectorStore vectorStore,
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${spring.ai.vectorstore.elasticsearch.index-name:saa-hybrid}") String indexName) {
        this.vectorStore = vectorStore;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.indexName = indexName;
    }

    public List<Document> vectorSearch(String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build());
    }

    public List<Document> fullTextSearch(String query, int topK) throws IOException {
        Map<String, Object> body = Map.of(
                "size", topK,
                "query", Map.of("match", Map.of("content", query)));
        Request request = new Request("POST", "/" + indexName + "/_search");
        request.setJsonEntity(objectMapper.writeValueAsString(body));
        Response response = restClient.performRequest(request);
        try (InputStream in = response.getEntity().getContent()) {
            JsonNode root = objectMapper.readTree(in);
            List<Document> docs = new ArrayList<>();
            for (JsonNode hit : root.path("hits").path("hits")) {
                JsonNode source = hit.path("_source");
                String id = source.path("id").asText(hit.path("_id").asText());
                String content = source.path("content").asText("");
                Map<String, Object> metadata = new LinkedHashMap<>();
                JsonNode metaNode = source.path("metadata");
                if (metaNode.isObject()) {
                    metaNode.fields().forEachRemaining(e -> metadata.put(e.getKey(), toJava(e.getValue())));
                }
                Double score = hit.path("_score").isMissingNode() || hit.path("_score").isNull()
                        ? null
                        : hit.path("_score").asDouble();
                Document.Builder builder = Document.builder()
                        .id(id)
                        .text(content)
                        .metadata(metadata);
                if (score != null) {
                    builder.score(score);
                }
                docs.add(builder.build());
            }
            return docs;
        }
    }

    /**
     * Reciprocal Rank Fusion：score(d) = Σ 1 / (k + rank_i(d))
     */
    public List<Map<String, Object>> hybridSearch(String query, int topK) throws IOException {
        int candidateK = Math.max(topK * 2, topK);
        List<Document> vectorHits = vectorSearch(query, candidateK);
        List<Document> textHits = fullTextSearch(query, candidateK);

        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Document> docsById = new HashMap<>();
        accumulateRrf(vectorHits, rrfScores, docsById);
        accumulateRrf(textHits, rrfScores, docsById);

        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(topK)
                .map(e -> {
                    Document doc = docsById.get(e.getKey());
                    Map<String, Object> hit = new LinkedHashMap<>();
                    hit.put("id", doc.getId());
                    hit.put("content", doc.getText());
                    hit.put("metadata", doc.getMetadata());
                    hit.put("rrfScore", e.getValue());
                    if (doc.getScore() != null) {
                        hit.put("sourceScore", doc.getScore());
                    }
                    return hit;
                })
                .toList();
    }

    private static void accumulateRrf(
            List<Document> hits,
            Map<String, Double> rrfScores,
            Map<String, Document> docsById) {
        for (int i = 0; i < hits.size(); i++) {
            Document doc = hits.get(i);
            String id = doc.getId();
            docsById.putIfAbsent(id, doc);
            double contrib = 1.0 / (RRF_K + i + 1);
            rrfScores.merge(id, contrib, Double::sum);
        }
    }

    private static Object toJava(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        return node.toString();
    }
}
