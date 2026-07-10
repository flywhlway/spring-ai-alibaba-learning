package com.flywhl.saa.smartcs.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 会话记忆装配：{@link MessageWindowChatMemory} + {@link MessageChatMemoryAdvisor}
 * （仓库禁用旧版 Prompt 前缀记忆 Advisor，一律使用消息级记忆 Advisor 新写法）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class ChatMemoryConfig {

    @Bean
    ChatMemory chatMemory(RedisChatMemoryRepository repository, ScsProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(properties.memory().maxMessages())
                .build();
    }

    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
