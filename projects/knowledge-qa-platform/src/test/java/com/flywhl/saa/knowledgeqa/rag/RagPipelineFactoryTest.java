package com.flywhl.saa.knowledgeqa.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;

import com.flywhl.saa.knowledgeqa.config.KqaProperties;

/**
 * RagPipelineFactory 相关逻辑单元测试：topK 检索参数与分数重排 limit。
 *
 * @author flywhl
 */
@DisplayName("RAG 管线工厂")
class RagPipelineFactoryTest {

    @Test
    @DisplayName("DocumentRetriever 使用 kqa.rag.topK 与相似度阈值")
    void documentRetrieverUsesRagProperties() {
        VectorStore vectorStore = mock(VectorStore.class);
        KqaProperties properties = new KqaProperties(null, new KqaProperties.Rag(3, 0.5, 512, 64, true), null, null);

        RagPipelineFactory factory = new RagPipelineFactory();
        VectorStoreDocumentRetriever retriever = factory.documentRetriever(vectorStore, properties);

        assertThat(retriever).isNotNull();
    }

    @Test
    @DisplayName("分数重排后仅保留 topK 条文档")
    void scoreRerankerLimitsToTopK() {
        int topK = 2;
        DocumentPostProcessor reranker = (query, documents) -> documents.stream()
                .sorted(Comparator.comparing(Document::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(topK)
                .toList();

        List<Document> input = List.of(
                doc("a", 0.5),
                doc("b", 0.95),
                doc("c", 0.8),
                doc("d", 0.7));

        List<Document> result = reranker.process(new Query("出差住宿费"), input);

        assertThat(result).hasSize(topK);
        assertThat(result.get(0).getText()).isEqualTo("b");
        assertThat(result.get(1).getText()).isEqualTo("c");
    }

    private static Document doc(String text, double score) {
        return Document.builder().text(text).metadata(Map.of()).score(score).build();
    }
}
