package com.flywhl.saa.fallback;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import com.flywhl.saa.starter.routing.FallbackModelRouter;
import com.flywhl.saa.starter.routing.ModelRouter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 降级演示：{@code reportFailure} 触发主备切换，{@code /fallback/status} 读取熔断状态。
 *
 * @author flywhl
 */
@RestController
public class FallbackController {

    private static final int FAILURE_THRESHOLD = 3;

    private final ModelRouter modelRouter;
    private final ChatModel primaryModel;
    private final AuditLoggingAdvisor auditLoggingAdvisor;

    public FallbackController(ModelRouter modelRouter,
                              @Qualifier("dashScopeChatModel") ChatModel primaryModel,
                              AuditLoggingAdvisor auditLoggingAdvisor) {
        this.modelRouter = modelRouter;
        this.primaryModel = primaryModel;
        this.auditLoggingAdvisor = auditLoggingAdvisor;
    }

    @GetMapping("/fallback/chat")
    public Result<String> chat(@RequestParam(defaultValue = "用一句话介绍模型降级") String message,
                               @RequestParam(defaultValue = "false") boolean forceFail) {
        if (forceFail) {
            simulatePrimaryFailures();
        }

        ChatModel model = modelRouter.route();
        try {
            return Result.ok(callWithModel(model, message));
        } catch (Exception ex) {
            modelRouter.reportFailure(model, ex);
            ChatModel retryModel = modelRouter.route();
            return Result.ok(callWithModel(retryModel, message));
        }
    }

    @GetMapping("/fallback/status")
    public Result<FallbackStatus> status() {
        boolean active = modelRouter instanceof FallbackModelRouter router && router.isFallbackActive();
        return Result.ok(new FallbackStatus(active));
    }

    private void simulatePrimaryFailures() {
        for (int i = 0; i < FAILURE_THRESHOLD; i++) {
            modelRouter.reportFailure(primaryModel, new RuntimeException("simulated-primary-failure-" + (i + 1)));
        }
    }

    private String callWithModel(ChatModel model, String message) {
        return ChatClient.builder(model)
                .defaultAdvisors(auditLoggingAdvisor)
                .build()
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
