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
 * 满意度反馈实体（表 cs_feedback）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "cs_feedback")
@Getter
@Setter
public class CsFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Short rating;

    @Column(length = 512)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
