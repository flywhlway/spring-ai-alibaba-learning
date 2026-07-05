package com.flywhl.saa.knowledgeqa.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Prompt 模板保存请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record PromptSaveRequest(
        @NotBlank String templateKey,
        @NotBlank String content,
        String description) {
}
