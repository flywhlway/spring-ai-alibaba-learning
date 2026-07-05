package com.flywhl.saa.knowledgeqa.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flywhl.saa.knowledgeqa.prompt.PromptTemplateProvider;
import com.flywhl.saa.starter.routing.ModelRouter;

/**
 * ChatClient 统一装配：多模型路由 + 会话记忆 + Modular RAG + 审计 Advisor 链。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class AiClientConfig {

    @Bean
    ChatClient chatClient(
            ModelRouter fallbackModelRouter,
            PromptTemplateProvider promptTemplateProvider,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor,
            RetrievalAugmentationAdvisor retrievalAugmentationAdvisor,
            DbAuditLoggingAdvisor dbAuditLoggingAdvisor) {
        ChatModel model = fallbackModelRouter.route();
        return ChatClient.builder(model)
                .defaultSystem(promptTemplateProvider.get("qa-system"))
                .defaultAdvisors(
                        messageChatMemoryAdvisor,
                        retrievalAugmentationAdvisor,
                        dbAuditLoggingAdvisor)
                .build();
    }
}
