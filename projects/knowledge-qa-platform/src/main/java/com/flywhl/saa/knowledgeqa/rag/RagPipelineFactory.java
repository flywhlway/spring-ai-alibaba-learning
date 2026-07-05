package com.flywhl.saa.knowledgeqa.rag;

import java.util.Comparator;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flywhl.saa.knowledgeqa.config.KqaProperties;
import com.flywhl.saa.knowledgeqa.prompt.PromptTemplateProvider;

/**
 * Modular RAG 管线工厂：组装 {@link RetrievalAugmentationAdvisor}（查询改写 + 向量检索 + 分数重排）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class RagPipelineFactory {

    @Bean
    VectorStoreDocumentRetriever documentRetriever(VectorStore vectorStore, KqaProperties properties) {
        KqaProperties.Rag rag = properties.rag();
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(rag.similarityThreshold())
                .topK(rag.topK())
                .build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            ChatClient.Builder chatClientBuilder,
            VectorStoreDocumentRetriever documentRetriever,
            KqaProperties properties,
            PromptTemplateProvider promptTemplateProvider) {

        KqaProperties.Rag rag = properties.rag();

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
