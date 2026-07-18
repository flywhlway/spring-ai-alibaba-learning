package com.flywhl.saa.smartcs.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.model.dto.ChatRequest;
import com.flywhl.saa.smartcs.model.vo.ChatAnswerVO;
import com.flywhl.saa.smartcs.service.ChatService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

/**
 * 客服会话网关：{@code POST /api/chat/ask} 同步问答，{@code POST /api/chat/stream} SSE 流式
 * （event: message / meta / interrupt / done / error）。流式接口使用 POST body，避免问题原文进入
 * query string / 访问日志（PII）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/chat")
@Validated
@PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public Result<ChatAnswerVO> ask(@Valid @RequestBody ChatRequest request) {
        return Result.ok(chatService.ask(request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@Valid @RequestBody ChatRequest request) {
        return chatService.stream(request.conversationId(), request.question());
    }
}
