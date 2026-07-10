package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 答案反馈请求 DTO（rating: 1 有帮助 / -1 无帮助）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record FeedbackRequest(
        @NotNull Long messageId,
        @NotNull Short rating,
        String comment) {
}
