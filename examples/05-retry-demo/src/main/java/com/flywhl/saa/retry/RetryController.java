package com.flywhl.saa.retry;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.retry.model.RetryPolicyVO;
import com.flywhl.saa.retry.model.RetryStatsVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 重试策略可读 + 正常问答入口（对应教程第 04 章 §4.8）。
 *
 * @author flywhl
 */
@RestController
public class RetryController {

    private final ChatClient chatClient;
    private final SpringAiRetryProperties retryProperties;
    private final RetryAttemptCounter attemptCounter;

    public RetryController(ChatClient.Builder chatClientBuilder,
                           SpringAiRetryProperties retryProperties,
                           RetryAttemptCounter attemptCounter) {
        this.chatClient = chatClientBuilder.build();
        this.retryProperties = retryProperties;
        this.attemptCounter = attemptCounter;
    }

    /**
     * 正常问答：底层 ChatModel 在瞬态错误时走自定义 {@code RetryTemplate}。
     */
    @GetMapping("/chat")
    public Result<String> chat(@RequestParam(defaultValue = "用一句话介绍重试机制") String message) {
        return Result.ok(chatClient.prompt().user(message).call().content());
    }

    /**
     * 展示当前 {@code spring.ai.retry.*} 绑定结果，便于核对配置是否生效。
     */
    @GetMapping("/retry/policy")
    public Result<RetryPolicyVO> policy() {
        SpringAiRetryProperties.Backoff backoff = retryProperties.getBackoff();
        return Result.ok(new RetryPolicyVO(
                retryProperties.getMaxAttempts(),
                backoff.getInitialInterval().toMillis(),
                backoff.getMultiplier(),
                backoff.getMaxInterval().toMillis()));
    }

    /**
     * 展示自定义 {@link RetryAttemptCounter} 累计的 open/error/success 次数。
     */
    @GetMapping("/retry/stats")
    public Result<RetryStatsVO> stats() {
        return Result.ok(new RetryStatsVO(
                attemptCounter.openCount(),
                attemptCounter.errorCount(),
                attemptCounter.successCount()));
    }
}
