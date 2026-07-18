package com.flywhl.saa.smartcs.agent;

import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.flywhl.saa.smartcs.config.ConfigurableModelRouter;
import com.flywhl.saa.smartcs.tool.FaqTool;
import com.flywhl.saa.smartcs.tool.HandoffTools;
import com.flywhl.saa.smartcs.tool.OrderTool;
import com.flywhl.saa.smartcs.tool.TicketTools;

/**
 * Phase 6 多智能体编排（JAR 真 API，非教程伪写法）：
 *
 * <ul>
 *   <li>{@code cs-intent-router}（{@link LlmRoutingAgent}）：顶层意图路由，四分支
 *       FAQ / 业务 / 工单 / 人工；</li>
 *   <li>{@code business-supervisor}（{@link ReactAgent} + {@link AgentTool#create}）：
 *       Supervisor 模式调度订单/售后/技术子 Agent；</li>
 *   <li>{@code human-escalation-agent}：挂载 {@link HumanInTheLoopHook#approvalOn}
 *       拦截 {@code requestHumanHandoff}，需坐席审批后才真正执行；</li>
 *   <li>{@code faq-parallel-context}（{@link ParallelAgent}）：演示知识库 + 工单历史
 *       并行检索的合并输出模式（{@code mergeOutputKey=mergedContext}）。</li>
 * </ul>
 *
 * <p>各 Agent 经 {@link ConfigurableModelRouter#routeForScene} 按 scene 选模型，
 * 使管理端 {@code model_profile} 对 BUSINESS/TICKET/FAQ 路径生效。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class CsAgentConfig {

    /**
     * 人工升级 HITL 与顶层路由共用同一 {@link MemorySaver}，保证 chat 中断与
     * {@code /api/handoff/approve} 使用相同 threadId 可 resume。
     */
    @Bean
    MemorySaver csHitlMemorySaver() {
        return new MemorySaver();
    }

    @Bean
    ReactAgent faqAgent(ConfigurableModelRouter modelRouter, FaqTool faqTool) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_FAQ);
        return ReactAgent.builder()
                .name("faq-agent")
                .description("处理标准 FAQ、政策、流程类可检索问题")
                .model(model)
                .systemPrompt("""
                        你是智能客服 FAQ 助手。收到用户问题后必须调用 answerFaq 工具获取答案，
                        不要凭空编造答案；将工具返回结果直接作为最终回复，可适当润色语气使其更亲切。
                        """)
                .methodTools(faqTool)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent knowledgeBaseAgent(ConfigurableModelRouter modelRouter) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_FAQ);
        return ReactAgent.builder()
                .name("kb-search-agent")
                .description("并行检索：从知识库检索相关政策/文档摘要")
                .model(model)
                .systemPrompt("你是知识库检索助手。针对用户问题输出一段知识库命中摘要（模拟检索），不超过 80 字。")
                .hooks(ModelCallLimitHook.builder().runLimit(2).build())
                .build();
    }

    @Bean
    ReactAgent ticketHistoryAgent(ConfigurableModelRouter modelRouter) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_TICKET);
        return ReactAgent.builder()
                .name("ticket-history-agent")
                .description("并行检索：从历史工单中检索相似案例摘要")
                .model(model)
                .systemPrompt("你是工单历史检索助手。针对用户问题输出一条相似历史工单摘要（模拟检索），不超过 80 字。")
                .hooks(ModelCallLimitHook.builder().runLimit(2).build())
                .build();
    }

    /**
     * 并行子智能体演示：知识库 + 工单历史并行拉取上下文，合并输出到 {@code mergedContext}。
     * 独立于顶层路由发布为 Bean，供后续波次（SSE 前置增强/可观测演示）按需消费。
     */
    @Bean
    ParallelAgent faqParallelContext(ReactAgent knowledgeBaseAgent, ReactAgent ticketHistoryAgent) {
        return ParallelAgent.builder()
                .name("faq-parallel-context")
                .description("并行拉取知识库与工单历史上下文，供 FAQ 前置增强参考")
                .subAgents(List.of(knowledgeBaseAgent, ticketHistoryAgent))
                .maxConcurrency(2)
                .mergeOutputKey("mergedContext")
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent orderAgent(ConfigurableModelRouter modelRouter, OrderTool orderTool) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_BUSINESS);
        return ReactAgent.builder()
                .name("order-agent")
                .description("处理订单查询、物流状态相关问题")
                .model(model)
                .systemPrompt("你专门负责订单与物流查询，优先调用 queryOrderStatus 工具，回复简洁。")
                .methodTools(orderTool)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent afterSalesAgent(ConfigurableModelRouter modelRouter, TicketTools ticketTools) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_BUSINESS);
        return ReactAgent.builder()
                .name("aftersales-agent")
                .description("处理退换货、保修等售后问题")
                .model(model)
                .systemPrompt("""
                        你专门负责售后 support（退换货、保修）。若无法当场解决，调用 createTicket
                        工具为客户建单留痕，回复简洁专业。
                        """)
                .methodTools(ticketTools)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent techSupportAgent(ConfigurableModelRouter modelRouter, TicketTools ticketTools) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_BUSINESS);
        return ReactAgent.builder()
                .name("techsupport-agent")
                .description("处理故障排查、安装配置等技术支持问题")
                .model(model)
                .systemPrompt("""
                        你专门负责技术支持（故障排查、安装配置）。若无法当场解决，调用 createTicket
                        工具为客户建单留痕，回复简洁专业。
                        """)
                .methodTools(ticketTools)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent businessSupervisor(ConfigurableModelRouter modelRouter,
            ReactAgent orderAgent, ReactAgent afterSalesAgent, ReactAgent techSupportAgent) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_BUSINESS);
        return ReactAgent.builder()
                .name("business-supervisor")
                .description("业务总控：订单/物流/售后/技术复杂问题")
                .model(model)
                .tools(
                        AgentTool.create(orderAgent),
                        AgentTool.create(afterSalesAgent),
                        AgentTool.create(techSupportAgent))
                .systemPrompt("""
                        你是智能客服业务总控。根据用户需求调度专职助手：
                        - order-agent：订单查询、物流状态
                        - aftersales-agent：退换货、保修
                        - techsupport-agent：故障排查、安装配置
                        不要自己编造工具结果，通过调用对应助手完成任务后汇总回复。
                        """)
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }

    @Bean
    ReactAgent ticketAgent(ConfigurableModelRouter modelRouter, TicketTools ticketTools) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_TICKET);
        return ReactAgent.builder()
                .name("ticket-agent")
                .description("明确要求建单、查单、催单")
                .model(model)
                .systemPrompt("""
                        你专门负责工单操作。用户消息开头形如 [conversationId=xxx] 标记当前会话ID，
                        调用 createTicket/urgeTicket 工具时必须原样传入该 conversationId 参数；
                        queryTicketByNo 直接按工单号查询，无需 conversationId。
                        """)
                .methodTools(ticketTools)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent humanEscalationAgent(ConfigurableModelRouter modelRouter, HandoffTools handoffTools,
            MemorySaver csHitlMemorySaver) {
        ChatModel model = modelRouter.routeForScene(ConfigurableModelRouter.SCENE_BUSINESS);
        HumanInTheLoopHook hitl = HumanInTheLoopHook.builder()
                .approvalOn("requestHumanHandoff", "人工接管需坐席确认")
                .build();
        return ReactAgent.builder()
                .name("human-escalation-agent")
                .description("用户要求人工、投诉升级")
                .model(model)
                .systemPrompt("""
                        你是人工升级助手。用户消息开头形如 [conversationId=xxx] 标记当前会话ID，
                        当用户要求人工介入或投诉升级时，必须调用 requestHumanHandoff 工具（携带
                        conversationId 与 reason），不要自行承诺处理结果；该工具执行前需人工审批。
                        """)
                .methodTools(handoffTools)
                .hooks(hitl, ModelCallLimitHook.builder().runLimit(6).build())
                .saver(csHitlMemorySaver)
                .build();
    }

    @Bean
    LlmRoutingAgent csIntentRouter(ConfigurableModelRouter modelRouter,
            ReactAgent faqAgent, ReactAgent businessSupervisor,
            ReactAgent ticketAgent, ReactAgent humanEscalationAgent,
            MemorySaver csHitlMemorySaver) {
        // 意图路由无独立 scene，回退 starter 主备
        ChatModel model = modelRouter.route();
        return LlmRoutingAgent.builder()
                .name("cs-intent-router")
                .description("客服意图路由：FAQ / 业务 / 工单 / 人工")
                .model(model)
                .systemPrompt("""
                        根据用户问题选择唯一子智能体：
                        - faq-agent：标准 FAQ、政策、流程类可检索问题
                        - business-supervisor：订单/物流/售后/技术复杂问题
                        - ticket-agent：明确要求建单、查单、催单
                        - human-escalation-agent：用户要求人工、投诉升级
                        """)
                .subAgents(List.of(faqAgent, businessSupervisor, ticketAgent, humanEscalationAgent))
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .saver(csHitlMemorySaver)
                .build();
    }
}
