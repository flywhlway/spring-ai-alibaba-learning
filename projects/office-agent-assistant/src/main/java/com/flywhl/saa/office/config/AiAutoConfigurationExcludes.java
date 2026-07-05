package com.flywhl.saa.office.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 排除 pgvector 默认自动装配（使用 {@link VectorStoreConfig} 独立 DataSource）。
 *
 * @author flywhl
 */
@Configuration
@EnableAutoConfiguration(excludeName = {
        "org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration"
})
public class AiAutoConfigurationExcludes {
}
