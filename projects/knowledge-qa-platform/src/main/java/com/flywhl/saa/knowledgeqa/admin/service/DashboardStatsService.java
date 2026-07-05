package com.flywhl.saa.knowledgeqa.admin.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.flywhl.saa.knowledgeqa.model.vo.DashboardStatsVO;
import com.flywhl.saa.knowledgeqa.repository.KbChunkRepository;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;
import com.flywhl.saa.knowledgeqa.repository.QaFeedbackRepository;
import com.flywhl.saa.knowledgeqa.repository.QaMessageRepository;
import com.flywhl.saa.starter.autoconfigure.SaaLearningProperties;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;

/**
 * 运营看板统计：问答量、Token 成本、反馈满意度、知识规模。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class DashboardStatsService {

    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final short POSITIVE_RATING = 1;

    private final QaMessageRepository messageRepository;
    private final QaFeedbackRepository feedbackRepository;
    private final KbDocumentRepository documentRepository;
    private final KbChunkRepository chunkRepository;
    private final SaaLearningProperties saaLearningProperties;
    private final MeterRegistry meterRegistry;

    public DashboardStatsService(
            QaMessageRepository messageRepository,
            QaFeedbackRepository feedbackRepository,
            KbDocumentRepository documentRepository,
            KbChunkRepository chunkRepository,
            SaaLearningProperties saaLearningProperties,
            MeterRegistry meterRegistry) {
        this.messageRepository = messageRepository;
        this.feedbackRepository = feedbackRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.saaLearningProperties = saaLearningProperties;
        this.meterRegistry = meterRegistry;
    }

    public DashboardStatsVO stats(int days) {
        int windowDays = Math.max(days, 1);
        OffsetDateTime since = OffsetDateTime.now().minusDays(windowDays);

        long totalQuestions = messageRepository.countByRoleAndCreatedAtAfter(ASSISTANT_ROLE, since);
        double totalCost = resolveTotalCost(since);
        double satisfactionRate = resolveSatisfactionRate(since);
        long documentCount = documentRepository.count();
        long chunkCount = chunkRepository.count();

        return new DashboardStatsVO(totalQuestions, totalCost, satisfactionRate, documentCount, chunkCount);
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
                .mapToDouble(counter -> counter.count() * saaLearningProperties.costTracking().pricePer1kInputTokens() / 1000.0)
                .sum();
    }

    private double estimateMessageCost(com.flywhl.saa.knowledgeqa.model.entity.QaMessage message) {
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
