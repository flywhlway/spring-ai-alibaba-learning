package com.flywhl.saa.smartcs.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 基座：PostgreSQL 16（库名 {@code scs_platform}）+ Redis 7；
 * Milvus / ES / Redis Stack 由子类按需 Mock 或连接真机。
 *
 * @author flywhl
 */
@Testcontainers
@ExtendWith(DockerAvailableCondition.class)
public abstract class ScsPostgresRedisITBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16"))
            .withDatabaseName("scs_platform")
            .withUsername("saa")
            .withPassword("saa123456");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379).toString());
        registry.add("spring.ai.dashscope.api-key",
                () -> System.getenv().getOrDefault("AI_DASHSCOPE_API_KEY", "test-dummy-key"));
        registry.add("spring.ai.deepseek.api-key", () -> "test-dummy-key");
        registry.add("spring.ai.nacos.prompt.template.enabled", () -> "false");
        registry.add("scs.security.jwt.secret", () -> "dev-only-scs-jwt-secret-key-32bytes!!");
        // 语义缓存指向同容器 Redis，避免 IT 额外依赖 Redis Stack
        registry.add("scs.cache.redis-uri",
                () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
    }
}
