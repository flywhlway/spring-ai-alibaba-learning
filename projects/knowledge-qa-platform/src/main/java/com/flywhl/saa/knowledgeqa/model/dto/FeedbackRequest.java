package com.flywhl.saa.knowledgeqa.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 答案反馈请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record FeedbackRequest(
        @NotNull Long messageId,
        @NotNull @Min(-1) @Max(1) Integer rating,
        String comment) {
}
