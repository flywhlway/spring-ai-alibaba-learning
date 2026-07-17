package com.flywhl.saa.smartcs.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;

/**
 * ChatClient 统一装配入口：按 scene 经 {@link ConfigurableModelRouter} 选模型
 * （无匹配时回退 starter {@code ModelRouter} 主备降级）+ 审计 Advisor。
 *
 * <p>本 Bean 仅提供 {@link ChatClient.Builder}（未 {@code build()}）：会话记忆
 * （{@code MessageChatMemoryAdvisor}，见 {@link ChatMemoryConfig}）与 RAG
 * （{@code RetrievalAugmentationAdvisor}，Wave 2 {@code RagPipelineFactory}）
 * 由下游 Service 按场景 {@code mutate()} 追加，避免在此提前固化全部 Advisor 链。
 *
 * <p>默认 Builder 使用 FAQ scene（高频问答路径）；其它 scene 可通过
 * {@link ConfigurableModelRouter#routeForScene(String)} 自行构建。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class AiClientConfig {

    @Bean
    ChatClient.Builder chatClientBuilder(
            ConfigurableModelRouter configurableModelRouter,
            AuditLoggingAdvisor auditLoggingAdvisor) {
        ChatModel model = configurableModelRouter.routeForScene(ConfigurableModelRouter.SCENE_FAQ);
        return ChatClient.builder(model)
                .defaultAdvisors(auditLoggingAdvisor);
    }
}
