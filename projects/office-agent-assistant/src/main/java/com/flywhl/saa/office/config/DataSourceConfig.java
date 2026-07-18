package com.flywhl.saa.office.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

/**
 * 显式声明 MySQL 主数据源为 {@link Primary}。
 *
 * <p>否则 {@link VectorStoreConfig} 中的 {@code vectorDataSource} 会抢占
 * {@code DataSourceAutoConfiguration} 的 {@code @ConditionalOnMissingBean(DataSource)}，
 * 导致 JPA / JDBC 记忆 / SQL Tool 误连 pgvector 库。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
