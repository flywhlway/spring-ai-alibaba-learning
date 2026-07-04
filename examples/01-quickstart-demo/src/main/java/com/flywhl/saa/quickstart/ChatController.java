package com.flywhl.saa.quickstart;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 最小问答接口：通过 ChatClient 调用 DashScope，验证端到端链路。
 *
 * @author flywhl
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        // ChatClient.Builder 由 spring-ai-alibaba-starter-dashscope 自动装配注入，
        // 这正是第 03 章 AutoConfiguration 要深入剖析的机制
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "用一句话介绍你自己") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
