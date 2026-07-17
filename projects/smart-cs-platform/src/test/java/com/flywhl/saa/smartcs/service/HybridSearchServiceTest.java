package com.flywhl.saa.smartcs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.smartcs.rag.HybridSearchService;

/**
 * {@link HybridSearchService} 单测：mock Milvus VectorStore + ES RestClient，验证 RRF 排序。
 *
 * @author flywhl
 */
@DisplayName("混合检索 RRF")
class HybridSearchServiceTest {

    private final VectorStore milvusVectorStore = mock(VectorStore.class);
    private final RestClient restClient = mock(RestClient.class);
    private HybridSearchService service;

    @BeforeEach
    void setUp() {
        service = new HybridSearchService(milvusVectorStore, restClient, new ObjectMapper(), "scs-faq");
    }

    @Test
    @DisplayName("双通道共现文档 RRF 分最高")
    void hybridSearch_ranksOverlappingDocHighest() throws Exception {
        Document docA = Document.builder().id("doc-a").text("仅向量命中").build();
        Document docB = Document.builder().id("doc-b").text("双通道共现").build();
        when(milvusVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(docA, docB));

        String esJson = """
                {
                  "hits": {
                    "hits": [
                      {
                        "_id": "doc-b",
                        "_score": 2.0,
                        "_source": { "id": "doc-b", "content": "双通道共现", "metadata": {} }
                      },
                      {
                        "_id": "doc-c",
                        "_score": 1.0,
                        "_source": { "id": "doc-c", "content": "仅全文命中", "metadata": {} }
                      }
                    ]
                  }
                }
                """;
        Response response = mock(Response.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent())
                .thenReturn(new ByteArrayInputStream(esJson.getBytes(StandardCharsets.UTF_8)));
        when(restClient.performRequest(any())).thenReturn(response);

        List<Map<String, Object>> hits = service.hybridSearch("退货政策", 3);

        assertThat(hits).isNotEmpty();
        assertThat(hits.get(0).get("id")).isEqualTo("doc-b");
        assertThat((Double) hits.get(0).get("rrfScore")).isGreaterThan((Double) hits.get(1).get("rrfScore"));
    }
}
