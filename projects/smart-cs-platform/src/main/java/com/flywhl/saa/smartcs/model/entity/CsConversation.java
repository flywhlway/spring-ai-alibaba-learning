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
 * 客服会话实体（表 cs_conversation）。{@code conversationId} 与 Agent {@code threadId} 一致，全链路 UUID。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "cs_conversation")
@Getter
@Setter
public class CsConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, unique = true, length = 64)
    private String conversationId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(nullable = false, length = 32)
    private String channel = "WEB";

    @Column(length = 256)
    private String title;

    @Column(name = "message_count", nullable = false)
    private Integer messageCount = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
