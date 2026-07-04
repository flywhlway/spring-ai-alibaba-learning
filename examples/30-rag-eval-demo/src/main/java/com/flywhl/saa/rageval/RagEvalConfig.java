package com.flywhl.saa.rageval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 评测用 RAG 链路：与 29 相同的 Modular RAG 骨架。
 *
 * @author flywhl
 */
@Configuration
public class RagEvalConfig {

    @Bean
    VectorStoreDocumentRetriever documentRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(5)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, VectorStoreDocumentRetriever documentRetriever) {
        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();

        return chatClientBuilder
                .defaultSystem("""
                        你是企业知识库助手。你只能依据检索到的知识库内容回答用户问题。
                        如果知识库中没有相关资料，请明确回复「知识库中未找到相关信息」，不要编造事实。
                        """)
                .defaultAdvisors(ragAdvisor)
                .build();
    }
}
