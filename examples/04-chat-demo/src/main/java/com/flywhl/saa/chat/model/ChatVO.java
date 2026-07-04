package com.flywhl.saa.chat.model;

/**
 * 对话响应视图：文本 + 模型标识 + Token 用量（来自 {@code ChatResponse.getMetadata()}）。
 *
 * @param content          模型输出文本
 * @param model            实际生效的模型名
 * @param promptTokens     输入 token 数
 * @param completionTokens 输出 token 数
 * @param totalTokens      总 token 数
 * @author flywhl
 */
public record ChatVO(String content, String model,
                     Integer promptTokens, Integer completionTokens, Integer totalTokens) {
}
