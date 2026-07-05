package com.flywhl.saa.office.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 记忆装配：Redis 短期窗口 + JDBC 长期（Spring AI JDBC 仓库）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class ChatMemoryConfig {

    @Bean
    @Primary
    ChatMemory shortTermChatMemory(RedisChatMemoryRepository repository, OfficeProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(properties.memory().shortTermMaxMessages())
                .build();
    }

    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(@Qualifier("shortTermChatMemory") ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean(name = "longTermChatMemory")
    @ConditionalOnBean(name = "jdbcChatMemoryRepository")
    ChatMemory longTermChatMemory(@Qualifier("jdbcChatMemoryRepository") ChatMemoryRepository jdbcRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcRepository)
                .maxMessages(100)
                .build();
    }
}
