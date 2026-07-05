package com.flywhl.saa.knowledgeqa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Milvus VectorStore 装配补充：collection 校验、检索请求默认参数（topK/相似度阈值来自 kqa.rag.*）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final KqaProperties properties;

    public VectorStoreConfig(ObjectProvider<VectorStore> vectorStoreProvider, KqaProperties properties) {
        this.vectorStoreProvider = vectorStoreProvider;
        this.properties = properties;
    }

    @PostConstruct
    void logVectorStoreStatus() {
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store != null) {
            log.info("Milvus VectorStore 已就绪，collection=kqa_knowledge，embedding-dimension=1024");
        } else {
            log.warn("Milvus VectorStore Bean 未装配，RAG 检索暂不可用");
        }
    }

    /**
     * RAG 检索默认参数，供 RagPipelineFactory 读取 kqa.rag.*。
     */
    @Bean
    RagRetrievalDefaults ragRetrievalDefaults() {
        KqaProperties.Rag rag = properties.rag();
        return new RagRetrievalDefaults(rag.topK(), rag.similarityThreshold());
    }

    /**
     * 基于 kqa.rag.* 构建的默认 {@link SearchRequest} 模板。
     */
    @Bean
    SearchRequest defaultSearchRequest(RagRetrievalDefaults defaults) {
        return SearchRequest.builder()
                .topK(defaults.topK())
                .similarityThreshold(defaults.similarityThreshold())
                .build();
    }

    public record RagRetrievalDefaults(int topK, double similarityThreshold) {
    }
}
