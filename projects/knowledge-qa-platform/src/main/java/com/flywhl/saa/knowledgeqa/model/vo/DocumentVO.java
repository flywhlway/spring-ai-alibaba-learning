package com.flywhl.saa.knowledgeqa.model.vo;

import java.time.OffsetDateTime;

/**
 * 文档视图 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record DocumentVO(
        Long id,
        String title,
        String category,
        String fileName,
        String contentType,
        Long fileSize,
        String status,
        Integer chunkCount,
        String failReason,
        Long uploadedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
