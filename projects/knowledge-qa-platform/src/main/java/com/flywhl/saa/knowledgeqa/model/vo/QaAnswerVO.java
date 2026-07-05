package com.flywhl.saa.knowledgeqa.model.vo;

import java.util.List;

/**
 * 问答结果 VO（同步接口与 SSE meta 事件共用）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record QaAnswerVO(
        String answer,
        String model,
        List<CitationVO> citations,
        TokenUsageVO usage) {

    public record TokenUsageVO(int inputTokens, int outputTokens) {
    }
}
