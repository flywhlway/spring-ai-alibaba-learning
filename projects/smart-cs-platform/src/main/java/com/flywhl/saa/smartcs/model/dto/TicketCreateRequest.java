package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 工单创建请求 DTO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record TicketCreateRequest(
        @NotBlank String conversationId,
        @NotBlank String summary,
        String priority) {
}
