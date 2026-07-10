package com.flywhl.saa.smartcs.model.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.flywhl.saa.smartcs.model.TicketStatus;

/**
 * 工单实体（表 cs_ticket）。状态机由 {@code TicketService} 服务端强制校验，禁止客户端直写。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "cs_ticket")
@Getter
@Setter
public class CsTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", nullable = false, unique = true, length = 32)
    private String ticketNo;

    @Column(name = "conversation_id", nullable = false, length = 64)
    private String conversationId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(nullable = false, length = 16)
    private String priority = "NORMAL";

    @Column(nullable = false, length = 512)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
