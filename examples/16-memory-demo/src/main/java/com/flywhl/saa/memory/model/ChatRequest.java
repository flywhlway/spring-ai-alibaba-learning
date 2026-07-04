package com.flywhl.saa.memory.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 会话请求：显式携带 {@code conversationId} 以隔离不同用户/会话。
 *
 * @param conversationId 会话标识
 * @param message        用户消息
 * @author flywhl
 */
public record ChatRequest(@NotBlank String conversationId, @NotBlank String message) {
}
