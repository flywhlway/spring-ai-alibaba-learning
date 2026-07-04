package com.flywhl.saa.retry;

import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResourceAccessException;

/**
 * 自定义 {@link RetryTemplate}：读取 {@code spring.ai.retry.*}，并挂载 {@link RetryAttemptCounter}。
 *
 * <p>官方 {@code SpringAiRetryAutoConfiguration#retryTemplate} 带
 * {@code @ConditionalOnMissingBean}，本 Bean 优先生效（教程 §4.8）。
 *
 * @author flywhl
 */
@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate(SpringAiRetryProperties properties, RetryAttemptCounter counter) {
        SpringAiRetryProperties.Backoff backoff = properties.getBackoff();
        return RetryTemplate.builder()
                .maxAttempts(properties.getMaxAttempts())
                .retryOn(TransientAiException.class)
                .retryOn(ResourceAccessException.class)
                .exponentialBackoff(
                        backoff.getInitialInterval(),
                        backoff.getMultiplier(),
                        backoff.getMaxInterval())
                .withListener(counter)
                .build();
    }
}
