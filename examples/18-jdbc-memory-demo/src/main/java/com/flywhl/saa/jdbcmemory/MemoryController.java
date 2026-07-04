package com.flywhl.saa.jdbcmemory;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话隔离验证入口。
 *
 * @author flywhl
 */
@RestController
public class MemoryController {

    private final ChatClient chatClient;

    public MemoryController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String message, @RequestParam String userId) {
        String content = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .user(message)
                .call()
                .content();
        return Result.ok(content);
    }
}
