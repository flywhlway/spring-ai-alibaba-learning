package com.flywhl.saa.mcpnacos.client;

import com.alibaba.cloud.ai.mcp.discovery.client.tool.DistributedSyncMcpToolCallbackProvider;
import com.alibaba.cloud.ai.mcp.discovery.client.transport.DistributedSyncMcpClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Nacos MCP Client 订阅为异步；ChatClient 在首次请求前等待 McpSyncClient 就绪。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class NacosMcpChatClientConfig {

    private static final Duration READY_TIMEOUT = Duration.ofSeconds(90);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @Bean
    Supplier<ChatClient> nacosAssistantChatClientSupplier(
            ChatClient.Builder chatClientBuilder,
            @Qualifier("streamableWebFluxDistributedSyncClients")
            List<DistributedSyncMcpClient> distributedSyncMcpClients) {
        return () -> chatClientBuilder
                .defaultSystem("你是企业办公助手，可以查询订单状态。请优先使用可用工具回答。")
                .defaultToolCallbacks(new DistributedSyncMcpToolCallbackProvider(distributedSyncMcpClients))
                .build();
    }

    static void awaitMcpClientsReady(List<DistributedSyncMcpClient> clients) {
        long deadline = System.nanoTime() + READY_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (clients.stream().allMatch(NacosMcpChatClientConfig::isClientReady)) {
                return;
            }
            try {
                Thread.sleep(POLL_INTERVAL.toMillis());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("等待 Nacos MCP Client 就绪被中断", ex);
            }
        }
        throw new IllegalStateException("Nacos MCP Client 在 " + READY_TIMEOUT.getSeconds()
                + "s 内未就绪，请确认 order-mcp-server 已注册 order-service-mcp");
    }

    private static boolean isClientReady(DistributedSyncMcpClient client) {
        try {
            client.getMcpSyncClient();
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }
}
