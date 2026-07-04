package com.flywhl.saa.routing;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import com.flywhl.saa.starter.routing.ModelRouter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面向 {@link ModelRouter} 的路由问答：每次请求由 starter 主备策略选择 {@link ChatModel}。
 *
 * @author flywhl
 */
@RestController
public class RoutingController {

    private final ModelRouter modelRouter;
    private final AuditLoggingAdvisor auditLoggingAdvisor;

    public RoutingController(ModelRouter modelRouter, AuditLoggingAdvisor auditLoggingAdvisor) {
        this.modelRouter = modelRouter;
        this.auditLoggingAdvisor = auditLoggingAdvisor;
    }

    @GetMapping("/route/ask")
    public Result<String> ask(@RequestParam(defaultValue = "用一句话介绍多模型路由") String question) {
        ChatModel model = modelRouter.route();
        String content = ChatClient.builder(model)
                .defaultAdvisors(auditLoggingAdvisor)
                .build()
                .prompt()
                .user(question)
                .call()
                .content();
        return Result.ok(content);
    }
}
