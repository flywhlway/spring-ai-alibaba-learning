package com.flywhl.saa.mcpnacos.client;

import com.alibaba.cloud.ai.mcp.discovery.client.transport.DistributedSyncMcpClient;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Supplier;

/**
 * 工具来自 Nacos 服务发现的 {@code order-service-mcp}，Client 不硬编码 Server 地址。
 *
 * @author flywhl
 */
@RestController
public class AssistantController {

    private final Supplier<ChatClient> chatClientSupplier;
    private final List<DistributedSyncMcpClient> distributedSyncMcpClients;
    private volatile ChatClient chatClient;

    public AssistantController(Supplier<ChatClient> nacosAssistantChatClientSupplier,
                               @Qualifier("streamableWebFluxDistributedSyncClients")
                               List<DistributedSyncMcpClient> distributedSyncMcpClients) {
        this.chatClientSupplier = nacosAssistantChatClientSupplier;
        this.distributedSyncMcpClients = distributedSyncMcpClients;
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        return Result.ok(chatClient().prompt().user(question).call().content());
    }

    private ChatClient chatClient() {
        ChatClient local = chatClient;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (chatClient == null) {
                NacosMcpChatClientConfig.awaitMcpClientsReady(distributedSyncMcpClients);
                chatClient = chatClientSupplier.get();
            }
            return chatClient;
        }
    }
}
