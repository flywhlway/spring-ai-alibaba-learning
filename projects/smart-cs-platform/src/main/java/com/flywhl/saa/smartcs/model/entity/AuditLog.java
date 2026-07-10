package com.flywhl.saa.smartcs.model.entity;

import java.time.OffsetDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 审计日志实体（表 audit_log）：后台操作 + AI 调用 + 工单/HITL 关键事件双轨审计。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(length = 256)
    private String target;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detail;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(nullable = false)
    private Boolean success = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
