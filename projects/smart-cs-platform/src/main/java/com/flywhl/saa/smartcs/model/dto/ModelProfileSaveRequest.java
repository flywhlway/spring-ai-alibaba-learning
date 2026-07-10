package com.flywhl.saa.smartcs.model.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 模型配置新建/更新请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ModelProfileSaveRequest(
        @NotBlank String profileKey,
        @NotBlank String provider,
        @NotBlank String modelName,
        @NotBlank String scene,
        @NotNull Integer priority,
        @NotNull Boolean enabled,
        Map<String, Object> optionsJson) {
}
