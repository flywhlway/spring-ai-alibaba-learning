package com.flywhl.saa.summarymemory;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 多轮对话入口；连续发送超过阈值条消息后，历史会被压缩为摘要。
 *
 * @author flywhl
 */
@RestController
public class MemoryController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public MemoryController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String message,
                                @RequestParam(defaultValue = "demo") String conversationId) {
        String content = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .call()
                .content();
        return Result.ok(content);
    }

    /**
     * 查看当前记忆内容，便于观察摘要是否已写入（SystemMessage 以【历史摘要】开头）。
     */
    @GetMapping("/history")
    public Result<List<Map<String, String>>> history(
            @RequestParam(defaultValue = "demo") String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        List<Map<String, String>> body = messages.stream()
                .map(m -> Map.of("role", m.getMessageType().name(), "content", m.getText()))
                .toList();
        return Result.ok(body);
    }
}
