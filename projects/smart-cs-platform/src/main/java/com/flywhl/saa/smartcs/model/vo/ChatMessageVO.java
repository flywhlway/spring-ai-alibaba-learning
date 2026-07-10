package com.flywhl.saa.smartcs.model.vo;

import java.time.OffsetDateTime;

/**
 * 会话历史消息 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ChatMessageVO(
        Long id,
        String role,
        String content,
        String routeAgent,
        boolean cacheHit,
        OffsetDateTime createdAt) {
}
