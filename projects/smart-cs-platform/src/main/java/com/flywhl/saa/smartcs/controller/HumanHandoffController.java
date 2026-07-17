package com.flywhl.saa.smartcs.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata.ToolFeedback;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata.ToolFeedback.FeedbackResult;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.model.dto.HandoffApproveRequest;
import com.flywhl.saa.smartcs.model.dto.HandoffStartRequest;
import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.HitlSessionResponse;
import com.flywhl.saa.smartcs.service.AuthService;
import com.flywhl.saa.smartcs.service.TicketService;

import jakarta.validation.Valid;

/**
 * 人工接管 HITL API：start 触发 {@code humanEscalationAgent} 中断；approve 携带
 * {@code addHumanFeedback} + {@code resume()} 恢复，并推进工单至 HUMAN_HANDLING。
 *
 * <p>演示环境用进程内 {@link ConcurrentHashMap} 缓存 pending
 * {@link InterruptionMetadata}；生产应持久化（Redis/DB），进程重启会丢失待审批会话。
 *
 * <p>{@code threadId} 与 {@code cs_conversation.conversation_id} 绑定（全链路 UUID）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/handoff")
public class HumanHandoffController {

    private final ReactAgent humanEscalationAgent;
    private final TicketService ticketService;
    private final AuthService authService;

    /**
     * 演示用内存 pending 表；生产应改为 Redis/DB 持久化。
     */
    private final ConcurrentHashMap<String, InterruptionMetadata> pendingByThread = new ConcurrentHashMap<>();

    public HumanHandoffController(
            @Qualifier("humanEscalationAgent") ReactAgent humanEscalationAgent,
            TicketService ticketService,
            AuthService authService) {
        this.humanEscalationAgent = humanEscalationAgent;
        this.ticketService = ticketService;
        this.authService = authService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public Result<HitlSessionResponse> start(@Valid @RequestBody HandoffStartRequest request)
            throws GraphRunnerException {
        SysUser user = authService.requireCurrentUser();
        String threadId = StringUtils.hasText(request.conversationId())
                ? request.conversationId().trim()
                : UUID.randomUUID().toString();

        String taggedQuery = "[conversationId=" + threadId + "] " + request.query();
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Optional<NodeOutput> output = humanEscalationAgent.invokeAndGetOutput(taggedQuery, config);

        if (output.isPresent() && output.get() instanceof InterruptionMetadata interruption) {
            pendingByThread.put(threadId, interruption);
            CsTicket ticket = ticketService.transitionToPendingHuman(
                    threadId, user.getId(), request.query(), user.getRole().name());
            return Result.ok(new HitlSessionResponse(
                    threadId,
                    "PENDING_HUMAN",
                    ticket.getTicketNo(),
                    "等待坐席确认后调用 POST /api/handoff/approve?threadId=" + threadId));
        }

        String answer = extractText(output.orElse(null));
        return Result.ok(new HitlSessionResponse(threadId, "COMPLETED", null, answer));
    }

    /**
     * 坐席审批恢复。支持 query 参数 {@code threadId}（计划契约）与 JSON body
     * {@link HandoffApproveRequest}（api.http 契约）。
     */
    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public Result<HitlSessionResponse> approve(
            @RequestParam(required = false) String threadId,
            @RequestBody(required = false) HandoffApproveRequest body) throws GraphRunnerException {
        String resolvedThreadId = resolveThreadId(threadId, body);
        SysUser agent = authService.requireCurrentUser();

        InterruptionMetadata pending = pendingByThread.remove(resolvedThreadId);
        if (pending == null) {
            throw new BizException(CommonResultCode.NOT_FOUND,
                    "无待审批会话或 threadId 无效：" + resolvedThreadId);
        }

        InterruptionMetadata approvedFeedback = buildApprovedFeedback(pending);
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(resolvedThreadId)
                .addHumanFeedback(approvedFeedback)
                .resume()
                .build();

        Optional<NodeOutput> output = humanEscalationAgent.invokeAndGetOutput(Map.of(), resumeConfig);

        if (output.isPresent() && output.get() instanceof InterruptionMetadata again) {
            pendingByThread.put(resolvedThreadId, again);
            String ticketNo = ticketService.findLatestByConversationId(resolvedThreadId)
                    .map(CsTicket::getTicketNo)
                    .orElse(null);
            return Result.ok(new HitlSessionResponse(
                    resolvedThreadId,
                    "PENDING_HUMAN",
                    ticketNo,
                    "仍有待审批工具调用，请再次 approve"));
        }

        CsTicket ticket = ticketService.transitionToHumanHandling(
                resolvedThreadId, agent.getId(), agent.getRole().name());
        return Result.ok(new HitlSessionResponse(
                resolvedThreadId,
                "HUMAN_HANDLING",
                ticket.getTicketNo(),
                extractText(output.orElse(null))));
    }

    private static String resolveThreadId(String queryParam, HandoffApproveRequest body) {
        if (StringUtils.hasText(queryParam)) {
            return queryParam.trim();
        }
        if (body != null && StringUtils.hasText(body.threadId())) {
            return body.threadId().trim();
        }
        throw new BizException(CommonResultCode.BAD_REQUEST, "threadId 不能为空");
    }

    private static InterruptionMetadata buildApprovedFeedback(InterruptionMetadata pending) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder(pending.node(), pending.state());
        for (ToolFeedback feedback : pending.toolFeedbacks()) {
            builder.addToolFeedback(ToolFeedback.builder(feedback)
                    .result(FeedbackResult.APPROVED)
                    .build());
        }
        return builder.build();
    }

    private static String extractText(NodeOutput output) {
        if (output == null || output.state() == null) {
            return "";
        }
        return com.flywhl.saa.smartcs.agent.FlowStateExtractor.extractText(Optional.of(output.state()));
    }
}
