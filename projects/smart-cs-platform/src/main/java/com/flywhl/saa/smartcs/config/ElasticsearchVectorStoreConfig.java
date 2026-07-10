package com.flywhl.saa.smartcs.config;

import org.elasticsearch.client.RestClient;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FAQ 全文/混合检索通道：Elasticsearch（index {@code scs-faq}），供
 * {@code HybridSearchService} 与 Milvus 语义召回做 RRF 融合。
 *
 * <p>{@link RestClient} 复用 Spring Boot 官方 {@code ElasticsearchRestClientAutoConfiguration}
 * 依据 {@code spring.elasticsearch.uris} 自动装配的 Bean，不重复建连接。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class ElasticsearchVectorStoreConfig {

    @Bean
    ElasticsearchVectorStore elasticsearchVectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("scs-faq");
        options.setDimensions(1024);
        options.setSimilarity(SimilarityFunction.cosine);

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(true)
                .build();
    }
}
