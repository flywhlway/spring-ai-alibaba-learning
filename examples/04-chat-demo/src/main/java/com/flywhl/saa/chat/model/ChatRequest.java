package com.flywhl.saa.chat.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 对话请求体。
 *
 * @param system      系统提示（可选，覆盖 defaultSystem）
 * @param message     用户输入（必填）
 * @param temperature 调用级温度覆盖（可选，null 时沿用 YAML 默认）
 * @author flywhl
 */
public record ChatRequest(String system, @NotBlank(message = "message 不能为空") String message, Double temperature) {
}
