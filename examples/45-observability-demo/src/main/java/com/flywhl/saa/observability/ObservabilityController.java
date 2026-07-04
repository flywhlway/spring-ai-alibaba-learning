package com.flywhl.saa.observability;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 触发一次模型调用，便于观测 gen_ai.* 指标与 starter 成本日志。
 *
 * @author flywhl
 */
@RestController
public class ObservabilityController {

    private final ChatClient chatClient;

    public ObservabilityController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/obs/chat")
    public Result<String> chat(@RequestParam(defaultValue = "用一句话介绍 Micrometer") String message) {
        String content = chatClient.prompt().user(message).call().content();
        return Result.ok(content);
    }
}
