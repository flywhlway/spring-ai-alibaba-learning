package com.flywhl.saa.smartcs.model.vo;

import java.time.OffsetDateTime;

/**
 * 工单 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record TicketVO(
        Long id,
        String ticketNo,
        String conversationId,
        Long customerId,
        Long assignedAgentId,
        String status,
        String priority,
        String summary,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
