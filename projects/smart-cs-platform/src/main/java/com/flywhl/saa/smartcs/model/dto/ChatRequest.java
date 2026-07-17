package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 会话问答请求 DTO（同步 {@code /api/chat/ask} 与流式 {@code /api/chat/stream} 共用）。
 * {@code conversationId} 可空，服务端首次生成 UUID 并与 threadId 绑定。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ChatRequest(
        String conversationId,
        @NotBlank @Size(max = 2000) String question) {
}
