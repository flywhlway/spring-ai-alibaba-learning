package com.flywhl.saa.smartcs.model.vo;

/**
 * 会话问答结果 VO（同步接口与 SSE meta 事件共用）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ChatAnswerVO(
        String answer,
        String routeAgent,
        boolean cacheHit,
        TokenUsageVO usage) {

    public record TokenUsageVO(int inputTokens, int outputTokens) {
    }
}
