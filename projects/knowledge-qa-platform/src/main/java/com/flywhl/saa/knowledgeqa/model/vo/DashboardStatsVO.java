package com.flywhl.saa.knowledgeqa.model.vo;

/**
 * 看板统计 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record DashboardStatsVO(
        long totalQuestions,
        double totalCost,
        double satisfactionRate,
        long documentCount,
        long chunkCount) {
}
