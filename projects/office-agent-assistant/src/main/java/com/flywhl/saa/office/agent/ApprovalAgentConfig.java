package com.flywhl.saa.office.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class ApprovalAgentConfig {
    @Bean
    ReactAgent approvalSummaryAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("approval-summary-agent")
                .description("提取审批单要点摘要")
                .model(dashScopeChatModel)
                .systemPrompt("你是审批摘要助手。阅读审批单正文，输出不超过120字的要点摘要。")
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }
    @Bean
    ReactAgent approvalOpinionAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("approval-opinion-agent")
                .description("生成标准审批初审意见 JSON")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是审批初审助手。结合上游摘要与审批单，输出 JSON：
                        {"summary":要点,"compliance":合规结论,"suggestion":"APPROVE|REJECT|ESCALATE","reason":理由}
                        """)
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }
    @Bean
    ReactAgent approvalEscalationAgent(ChatModel dashScopeChatModel) {
        return ReactAgent.builder()
                .name("approval-escalation-agent")
                .description("大额审批升级链路意见")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是大额审批升级助手。金额超过阈值时必须 suggestion=ESCALATE，
                        输出 JSON：{"summary":要点,"compliance":合规结论,"suggestion":"ESCALATE","reason":升级理由}
                        """)
                .hooks(ModelCallLimitHook.builder().runLimit(3).build())
                .build();
    }
    @Bean
    SequentialAgent approvalSequentialPipeline(ReactAgent approvalSummaryAgent, ReactAgent approvalOpinionAgent) {
        return SequentialAgent.builder()
                .name("approval-sequential-pipeline")
                .description("审批：摘要 → 初审意见")
                .subAgents(List.of(approvalSummaryAgent, approvalOpinionAgent))
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }
    @Bean
    LlmRoutingAgent approvalRoutingAgent(ChatModel dashScopeChatModel,
            SequentialAgent approvalSequentialPipeline, ReactAgent approvalEscalationAgent) {
        return LlmRoutingAgent.builder()
                .name("approval-routing-agent")
                .description("按金额/复杂度路由标准或升级审批链路")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是审批路由助手。若输入含大额/升级/超过阈值等关键词，路由 approval-escalation-agent；
                        否则路由 approval-sequential-pipeline。
                        """)
                .subAgents(List.of(approvalSequentialPipeline, approvalEscalationAgent))
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }
}

