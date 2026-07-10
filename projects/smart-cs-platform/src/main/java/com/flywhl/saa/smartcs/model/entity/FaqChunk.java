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
 * FAQ Chunk 溯源实体（表 faq_chunk）：与 Milvus/ES 中的向量一一对应。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "faq_chunk")
@Getter
@Setter
public class FaqChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "milvus_pk", nullable = false, unique = true, length = 64)
    private String milvusPk;

    @Column(name = "es_doc_id", length = 64)
    private String esDocId;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @Column(name = "text_preview", nullable = false, length = 512)
    private String textPreview;

    @Column(name = "token_count", nullable = false)
    private Integer tokenCount = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
