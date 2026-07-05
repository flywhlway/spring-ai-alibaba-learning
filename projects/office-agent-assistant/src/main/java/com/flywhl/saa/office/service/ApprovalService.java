package com.flywhl.saa.office.service;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.office.agent.FlowStateExtractor;
import com.flywhl.saa.office.config.OfficeProperties;
import com.flywhl.saa.office.model.entity.ApprovalRequest;
import com.flywhl.saa.office.model.vo.ApprovalReviewVO;
import com.flywhl.saa.office.prompt.PromptTemplateProvider;
import com.flywhl.saa.office.repository.ApprovalRequestRepository;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApprovalService {
    private final ApprovalRequestRepository approvalRequestRepository;
    private final SequentialAgent approvalSequentialPipeline;
    private final LlmRoutingAgent approvalRoutingAgent;
    private final PromptTemplateProvider promptTemplateProvider;
    private final OfficeProperties properties;
    public ApprovalService(ApprovalRequestRepository approvalRequestRepository,
            @Qualifier("approvalSequentialPipeline") SequentialAgent approvalSequentialPipeline,
            @Qualifier("approvalRoutingAgent") LlmRoutingAgent approvalRoutingAgent,
            PromptTemplateProvider promptTemplateProvider, OfficeProperties properties) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalSequentialPipeline = approvalSequentialPipeline;
        this.approvalRoutingAgent = approvalRoutingAgent;
        this.promptTemplateProvider = promptTemplateProvider;
        this.properties = properties;
    }
    @Transactional
    public ApprovalReviewVO review(Long approvalId) throws GraphRunnerException {
        ApprovalRequest request = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "审批单不存在"));
        String template = promptTemplateProvider.get("approval-opinion");
        String payload = template.replace("{request}", buildRequestText(request));
        RunnableConfig config = RunnableConfig.builder().threadId(UUID.randomUUID().toString()).build();
        Optional<OverAllState> state;
        if (needsEscalation(request)) {
            String routedInput = payload + "\n[系统提示：金额超过阈值，走升级链路]";
            state = approvalRoutingAgent.invoke(routedInput, config);
        } else {
            state = approvalSequentialPipeline.invoke(payload, config);
        }
        String opinion = FlowStateExtractor.extractText(state);
        String summary = extractSummary(opinion);
        request.setAiSummary(summary);
        request.setAiOpinion(opinion);
        request.setStatus("AI_REVIEWED");
        request.setUpdatedAt(LocalDateTime.now());
        approvalRequestRepository.save(request);
        return new ApprovalReviewVO(request.getId(), request.getRequestNo(), summary, opinion, request.getStatus());
    }
    private boolean needsEscalation(ApprovalRequest request) {
        BigDecimal amount = request.getAmount();
        return amount != null && amount.compareTo(BigDecimal.valueOf(properties.approval().escalationThreshold())) > 0;
    }
    private String buildRequestText(ApprovalRequest request) {
        return "单号:" + request.getRequestNo() + " 类型:" + request.getType()
                + " 标题:" + request.getTitle() + " 金额:" + request.getAmount() + "\n" + request.getContent();
    }
    private String extractSummary(String opinion) {
        if (opinion == null) return "";
        int idx = opinion.indexOf("summary");
        return idx >= 0 ? opinion.substring(0, Math.min(opinion.length(), 200)) : opinion.substring(0, Math.min(opinion.length(), 120));
    }
}

