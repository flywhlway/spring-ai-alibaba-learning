package com.flywhl.saa.smartcs.model;

/**
 * 工单状态枚举，对应 {@code cs_ticket.status}。
 *
 * <p>合法转移（由 {@code TicketService} 服务端强制校验，禁止客户端直写）：
 * <pre>
 * OPEN → AI_PROCESSING
 * AI_PROCESSING → RESOLVED | PENDING_HUMAN
 * PENDING_HUMAN → HUMAN_HANDLING | AI_PROCESSING
 * HUMAN_HANDLING → RESOLVED
 * RESOLVED → CLOSED
 * </pre>
 *
 * @author flywhl
 * @since 1.0.0
 */
public enum TicketStatus {

    OPEN,
    AI_PROCESSING,
    PENDING_HUMAN,
    HUMAN_HANDLING,
    RESOLVED,
    CLOSED
}
