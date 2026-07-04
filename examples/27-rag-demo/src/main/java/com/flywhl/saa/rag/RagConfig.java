package com.flywhl.saa.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Naive RAG：{@link QuestionAnswerAdvisor} 自动检索并拼入 Prompt。
 *
 * <p>T-27-01：system 提示仅依据知识库；无相关资料时明确说明，禁止编造。
 *
 * @author flywhl
 */
@Configuration
public class RagConfig {

    static final String SYSTEM_PROMPT = """
            你是企业知识库助手。你只能依据检索到的知识库内容回答用户问题。
            如果知识库中没有相关资料，请明确回复「知识库中未找到相关信息」，不要编造事实。
            """;

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }
}
