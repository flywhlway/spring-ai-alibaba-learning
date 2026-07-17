package com.flywhl.saa.smartcs.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 标记需 Docker + Testcontainers 的集成测试。
 *
 * @author flywhl
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration,"
                + "org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration,"
                + "org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration"
})
@ActiveProfiles("it")
public @interface ScsIntegrationTest {
}
