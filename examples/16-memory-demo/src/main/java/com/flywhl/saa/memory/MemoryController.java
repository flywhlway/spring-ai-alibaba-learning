package com.flywhl.saa.memory;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.memory.model.ChatRequest;
import com.flywhl.saa.memory.model.MessageVO;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 演示 {@code MessageWindowChatMemory} 的整轮次滑动窗口与 {@code conversationId} 会话隔离。
 *
 * @author flywhl
 */
@RestController
@RequestMapping("/memory")
public class MemoryController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public MemoryController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 发送一条消息；Advisor 会自动读取该 conversationId 的历史并在响应后写回。
     */
    @PostMapping("/chat")
    public Result<String> chat(@Valid @RequestBody ChatRequest request) {
        String content = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .user(request.message())
                .call()
                .content();
        return Result.ok(content);
    }

    /**
     * 查看指定会话当前保留的历史消息——用于观察窗口驱逐效果。
     */
    @GetMapping("/history/{conversationId}")
    public Result<List<MessageVO>> history(@PathVariable String conversationId) {
        List<MessageVO> messages = chatMemory.get(conversationId).stream()
                .map(m -> new MessageVO(m.getMessageType().name(), m.getText()))
                .toList();
        return Result.ok(messages);
    }

    /**
     * 清空指定会话的记忆。
     */
    @DeleteMapping("/history/{conversationId}")
    public Result<Void> clear(@PathVariable String conversationId) {
        chatMemory.clear(conversationId);
        return Result.ok();
    }
}
