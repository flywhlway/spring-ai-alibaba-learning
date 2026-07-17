package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotNull;

import com.flywhl.saa.smartcs.model.TicketStatus;

/**
 * 工单状态转移请求（仅允许通过服务端状态机，禁止直写 status）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record TicketTransitionRequest(
        @NotNull TicketStatus to,
        String reason) {
}
