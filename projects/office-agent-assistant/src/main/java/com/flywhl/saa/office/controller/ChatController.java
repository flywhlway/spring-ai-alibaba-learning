package com.flywhl.saa.office.controller;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.web.bind.annotation.*;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.office.model.dto.ChatRequest;
import com.flywhl.saa.office.model.vo.ChatResponseVO;
import com.flywhl.saa.office.service.ChatService;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) { this.chatService = chatService; }
    @PostMapping
    public Result<ChatResponseVO> chat(@Valid @RequestBody ChatRequest request) throws GraphRunnerException {
        return Result.ok(chatService.chat(request));
    }
}

