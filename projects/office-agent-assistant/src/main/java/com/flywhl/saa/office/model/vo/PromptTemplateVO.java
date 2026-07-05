package com.flywhl.saa.office.model.vo;
import java.time.LocalDateTime;
public record PromptTemplateVO(Long id, String templateKey, Integer version, String content,
        String description, String status, LocalDateTime publishedAt, Long createdBy, LocalDateTime createdAt) {}

