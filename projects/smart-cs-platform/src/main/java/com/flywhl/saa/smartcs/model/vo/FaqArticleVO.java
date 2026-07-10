package com.flywhl.saa.smartcs.model.vo;

import java.time.OffsetDateTime;

/**
 * FAQ 文档 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record FaqArticleVO(
        Long id,
        String title,
        String category,
        String question,
        String answer,
        String status,
        int chunkCount,
        OffsetDateTime createdAt) {
}
