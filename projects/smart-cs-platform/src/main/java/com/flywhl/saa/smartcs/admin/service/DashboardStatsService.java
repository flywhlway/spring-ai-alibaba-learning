package com.flywhl.saa.smartcs.admin.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.flywhl.saa.smartcs.model.TicketStatus;
import com.flywhl.saa.smartcs.model.entity.CsMessage;
import com.flywhl.saa.smartcs.model.vo.DashboardStatsVO;
import com.flywhl.saa.smartcs.repository.CsConversationRepository;
import com.flywhl.saa.smartcs.repository.CsFeedbackRepository;
import com.flywhl.saa.smartcs.repository.CsMessageRepository;
import com.flywhl.saa.smartcs.repository.CsTicketRepository;
import com.flywhl.saa.starter.autoconfigure.SaaLearningProperties;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;

/**
 * 运营看板统计：会话/消息/工单/缓存命中/route_agent 分布/Token 成本。
 *
 * <p>Token 成本优先读 Micrometer {@code gen_ai.client.token.usage}，否则按消息表 token 字段与
 * {@link SaaLearningProperties.CostTracking} 单价估算。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class DashboardStatsService {

    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final short POSITIVE_RATING = 1;

    private final CsConversationRepository conversationRepository;
    private final CsMessageRepository messageRepository;
    private final CsTicketRepository ticketRepository;
    private final CsFeedbackRepository feedbackRepository;
    private final SaaLearningProperties saaLearningProperties;
    private final MeterRegistry meterRegistry;

    public DashboardStatsService(
            CsConversationRepository conversationRepository,
            CsMessageRepository messageRepository,
            CsTicketRepository ticketRepository,
            CsFeedbackRepository feedbackRepository,
            SaaLearningProperties saaLearningProperties,
            MeterRegistry meterRegistry) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.ticketRepository = ticketRepository;
        this.feedbackRepository = feedbackRepository;
        this.saaLearningProperties = saaLearningProperties;
        this.meterRegistry = meterRegistry;
    }

    public DashboardStatsVO stats(int days) {
        int windowDays = Math.max(days, 1);
        OffsetDateTime since = OffsetDateTime.now().minusDays(windowDays);

        long totalConversations = conversationRepository.countByCreatedAtAfter(since);
        long totalMessages = messageRepository.countByCreatedAtAfter(since);
        Map<String, Long> ticketsByStatus = resolveTicketsByStatus();
        double cacheHitRate = resolveCacheHitRate(since);
        Map<String, Long> routeAgentDistribution = resolveRouteAgentDistribution(since);
        double totalCost = resolveTotalCost(since);
        double satisfactionRate = resolveSatisfactionRate(since);

        return new DashboardStatsVO(
                totalConversations,
                totalMessages,
                ticketsByStatus,
                cacheHitRate,
                routeAgentDistribution,
                totalCost,
                satisfactionRate);
    }

    private Map<String, Long> resolveTicketsByStatus() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            map.put(status.name(), ticketRepository.countByStatus(status));
        }
        return map;
    }

    private double resolveCacheHitRate(OffsetDateTime since) {
        long assistantMessages = messageRepository.countByRoleAndCreatedAtAfter(ASSISTANT_ROLE, since);
        if (assistantMessages == 0) {
            return 0.0;
        }
        long hits = messageRepository.countByCacheHitTrueAndCreatedAtAfter(since);
        return (double) hits / assistantMessages;
    }

    private Map<String, Long> resolveRouteAgentDistribution(OffsetDateTime since) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : messageRepository.countRouteAgentByRoleAfter(ASSISTANT_ROLE, since)) {
            String agent = row[0] != null ? row[0].toString() : "UNKNOWN";
            long count = row[1] instanceof Number number ? number.longValue() : 0L;
            map.put(agent, count);
        }
        return map;
    }

    private double resolveTotalCost(OffsetDateTime since) {
        double fromMetrics = sumGenAiUsageCost();
        if (fromMetrics > 0) {
            return fromMetrics;
        }
        return messageRepository.findByRoleAndCreatedAtAfter(ASSISTANT_ROLE, since).stream()
                .mapToDouble(this::estimateMessageCost)
                .sum();
    }

    private double sumGenAiUsageCost() {
        return Search.in(meterRegistry)
                .name("gen_ai.client.token.usage")
                .counters()
                .stream()
                .mapToDouble(counter -> counter.count()
                        * saaLearningProperties.costTracking().pricePer1kInputTokens() / 1000.0)
                .sum();
    }

    private double estimateMessageCost(CsMessage message) {
        SaaLearningProperties.CostTracking pricing = saaLearningProperties.costTracking();
        int inputTokens = message.getInputTokens() != null ? message.getInputTokens() : 0;
        int outputTokens = message.getOutputTokens() != null ? message.getOutputTokens() : 0;
        return (inputTokens / 1000.0) * pricing.pricePer1kInputTokens()
                + (outputTokens / 1000.0) * pricing.pricePer1kOutputTokens();
    }

    private double resolveSatisfactionRate(OffsetDateTime since) {
        long total = feedbackRepository.countByCreatedAtAfter(since);
        if (total == 0) {
            return 0.0;
        }
        long positive = feedbackRepository.countByRatingAndCreatedAtAfter(POSITIVE_RATING, since);
        return (double) positive / total;
    }
}
