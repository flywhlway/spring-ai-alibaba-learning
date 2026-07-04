package com.flywhl.saa.routing;

import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import com.flywhl.saa.starter.routing.ModelRouter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证 ModelRouter 装配与一次真实调用。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class RoutingDemoApplicationIT {

    @Autowired
    private ModelRouter modelRouter;

    @Autowired
    private AuditLoggingAdvisor auditLoggingAdvisor;

    @Test
    void modelRouterBeanIsPresentAndRouteReturnsModel() {
        assertThat(modelRouter).isNotNull();
        assertThat(modelRouter.route()).isNotNull();
    }

    @Test
    void routedCallReturnsNonBlankContent() {
        ChatModel model = modelRouter.route();
        String content = ChatClient.builder(model)
                .defaultAdvisors(auditLoggingAdvisor)
                .build()
                .prompt()
                .user("回复一个字：好")
                .call()
                .content();
        assertThat(content).isNotBlank();
    }
}
