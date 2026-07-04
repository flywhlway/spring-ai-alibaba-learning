package com.flywhl.saa.summarymemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 摘要 Advisor（先）+ MessageChatMemoryAdvisor（后）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class MemoryConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(50)
                .build();
    }

    @Bean
    public SummaryCompressingAdvisor summaryCompressingAdvisor(
            ChatMemory chatMemory,
            ChatModel chatModel,
            @Value("${saa.summary-memory.threshold:6}") int threshold,
            @Value("${saa.summary-memory.keep-recent:2}") int keepRecent) {
        return new SummaryCompressingAdvisor(chatMemory, chatModel, threshold, keepRecent);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 ChatMemory chatMemory,
                                 SummaryCompressingAdvisor summaryCompressingAdvisor) {
        return builder
                .defaultAdvisors(
                        summaryCompressingAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
