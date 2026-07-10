package com.flywhl.saa.smartcs.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FAQ 主向量库：Milvus（collection {@code scs_faq}，embedding dimensions=1024）。
 *
 * <p>手动装配（而非依赖 {@code spring-ai-autoconfigure-vector-store-milvus} 的默认
 * {@code vectorStore} Bean）：项目同时接入 Milvus/Elasticsearch/Redis Stack 三个
 * {@link org.springframework.ai.vectorstore.VectorStore}，各自 {@code @ConditionalOnMissingBean}
 * 仅按各自具体类型（MilvusVectorStore/ElasticsearchVectorStore/RedisVectorStore）生效，
 * 此处显式声明具体类型 Bean 以保证三者互不覆盖、下游可用 {@code @Qualifier} 精确区分。
 * {@link MilvusServiceClient} 仍复用 starter 自动装配的连接（host/port 见
 * {@code spring.ai.vectorstore.milvus.client.*}）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class MilvusVectorStoreConfig {

    @Bean
    MilvusVectorStore milvusVectorStore(MilvusServiceClient milvusServiceClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
                .databaseName("default")
                .collectionName("scs_faq")
                .embeddingDimension(1024)
                .indexType(IndexType.IVF_FLAT)
                .metricType(MetricType.COSINE)
                .initializeSchema(true)
                .build();
    }
}
