package com.flywhl.saa.agenthitl;

import java.util.List;

/**
 * HITL 会话响应：start 返回 PENDING_APPROVAL，approve 后返回 COMPLETED。
 *
 * @author flywhl
 */
public record HitlSessionResponse(
        String threadId,
        String status,
        List<PendingToolCall> pendingTools,
        String message) {
}
