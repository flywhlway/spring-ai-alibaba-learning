package com.flywhl.saa.smartcs.model.vo;

/**
 * 运营看板统计 VO：会话量 / 成本 / 语义缓存命中率 / 工单分布。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record DashboardStatsVO(
        long totalConversations,
        long totalMessages,
        double totalCost,
        double cacheHitRate,
        long openTicketCount,
        long pendingHumanTicketCount,
        long resolvedTicketCount,
        double satisfactionRate) {
}
