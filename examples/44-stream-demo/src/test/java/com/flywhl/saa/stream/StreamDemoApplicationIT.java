package com.flywhl.saa.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证流式输出至少产生一个非空文本片段。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class StreamDemoApplicationIT {

    @Autowired
    private ChatClient chatClient;

    @Test
    void streamReturnsNonEmptyContent() {
        List<String> chunks = chatClient.prompt()
                .user("用一句话介绍 Spring AI")
                .stream()
                .content()
                .collectList()
                .block(Duration.ofSeconds(60));

        assertThat(chunks).isNotEmpty();
        assertThat(String.join("", chunks)).isNotBlank();
    }
}
