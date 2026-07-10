package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
