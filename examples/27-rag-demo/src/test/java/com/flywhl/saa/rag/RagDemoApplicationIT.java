package com.flywhl.saa.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 且 Milvus 可用时验证上下文装配。
 * 无 Key 时自动 Disabled；无 Milvus 时启动失败，需先 {@code bash scripts/infra.sh up vector}。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class RagDemoApplicationIT {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    @Test
    void contextLoadsWithQuestionAnswerAdvisor() {
        assertThat(chatClient).isNotNull();
        assertThat(vectorStore).isNotNull();
    }
}
