package com.flywhl.saa.knowledgeqa.model.vo;

import java.time.OffsetDateTime;

/**
 * Prompt 模板视图 VO（后台列表/详情）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record PromptTemplateVO(
        Long id,
        String templateKey,
        Integer version,
        String content,
        String description,
        String status,
        OffsetDateTime publishedAt,
        Long createdBy,
        OffsetDateTime createdAt) {
}
