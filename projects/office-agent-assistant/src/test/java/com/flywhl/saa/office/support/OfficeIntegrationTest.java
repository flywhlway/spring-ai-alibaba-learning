package com.flywhl.saa.office.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testcontainers MySQL 集成测试标记。
 *
 * @author flywhl
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerAvailableCondition.class)
@SpringBootTest(properties = {
        "spring.ai.mcp.client.enabled=false",
        "spring.ai.mcp.server.enabled=false"
})
@ActiveProfiles("it")
public @interface OfficeIntegrationTest {
}
