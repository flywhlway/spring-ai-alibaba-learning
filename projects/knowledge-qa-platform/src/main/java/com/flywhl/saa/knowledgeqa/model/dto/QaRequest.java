package com.flywhl.saa.knowledgeqa.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 问答请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record QaRequest(
        @NotBlank String conversationId,
        @NotBlank String question) {
}
