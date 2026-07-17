package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * FAQ 新建请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record FaqSaveRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String question,
        @NotBlank String answer) {
}
