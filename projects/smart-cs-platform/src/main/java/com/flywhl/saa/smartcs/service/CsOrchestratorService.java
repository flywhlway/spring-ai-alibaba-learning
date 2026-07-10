package com.flywhl.saa.smartcs.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.agent.FlowStateExtractor;

/**
 * 客服编排统一入口：以 {@code conversationId} 作 {@link RunnableConfig#threadId(String)}
 * 调用顶层 {@code csIntentRouter}（{@link LlmRoutingAgent}），提取路由后文本，并识别
 * {@link InterruptionMetadata} 中断态（人工升级分支挂载的
 * {@code HumanInTheLoopHook} 拦截 {@code requestHumanHandoff} 时产生），供 Wave 4
 * SSE 会话网关与 HITL Controller 消费。
 *
 * <p>本 Wave 不实现 SSE 流式与 HITL 恢复（approve/resume），仅提供同步 invoke 入口与
 * 中断态透传；{@code routeAgent} 归一化为四类常量供 {@code cs_message.route_agent} 落库
 * （落库动作由 Wave 4 {@code ChatService} 执行）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class CsOrchestratorService {

    private final LlmRoutingAgent csIntentRouter;

    public CsOrchestratorService(LlmRoutingAgent csIntentRouter) {
        this.csIntentRouter = csIntentRouter;
    }

    /**
     * @param conversationId 会话 ID（UUID 字符串）；为空时自动生成，禁止自增 ID 作 threadId
     * @param question       用户问题原文
     */
    public OrchestratorResult invoke(String conversationId, String question) {
        String threadId = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        // 子 Agent（ticket-agent/human-escalation-agent）需要 conversationId 作为工具入参，
        // 通过消息前缀标记传递，系统 Prompt 已约定原样透传给 createTicket/urgeTicket/requestHumanHandoff。
        String taggedQuestion = "[conversationId=" + threadId + "] " + question;

        try {
            Optional<NodeOutput> output = csIntentRouter.invokeAndGetOutput(taggedQuestion, config);
            if (output.isEmpty()) {
                return new OrchestratorResult("", RouteAgent.UNKNOWN, false, null);
            }

            NodeOutput nodeOutput = output.get();
            if (nodeOutput instanceof InterruptionMetadata interruption) {
                return new OrchestratorResult("", resolveRouteAgent(interruption.agent()), true, interruption);
            }

            String text = FlowStateExtractor.extractText(Optional.of(nodeOutput.state()));
            return new OrchestratorResult(text, resolveRouteAgent(nodeOutput.agent()), false, null);
        } catch (GraphRunnerException ex) {
            throw new BizException(CommonResultCode.INTERNAL_ERROR, "智能体编排调用失败：" + ex.getMessage(), ex);
        }
    }

    /**
     * 将 {@link NodeOutput#agent()} 返回的具体子 Agent bean 名归一化为 {@link RouteAgent} 四类常量。
     */
    private static String resolveRouteAgent(String agentName) {
        if (agentName == null) {
            return RouteAgent.UNKNOWN;
        }
        return switch (agentName) {
            case "faq-agent" -> RouteAgent.FAQ;
            case "business-supervisor", "order-agent", "aftersales-agent", "techsupport-agent" ->
                RouteAgent.BUSINESS;
            case "ticket-agent" -> RouteAgent.TICKET;
            case "human-escalation-agent" -> RouteAgent.HUMAN;
            default -> RouteAgent.UNKNOWN;
        };
    }

    /**
     * 编排结果：{@code interrupted=true} 时 {@code answer} 为空，{@code interruptionMetadata}
     * 携带待审批工具调用，供 Wave 4 HITL Controller 落工单 {@code PENDING_HUMAN} 并返回坐席确认引导。
     */
    public record OrchestratorResult(
            String answer,
            String routeAgent,
            boolean interrupted,
            InterruptionMetadata interruptionMetadata) {
    }

    /** {@code cs_message.route_agent} 落库常量枚举（Wave 4 {@code ChatService} 消费）。 */
    public static final class RouteAgent {
        public static final String FAQ = "FAQ";
        public static final String BUSINESS = "BUSINESS";
        public static final String TICKET = "TICKET";
        public static final String HUMAN = "HUMAN";
        public static final String UNKNOWN = "UNKNOWN";

        private RouteAgent() {
        }
    }
}
