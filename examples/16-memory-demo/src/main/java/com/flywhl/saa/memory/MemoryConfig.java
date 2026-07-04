package com.flywhl.saa.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 策略（{@link MessageWindowChatMemory}）与存储（{@link InMemoryChatMemoryRepository}）分离装配。
 *
 * <p>窗口大小刻意取 6（约 3 轮对话），便于 Demo 场景在少量请求内观察到旧消息被驱逐。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class MemoryConfig {

    private static final int MAX_MESSAGES = 6;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(MAX_MESSAGES)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
