package com.flywhl.saa.logging;

import com.flywhl.saa.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 同步问答：业务日志与 AUDIT 日志共享同一 MDC traceId。
 *
 * @author flywhl
 */
@RestController
public class LoggingController {

    private static final Logger log = LoggerFactory.getLogger(LoggingController.class);

    private final ChatClient chatClient;

    public LoggingController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/log/chat")
    public Result<String> chat(@RequestParam(defaultValue = "用一句话介绍结构化日志") String message) {
        log.info("收到聊天请求 messageLength={}", message.length());
        String content = chatClient.prompt().user(message).call().content();
        log.info("聊天完成 contentLength={}", content.length());
        return Result.ok(content);
    }
}
