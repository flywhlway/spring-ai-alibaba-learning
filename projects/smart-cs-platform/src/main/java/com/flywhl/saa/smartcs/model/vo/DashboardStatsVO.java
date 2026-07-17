package com.flywhl.saa.smartcs.model.vo;

import java.util.Map;

/**
 * 运营看板统计 VO：会话量 / 消息量 / 工单分布 / 缓存命中率 / route_agent 分布 / Token 成本。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record DashboardStatsVO(
        long totalConversations,
        long totalMessages,
        Map<String, Long> ticketsByStatus,
        double cacheHitRate,
        Map<String, Long> routeAgentDistribution,
        double totalCost,
        double satisfactionRate) {
}
