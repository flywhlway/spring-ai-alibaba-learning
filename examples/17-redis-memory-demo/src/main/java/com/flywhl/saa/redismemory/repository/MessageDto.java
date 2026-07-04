package com.flywhl.saa.redismemory.repository;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * {@link Message} 的 Redis 序列化载体：只保留角色与文本，足以还原本 Demo 的纯文本对话场景。
 *
 * <p>{@code TOOL} 类型消息（工具调用响应）在真实生产实现中需要保留完整的
 * {@code ToolResponseMessage} 结构才能保证对话完整性，本 Demo 聚焦纯文本记忆持久化，
 * 简化为按 {@code ASSISTANT} 还原（见 README 已知限制）。
 *
 * @param role    {@link MessageType} 名称
 * @param content 消息文本
 * @author flywhl
 */
public record MessageDto(String role, String content) {

    public static MessageDto from(Message message) {
        return new MessageDto(message.getMessageType().name(), message.getText());
    }

    public Message toMessage() {
        MessageType type = MessageType.valueOf(role);
        return switch (type) {
            case USER -> new UserMessage(content);
            case SYSTEM -> new SystemMessage(content);
            case ASSISTANT, TOOL -> new AssistantMessage(content);
        };
    }
}
