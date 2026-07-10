package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 会话问答请求 DTO（同步 {@code /api/chat/ask} 与流式 {@code /api/chat/stream} 共用）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ChatRequest(
        @NotBlank String conversationId,
        @NotBlank String question) {
}
