package com.flywhl.saa.agenthitl;

/**
 * 待人工审批的工具调用摘要（同包小 DTO，不拆 model 子包）。
 *
 * @author flywhl
 */
public record PendingToolCall(String id, String name, String arguments, String description) {
}
