package com.flywhl.saa.customadvisor;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自定义 {@link AuditLoggingAdvisor} + 内置 {@link SimpleLoggerAdvisor}，
 * 验证脱敏只进审计日志、不进模型请求。
 *
 * @author flywhl
 */
@RestController
public class AdvisorDemoController {

    private final ChatClient chatClient;

    public AdvisorDemoController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new AuditLoggingAdvisor(),
                        new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        return Result.ok(chatClient.prompt().user(question).call().content());
    }
}
