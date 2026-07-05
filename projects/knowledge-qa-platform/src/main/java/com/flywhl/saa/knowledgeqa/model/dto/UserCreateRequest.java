package com.flywhl.saa.knowledgeqa.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新建用户请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String displayName,
        @NotBlank String role,
        String department) {
}
