package com.flywhl.saa.promptbuilder.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 注册一个具名 Prompt 模板的某个版本。
 *
 * @author flywhl
 */
public record RegisterPromptRequest(@NotBlank String name, @NotBlank String version, @NotBlank String template) {
}
