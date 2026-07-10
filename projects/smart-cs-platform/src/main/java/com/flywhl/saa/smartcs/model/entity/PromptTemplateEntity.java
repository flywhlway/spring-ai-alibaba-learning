package com.flywhl.saa.smartcs.model.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Prompt 模板版本化实体（表 prompt_template）。发布后经 Nacos 推送热更新。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "prompt_template")
@Getter
@Setter
public class PromptTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_key", nullable = false, length = 128)
    private String templateKey;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 256)
    private String description;

    @Column(nullable = false, length = 32)
    private String status = "DRAFT";

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
