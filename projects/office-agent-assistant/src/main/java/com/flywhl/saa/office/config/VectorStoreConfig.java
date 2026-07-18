package com.flywhl.saa.office.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

/**
 * pgvector 独立 DataSource 与 VectorStore 装配（与 MySQL 主库隔离）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "office.vector-datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Bean
    @ConfigurationProperties("office.vector-datasource")
    DataSourceProperties officeVectorDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "vectorDataSource")
    DataSource vectorDataSource(
            @Qualifier("officeVectorDataSourceProperties") DataSourceProperties officeVectorDataSourceProperties) {
        return officeVectorDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    JdbcTemplate vectorJdbcTemplate(@Qualifier("vectorDataSource") DataSource vectorDataSource) {
        return new JdbcTemplate(vectorDataSource);
    }

    @Bean
    VectorStore vectorStore(
            EmbeddingModel embeddingModel,
            @Qualifier("vectorDataSource") DataSource vectorDataSource) {
        PgVectorStore store = PgVectorStore.builder(new JdbcTemplate(vectorDataSource), embeddingModel)
                .dimensions(1024)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .vectorTableName("office_knowledge")
                .build();
        log.info("PgVectorStore 已就绪，table=office_knowledge，dimensions=1024");
        return store;
    }
}
