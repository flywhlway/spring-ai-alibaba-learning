package com.flywhl.saa.observability;

import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 挂载 starter 审计 Advisor；成本采集由 starter 自动装配的
 * {@link com.flywhl.saa.starter.metrics.CostTrackingObservationHandler} 完成。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilityConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AuditLoggingAdvisor auditLoggingAdvisor) {
        return builder.defaultAdvisors(auditLoggingAdvisor).build();
    }
}
