package com.flywhl.saa.retry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证上下文与自定义 RetryTemplate 装配。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class RetryDemoApplicationIT {

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private RetryAttemptCounter attemptCounter;

    @Test
    void contextLoadsWithCustomRetryTemplate() {
        assertThat(retryTemplate).isNotNull();
        assertThat(attemptCounter).isNotNull();
    }
}
