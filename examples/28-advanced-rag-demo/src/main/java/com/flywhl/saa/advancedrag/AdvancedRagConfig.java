package com.flywhl.saa.advancedrag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

/**
 * Modular RAG：{@link RetrievalAugmentationAdvisor} 组装查询改写与重排序。
 *
 * <p>相对 Naive RAG（27）：可插拔 QueryTransformer / DocumentPostProcessor，
 * 空上下文默认拒绝作答（不编造）。
 *
 * @author flywhl
 */
@Configuration
public class AdvancedRagConfig {

    static final String SYSTEM_PROMPT = """
            你是企业知识库助手。你只能依据检索到的知识库内容回答用户问题。
            如果知识库中没有相关资料，请明确回复「知识库中未找到相关信息」，不要编造事实。
            """;

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        // mutate() 得到无 Advisor 的 Builder，避免改写链路递归挂载 RAG Advisor
        var rewriteTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.build().mutate())
                .build();

        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(20)
                .build();

        // 按相似度分数降序截断 Top-5，演示 DocumentPostProcessor 重排序扩展点
        DocumentPostProcessor scoreReranker = (query, documents) -> documents.stream()
                .sorted(Comparator.comparing(Document::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();

        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(rewriteTransformer)
                .documentRetriever(documentRetriever)
                .documentPostProcessors(scoreReranker)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(false)
                        .build())
                .build();

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(ragAdvisor)
                .build();
    }
}
