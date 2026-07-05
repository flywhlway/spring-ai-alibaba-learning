package com.flywhl.saa.knowledgeqa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.flywhl.saa.knowledgeqa.config.DemoKnowledgeSeeder;

import io.minio.MinioClient;

/**
 * 应用上下文冒烟测试：无 Docker 时通过 @MockBean 替代外部中间件，仅验证 Spring 容器启动。
 *
 * @author flywhl
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration"
})
@ActiveProfiles("test")
@DisplayName("应用上下文加载")
class KnowledgeQaApplicationTests {

    @MockBean
    private VectorStore vectorStore;

    @MockBean
    private MinioClient minioClient;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private DemoKnowledgeSeeder demoKnowledgeSeeder;

    @Test
    @DisplayName("Spring 上下文可正常启动")
    void contextLoads() {
    }
}
