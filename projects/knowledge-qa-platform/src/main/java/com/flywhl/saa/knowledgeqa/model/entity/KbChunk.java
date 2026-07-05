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
import lombok.Getter;
import lombok.Setter;

/**
 * Chunk 元数据实体（表 kb_chunk）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "kb_chunk")
@Getter
@Setter
public class KbChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KbDocument document;

    @Column(name = "milvus_pk", nullable = false, unique = true, length = 64)
    private String milvusPk;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @Column(name = "text_preview", nullable = false, length = 512)
    private String textPreview;

    @Column(name = "token_count", nullable = false)
    private Integer tokenCount = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
