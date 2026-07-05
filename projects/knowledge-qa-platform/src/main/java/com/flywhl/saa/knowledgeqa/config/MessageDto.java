package com.flywhl.saa.knowledgeqa.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * {@link Message} 的 Redis 序列化载体。
 *
 * @param role    {@link MessageType} 名称
 * @param content 消息文本
 * @author flywhl
 */
record MessageDto(String role, String content) {

    static MessageDto from(Message message) {
        return new MessageDto(message.getMessageType().name(), message.getText());
    }

    Message toMessage() {
        MessageType type = MessageType.valueOf(role);
        return switch (type) {
            case USER -> new UserMessage(content);
            case SYSTEM -> new SystemMessage(content);
            case ASSISTANT, TOOL -> new AssistantMessage(content);
        };
    }
}
