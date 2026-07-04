package com.flywhl.saa.agenthitl;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HITL Agent：HumanInTheLoopHook.approvalOn 替代教程伪 API interruptBefore。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class HitlAgentConfig {

    @Bean
    public ReactAgent approvalAgent(ChatModel dashScopeChatModel, HighRiskTools highRiskTools) {
        HumanInTheLoopHook hitl = HumanInTheLoopHook.builder()
                .approvalOn("execute_payment", "支付操作需人工确认")
                .build();

        return ReactAgent.builder()
                .name("approval-agent")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是支付助手。当用户要求支付时，必须调用 execute_payment 工具完成扣款。
                        不要编造支付结果；工具未执行成功前不得声称已支付。
                        """)
                .methodTools(highRiskTools)
                .hooks(hitl, ModelCallLimitHook.builder().runLimit(6).build())
                .saver(new MemorySaver())
                .build();
    }
}
