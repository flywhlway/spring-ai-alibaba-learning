package com.flywhl.saa.knowledgeqa.model.vo;

import java.time.OffsetDateTime;
import java.util.List;

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
        List<CitationVO> citations,
        OffsetDateTime createdAt) {
}
