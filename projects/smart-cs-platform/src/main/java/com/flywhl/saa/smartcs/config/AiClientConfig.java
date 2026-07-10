package com.flywhl.saa.smartcs.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import com.flywhl.saa.starter.routing.ModelRouter;

/**
 * ChatClient 统一装配入口：多模型路由（DashScope 主 / DeepSeek 备，经 starter
 * {@link ModelRouter} 降级）+ 审计 Advisor。
 *
 * <p>本 Bean 仅提供 {@link ChatClient.Builder}（未 {@code build()}）：会话记忆
 * （{@code MessageChatMemoryAdvisor}，见 {@link ChatMemoryConfig}）与 RAG
 * （{@code RetrievalAugmentationAdvisor}，Wave 2 {@code RagPipelineFactory}）
 * 由下游 Service 按场景 {@code mutate()} 追加，避免在此提前固化全部 Advisor 链。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class AiClientConfig {

    @Bean
    ChatClient.Builder chatClientBuilder(ModelRouter fallbackModelRouter, AuditLoggingAdvisor auditLoggingAdvisor) {
        ChatModel model = fallbackModelRouter.route();
        return ChatClient.builder(model)
                .defaultAdvisors(auditLoggingAdvisor);
    }
}
