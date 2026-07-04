package com.flywhl.saa.multiagent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.CountLoopStrategy;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 四模式 FlowAgent 与子 ReactAgent 装配（JAR 真 API，非教程伪写法）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class MultiAgentConfig {

    @Bean
    ReactAgent queryRewriteAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("query-rewrite-agent")
                .description("将用户问题改写为更清晰的专业查询")
                .model(dashScopeChatModel)
                .systemPrompt("你是查询改写助手。将用户问题改写为简洁、专业的检索查询，只输出改写结果，不超过两句话。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }

    @Bean
    ReactAgent answerAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("answer-agent")
                .description("基于上下文生成最终回答")
                .model(dashScopeChatModel)
                .systemPrompt("你是故障诊断助手。根据上游改写后的问题给出简洁、可操作的回答。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }

    @Bean
    SequentialAgent sequentialPipeline(ReactAgent queryRewriteAgent, ReactAgent answerAgent) {
        return SequentialAgent.builder()
                .name("sequential-pipeline")
                .description("顺序：改写 → 回答")
                .subAgents(List.of(queryRewriteAgent, answerAgent))
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }

    @Bean
    ReactAgent knowledgeBaseAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("kb-search-agent")
                .description("从知识库检索故障码解释")
                .model(dashScopeChatModel)
                .systemPrompt("你是知识库检索助手。针对故障码问题，输出一段标准解释（模拟 KB 命中），不超过 80 字。")
                .hooks(ModelCallLimitHook.builder().runLimit(2).build())
                .build();
    }

    @Bean
    ReactAgent ticketHistoryAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("ticket-history-agent")
                .description("从工单历史检索相似案例")
                .model(dashScopeChatModel)
                .systemPrompt("你是工单历史助手。针对故障码问题，输出一条相似历史工单摘要（模拟检索），不超过 80 字。")
                .hooks(ModelCallLimitHook.builder().runLimit(2).build())
                .build();
    }

    @Bean
    ParallelAgent parallelSearch(ReactAgent knowledgeBaseAgent, ReactAgent ticketHistoryAgent) {
        return ParallelAgent.builder()
                .name("parallel-search")
                .description("并行检索知识库与工单历史")
                .subAgents(List.of(knowledgeBaseAgent, ticketHistoryAgent))
                .maxConcurrency(2)
                .mergeOutputKey("mergedContext")
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent presalesAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("presales-agent")
                .description("处理售前咨询：价格、配置、购买流程")
                .model(dashScopeChatModel)
                .systemPrompt("你是售前顾问，只回答价格、配置、购买流程相关问题，简洁专业。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }

    @Bean
    ReactAgent afterSalesAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("aftersales-agent")
                .description("处理售后 support：退换货、保修、物流")
                .model(dashScopeChatModel)
                .systemPrompt("你是售后客服，只回答退换货、保修、物流相关问题，简洁专业。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }

    @Bean
    ReactAgent techSupportAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("techsupport-agent")
                .description("处理技术支持：故障排查、安装配置")
                .model(dashScopeChatModel)
                .systemPrompt("你是技术支持工程师，只回答故障排查、安装配置相关问题，简洁专业。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }

    @Bean
    LlmRoutingAgent customerServiceRouter(ChatModel dashScopeChatModel,
                                          ReactAgent presalesAgent,
                                          ReactAgent afterSalesAgent,
                                          ReactAgent techSupportAgent) {
        return LlmRoutingAgent.builder()
                .name("customer-service-router")
                .description("根据意图路由到售前/售后/技术支持")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是客服路由助手。根据用户问题选择最合适的子智能体：
                        - presales-agent：售前咨询
                        - aftersales-agent：售后 support
                        - techsupport-agent：技术支持
                        只路由到其中一个子智能体处理。
                        """)
                .subAgents(List.of(presalesAgent, afterSalesAgent, techSupportAgent))
                .hooks(ModelCallLimitHook.builder().runLimit(5).build())
                .build();
    }

    @Bean
    ReactAgent refineAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("refine-agent")
                .description("生成并自我改进回答")
                .model(dashScopeChatModel)
                .systemPrompt("你是写作助手。根据用户问题生成简短回答；若已有上一轮输出，则在原基础上改进表述。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }

    @Bean
    LoopAgent refineLoop(ReactAgent refineAgent) {
        return LoopAgent.builder()
                .name("refine-loop")
                .description("有限次循环改进回答")
                .subAgent(refineAgent)
                .loopStrategy(new CountLoopStrategy(2))
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }
}
