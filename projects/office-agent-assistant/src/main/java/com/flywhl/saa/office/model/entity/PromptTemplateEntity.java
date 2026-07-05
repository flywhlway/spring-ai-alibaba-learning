package com.flywhl.saa.office.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prompt_template", uniqueConstraints = @UniqueConstraint(columnNames = {"template_key", "version"}))
@Getter @Setter
public class PromptTemplateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private LocalDateTime publishedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private SysUser createdBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

