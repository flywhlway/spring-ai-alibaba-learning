package com.flywhl.saa.advisor;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 内置 Advisor 组合：{@link SafeGuardAdvisor}（靠前拦截敏感词）+
 * {@link SimpleLoggerAdvisor}（默认 order=0 记录请求/响应）。
 *
 * <p>观察日志顺序即可验证"栈"语义：order 更小的 Advisor 先处理请求、后处理响应。
 *
 * @author flywhl
 */
@RestController
public class AdvisorDemoController {

    private final ChatClient chatClient;

    public AdvisorDemoController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("违禁词", "敏感内容"))
                                .failureResponse("请求包含敏感内容，已被安全策略拦截")
                                .order(Ordered.HIGHEST_PRECEDENCE + 50)
                                .build(),
                        new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        return Result.ok(chatClient.prompt().user(question).call().content());
    }

    /**
     * 返回本 Demo 注册的 Advisor 顺序说明，便于对照日志。
     */
    @GetMapping("/advisors")
    public Result<List<Map<String, Object>>> advisors() {
        return Result.ok(List.of(
                Map.of("name", "SafeGuardAdvisor", "order", Ordered.HIGHEST_PRECEDENCE + 50,
                        "role", "请求方向最先执行：命中敏感词则短路，不消耗模型配额"),
                Map.of("name", "SimpleLoggerAdvisor", "order", 0,
                        "role", "记录请求/响应；响应方向先于 SafeGuard 打印")));
    }
}
