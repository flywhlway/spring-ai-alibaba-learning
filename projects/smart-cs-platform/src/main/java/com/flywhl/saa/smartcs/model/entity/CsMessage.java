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
 * 客服消息归档实体（表 cs_message）：含路由 Agent、语义缓存命中、token 用量。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "cs_message")
@Getter
@Setter
public class CsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 64)
    private String conversationId;

    @Column(nullable = false, length = 16)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "route_agent", length = 64)
    private String routeAgent;

    @Column(name = "cache_hit", nullable = false)
    private Boolean cacheHit = false;

    @Column(name = "input_tokens", nullable = false)
    private Integer inputTokens = 0;

    @Column(name = "output_tokens", nullable = false)
    private Integer outputTokens = 0;

    @Column(name = "latency_ms", nullable = false)
    private Long latencyMs = 0L;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
