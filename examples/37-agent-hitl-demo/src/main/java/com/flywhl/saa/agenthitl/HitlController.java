package com.flywhl.saa.agenthitl;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata.ToolFeedback;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata.ToolFeedback.FeedbackResult;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HITL 最小 REST：start 暂停于 execute_payment，approve 携带同一 UUID threadId 恢复。
 *
 * @author flywhl
 */
@RestController
public class HitlController {

    private static final String STATUS_PENDING = "PENDING_APPROVAL";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final ReactAgent approvalAgent;
    private final ConcurrentHashMap<String, InterruptionMetadata> pendingByThread = new ConcurrentHashMap<>();

    public HitlController(ReactAgent approvalAgent) {
        this.approvalAgent = approvalAgent;
    }

    @PostMapping("/hitl/start")
    public Result<HitlSessionResponse> start(@RequestParam String query) throws GraphRunnerException {
        String threadId = UUID.randomUUID().toString();
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Optional<NodeOutput> output = approvalAgent.invokeAndGetOutput(query, config);

        if (output.isPresent() && output.get() instanceof InterruptionMetadata interruption) {
            pendingByThread.put(threadId, interruption);
            return Result.ok(new HitlSessionResponse(
                    threadId,
                    STATUS_PENDING,
                    toPendingTools(interruption),
                    "等待人工确认后调用 POST /hitl/approve?threadId=" + threadId));
        }

        return Result.ok(new HitlSessionResponse(
                threadId,
                STATUS_COMPLETED,
                List.of(),
                extractAssistantText(output.orElse(null))));
    }

    @PostMapping("/hitl/approve")
    public Result<HitlSessionResponse> approve(@RequestParam String threadId) throws GraphRunnerException {
        InterruptionMetadata pending = pendingByThread.remove(threadId);
        if (pending == null) {
            throw new BizException(CommonResultCode.NOT_FOUND, "无待审批会话或 threadId 无效：" + threadId);
        }

        InterruptionMetadata approvedFeedback = buildApprovedFeedback(pending);
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addHumanFeedback(approvedFeedback)
                .resume()
                .build();

        Optional<NodeOutput> output = approvalAgent.invokeAndGetOutput(Map.of(), resumeConfig);
        if (output.isPresent() && output.get() instanceof InterruptionMetadata again) {
            pendingByThread.put(threadId, again);
            return Result.ok(new HitlSessionResponse(
                    threadId,
                    STATUS_PENDING,
                    toPendingTools(again),
                    "仍有待审批工具调用，请再次 approve"));
        }

        return Result.ok(new HitlSessionResponse(
                threadId,
                STATUS_COMPLETED,
                List.of(),
                extractAssistantText(output.orElse(null))));
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

    private static List<PendingToolCall> toPendingTools(InterruptionMetadata interruption) {
        List<PendingToolCall> tools = new ArrayList<>();
        for (ToolFeedback feedback : interruption.toolFeedbacks()) {
            tools.add(new PendingToolCall(
                    feedback.getId(),
                    feedback.getName(),
                    feedback.getArguments(),
                    feedback.getDescription()));
        }
        return List.copyOf(tools);
    }

    private static String extractAssistantText(NodeOutput output) {
        if (output == null || output.state() == null) {
            return "";
        }
        OverAllState state = output.state();
        List<Message> messages = state.value("messages", List.of());
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message instanceof AssistantMessage assistant) {
                String text = assistant.getText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }
}
