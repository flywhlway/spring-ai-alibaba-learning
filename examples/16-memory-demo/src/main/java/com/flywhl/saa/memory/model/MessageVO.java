package com.flywhl.saa.memory.model;

/**
 * ChatMemory 中一条历史消息的只读视图。
 *
 * @param role    消息类型（USER/ASSISTANT/SYSTEM/TOOL）
 * @param content 消息文本
 * @author flywhl
 */
public record MessageVO(String role, String content) {
}
