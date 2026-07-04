package com.flywhl.saa.stream;

import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 显式挂载 starter 提供的 {@link AuditLoggingAdvisor}。
 *
 * <p>Starter 不会自动把 Advisor 挂到 ChatClient，Demo 必须 {@code defaultAdvisors(audit)}。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class StreamConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AuditLoggingAdvisor auditLoggingAdvisor) {
        return builder.defaultAdvisors(auditLoggingAdvisor).build();
    }
}
