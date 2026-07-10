package com.flywhl.saa.smartcs.model.vo;

/**
 * HITL 会话响应：start 返回 {@code PENDING_HUMAN}，approve 后返回 {@code HUMAN_HANDLING}/{@code RESOLVED}。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record HitlSessionResponse(
        String threadId,
        String status,
        String ticketNo,
        String message) {
}
