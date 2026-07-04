package com.flywhl.saa.toolsecurity;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author flywhl
 */
@RestController
public class ToolSecurityController {

    private final ChatClient chatClient;

    public ToolSecurityController(ChatClient.Builder chatClientBuilder, KnowledgeAdminTools tools) {
        this.chatClient = chatClientBuilder.defaultTools(tools).build();
    }

    @GetMapping("/admin/ask")
    public String ask(@RequestParam String question,
                       @RequestParam(defaultValue = "USER") String role) {
        return chatClient.prompt()
                .toolContext(Map.of("role", role))
                .user(question)
                .call()
                .content();
    }
}
