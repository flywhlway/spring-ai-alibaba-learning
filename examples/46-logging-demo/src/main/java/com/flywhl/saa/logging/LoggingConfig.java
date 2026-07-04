package com.flywhl.saa.logging;

import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 挂载 starter 审计 Advisor。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class LoggingConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AuditLoggingAdvisor auditLoggingAdvisor) {
        return builder.defaultAdvisors(auditLoggingAdvisor).build();
    }
}
