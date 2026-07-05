package com.flywhl.saa.office.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 基座：MySQL 8 + Redis 7。
 *
 * @author flywhl
 */
@Testcontainers
@ExtendWith(DockerAvailableCondition.class)
public abstract class OfficeMySqlITBase {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("office_agent")
            .withUsername("saa")
            .withPassword("saa123456");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379).toString());
        registry.add("spring.ai.dashscope.api-key",
                () -> System.getenv().getOrDefault("AI_DASHSCOPE_API_KEY", "test-dummy-key"));
        registry.add("office.security.jwt.secret", () -> "dev-only-office-jwt-secret-32bytes!!");
        registry.add("office.vector-datasource.enabled", () -> "false");
    }
}
