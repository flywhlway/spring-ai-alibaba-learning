package com.flywhl.saa.mcpnacos.client;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具来自 Nacos 服务发现的 {@code order-service-mcp}，Client 不硬编码 Server 地址。
 *
 * @author flywhl
 */
@RestController
public class AssistantController {

    private final ChatClient chatClient;

    public AssistantController(ChatClient.Builder chatClientBuilder,
                               ObjectProvider<ToolCallbackProvider> toolCallbackProviders) {
        ChatClient.Builder builder = chatClientBuilder
                .defaultSystem("你是企业办公助手，可以查询订单状态。请优先使用可用工具回答。");
        // SAA Nacos MCP Client 自动装配 DistributedSyncMcpToolCallbackProvider
        toolCallbackProviders.orderedStream().forEach(builder::defaultToolCallbacks);
        this.chatClient = builder.build();
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        return Result.ok(chatClient.prompt().user(question).call().content());
    }
}
