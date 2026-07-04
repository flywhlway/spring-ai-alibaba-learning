package com.flywhl.saa.mcpclient;

import com.flywhl.saa.common.result.Result;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 将远程 MCP 工具挂到 ChatClient，供模型按需调用。
 *
 * @author flywhl
 */
@RestController
public class McpClientController {

    private final ChatClient chatClient;

    public McpClientController(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpSyncClients) {
        // 1.1.2 使用 defaultToolCallbacks(ToolCallbackProvider...)，等价教程 defaultTools(SyncMcpToolCallbackProvider)
        this.chatClient = chatClientBuilder
                .defaultSystem("你是企业办公助手，可以查询订单状态。请优先使用可用工具回答。")
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .build();
    }

    @GetMapping("/ask")
    public Result<String> askGet(@RequestParam String question) {
        return Result.ok(chatClient.prompt().user(question).call().content());
    }

    @PostMapping("/ask")
    public Result<String> askPost(@RequestParam(required = false) String question,
                                  @RequestBody(required = false) String body) {
        String q = question != null && !question.isBlank() ? question : body;
        return Result.ok(chatClient.prompt().user(q).call().content());
    }
}
