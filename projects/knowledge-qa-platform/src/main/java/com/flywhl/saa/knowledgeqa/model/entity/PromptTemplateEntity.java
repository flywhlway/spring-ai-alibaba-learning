package com.flywhl.saa.knowledgeqa.model.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Prompt 模板实体（表 prompt_template）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "prompt_template", uniqueConstraints = @UniqueConstraint(columnNames = {"template_key", "version"}))
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private SysUser createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
