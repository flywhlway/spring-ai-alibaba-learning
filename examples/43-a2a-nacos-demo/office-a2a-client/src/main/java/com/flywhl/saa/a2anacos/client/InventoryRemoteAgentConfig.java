package com.flywhl.saa.a2anacos.client;

import com.alibaba.cloud.ai.a2a.registry.nacos.discovery.NacosAgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A2A 远程 Agent：通过 Nacos 发现 AgentCard（禁止硬编码 URL / nacosServiceName）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class InventoryRemoteAgentConfig {

    @Bean
    A2aRemoteAgent inventoryRemoteAgent(NacosAgentCardProvider nacosAgentCardProvider) {
        return A2aRemoteAgent.builder()
                .name("inventory-agent")
                .description("通过 Nacos 发现的远程库存查询智能体")
                .agentCardProvider(nacosAgentCardProvider)
                .build();
    }
}
