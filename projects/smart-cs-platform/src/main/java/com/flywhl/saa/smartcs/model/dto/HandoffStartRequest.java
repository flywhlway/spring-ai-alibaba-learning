package com.flywhl.saa.smartcs.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 人工接管启动请求：触发 human-escalation-agent，遇中断进入 PENDING_HUMAN。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record HandoffStartRequest(
        String conversationId,
        @NotBlank @Size(max = 2000) String query) {
}
