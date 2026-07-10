package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 人工接管审批恢复请求 DTO（HITL resume，threadId 与 conversationId 一致）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record HandoffApproveRequest(
        @NotBlank String threadId) {
}
