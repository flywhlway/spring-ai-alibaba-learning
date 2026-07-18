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
import com.flywhl.saa.smartcs.tool.ToolSecuritySupport;

/**
 * 客服编排统一入口：以 {@code conversationId} 作 {@link RunnableConfig#threadId(String)}
 * 调用顶层 {@code csIntentRouter}（{@link LlmRoutingAgent}），提取路由后文本，并识别
 * {@link InterruptionMetadata} 中断态（人工升级分支挂载的
 * {@code HumanInTheLoopHook} 拦截 {@code requestHumanHandoff} 时产生），供 Wave 4
 * SSE 会话网关与 HITL Controller 消费。
 *
 * <p>编排入口将当前用户 {@code userId}/{@code role}/{@code conversationId} 写入
 * {@link RunnableConfig} metadata，经 Agent 框架注入 {@code ToolContext}（威胁登记 T-06-06）。
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
     * @param userId         当前用户 ID（注入 ToolContext）
     * @param role           当前用户角色名（注入 ToolContext）
     */
    public OrchestratorResult invoke(String conversationId, String question, Long userId, String role) {
        if (userId == null) {
            throw new BizException(CommonResultCode.UNAUTHORIZED, "编排调用缺少 userId");
        }
        if (role == null || role.isBlank()) {
            throw new BizException(CommonResultCode.UNAUTHORIZED, "编排调用缺少 role");
        }
        String threadId = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(ToolSecuritySupport.META_USER_ID, userId)
                .addMetadata(ToolSecuritySupport.META_ROLE, role)
                .addMetadata(ToolSecuritySupport.META_CONVERSATION_ID, threadId)
                .build();
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
