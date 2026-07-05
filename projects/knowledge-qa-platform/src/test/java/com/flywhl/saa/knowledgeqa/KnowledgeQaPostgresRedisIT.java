package com.flywhl.saa.knowledgeqa;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flywhl.saa.knowledgeqa.config.DemoKnowledgeSeeder;
import com.flywhl.saa.knowledgeqa.repository.SysUserRepository;
import com.flywhl.saa.knowledgeqa.support.KqaIntegrationTest;
import com.flywhl.saa.knowledgeqa.support.KqaPostgresRedisITBase;

import io.minio.MinioClient;

/**
 * PostgreSQL + Redis Testcontainers 冒烟：Repository 可访问。
 *
 * @author flywhl
 */
@KqaIntegrationTest
@DisplayName("PostgreSQL/Redis Testcontainers")
class KnowledgeQaPostgresRedisIT extends KqaPostgresRedisITBase {

    @MockBean
    private VectorStore vectorStore;

    @MockBean
    private MinioClient minioClient;

    @MockBean
    private DemoKnowledgeSeeder demoKnowledgeSeeder;

    @Autowired
    private SysUserRepository userRepository;

    @Test
    @DisplayName("演示账号可从 PG 加载")
    void loadsSeedUsers() {
        assertThat(userRepository.findByUsername("admin")).isPresent();
        assertThat(userRepository.findByUsername("zhangsan")).isPresent();
    }
}
