package com.flywhl.saa.prompt.model;

import jakarta.validation.constraints.NotBlank;

/**
 * PromptTemplate 渲染请求：领域 + 日志正文。
 *
 * @author flywhl
 */
public record TemplateRequest(@NotBlank String domain, @NotBlank String log) {
}
