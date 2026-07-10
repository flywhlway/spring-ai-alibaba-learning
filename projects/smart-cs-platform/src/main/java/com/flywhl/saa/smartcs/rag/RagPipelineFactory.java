package com.flywhl.saa.smartcs.rag;

import java.util.Comparator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flywhl.saa.smartcs.config.ScsProperties;
import com.flywhl.saa.smartcs.prompt.PromptTemplateProvider;

/**
 * Modular RAG 管线工厂：组装 {@link RetrievalAugmentationAdvisor}（查询改写 + Milvus 向量检索
 * + 分数重排 + 上下文增强），供 Wave 3 Agent（{@code faq-agent} 知识库工具）直接消费。
 *
 * <p>与 {@code service.FaqAnswerService} 自建的 Milvus+ES RRF 混合检索链路相互独立：本 Bean
 * 仅对 {@code milvusVectorStore} 单库检索，服务于需要 Spring AI 标准 RAG Advisor 契约的场景。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class RagPipelineFactory {

    @Bean
    VectorStoreDocumentRetriever documentRetriever(
            @Qualifier("milvusVectorStore") VectorStore milvusVectorStore, ScsProperties properties) {
        ScsProperties.Rag rag = properties.rag();
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(milvusVectorStore)
                .similarityThreshold(rag.similarityThreshold())
                .topK(rag.topK())
                .build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            ChatClient.Builder chatClientBuilder,
            VectorStoreDocumentRetriever documentRetriever,
            ScsProperties properties,
            PromptTemplateProvider promptTemplateProvider) {

        ScsProperties.Rag rag = properties.rag();

        var rewriteTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.build().mutate())
                .promptTemplate(new PromptTemplate(promptTemplateProvider.getQueryRewriteTemplate()))
                .build();

        DocumentPostProcessor scoreReranker = (query, documents) -> documents.stream()
                .sorted(Comparator.comparing(Document::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(rag.topK())
                .toList();

        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(rewriteTransformer)
                .documentRetriever(documentRetriever)
                .documentPostProcessors(scoreReranker)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(false)
                        .build())
                .build();
    }
}
