package com.flywhl.saa.knowledgeqa.model.vo;

import java.time.OffsetDateTime;

/**
 * 会话视图 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ConversationVO(
        String conversationId,
        String title,
        Integer messageCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
