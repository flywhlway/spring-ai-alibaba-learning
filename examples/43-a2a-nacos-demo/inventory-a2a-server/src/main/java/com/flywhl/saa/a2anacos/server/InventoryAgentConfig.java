package com.flywhl.saa.a2anacos.server;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 暴露为 A2A Server 根 Agent 的库存 ReactAgent（自动装配 AgentCard + GraphAgentExecutor）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class InventoryAgentConfig {

    @Bean
    ReactAgent inventoryAgent(ChatModel dashScopeChatModel, InventoryTools inventoryTools) {
        return ReactAgent.builder()
                .name("inventory-agent")
                .description("库存查询智能体，支持按 SKU 查询库存数量")
                .model(dashScopeChatModel)
                .systemPrompt("你是库存查询助手。收到 SKU 查询时优先调用 queryStock 工具，简洁回答库存数量与状态。")
                .methodTools(inventoryTools)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }
}
