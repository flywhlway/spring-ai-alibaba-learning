package com.flywhl.saa.agentdemo;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 车辆诊断 ReactAgent：methodTools + ModelCallLimitHook（替代教程伪 API maxIterations）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class VehicleDiagnosisAgentConfig {

    @Bean
    public ReactAgent vehicleDiagnosisAgent(ChatModel dashScopeChatModel, DtcLookupTools dtcLookupTools) {
        return ReactAgent.builder()
                .name("vehicle-diagnosis-agent")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是资深车辆故障诊断专家。收到故障码时：
                        1. 先调用工具查询故障码的标准解释
                        2. 结合解释给出可能原因（按可能性排序）
                        3. 给出具体的排查建议
                        """)
                .methodTools(dtcLookupTools)
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .saver(new MemorySaver())
                .build();
    }
}
