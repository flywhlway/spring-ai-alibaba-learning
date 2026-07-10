package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Prompt 模板新建草稿版本请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record PromptSaveRequest(
        @NotBlank String templateKey,
        @NotBlank String content,
        String description) {
}
