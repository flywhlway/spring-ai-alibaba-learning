package com.flywhl.saa.starter.autoconfigure;

import com.flywhl.saa.common.exception.AccessDeniedExceptionHandler;
import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;
import com.flywhl.saa.starter.metrics.CostRecorder;
import com.flywhl.saa.starter.metrics.CostTrackingObservationHandler;
import com.flywhl.saa.starter.metrics.LoggingCostRecorder;
import com.flywhl.saa.starter.routing.FallbackModelRouter;
import com.flywhl.saa.starter.routing.ModelRouter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 统一 AI Starter 自动装配入口（第 03 章自动装配规范的仓库内落地范例）。
 *
 * <p>装配产出：
 * <ul>
 *   <li>{@link ModelRouter}：主备模型降级路由，仅当容器中同时存在主/备两个具名
 *       {@link ChatModel} Bean 时才装配（{@code @ConditionalOnBean}，见第 04 章多模型 Demo）；</li>
 *   <li>{@link AuditLoggingAdvisor}：默认审计 Advisor，受 {@code saa.learning.audit-enabled} 开关控制；</li>
 *   <li>{@link CostTrackingObservationHandler}：成本采集，受 {@code saa.learning.cost-tracking.enabled} 开关控制；</li>
 *   <li>{@link GlobalExceptionHandler}：复用 common 模块的统一异常处理器；</li>
 *   <li>{@link AccessDeniedExceptionHandler}：classpath 存在 Spring Security 时条件装配，
 *       AccessDenied → HTTP 403（字符串 {@code @ConditionalOnClass}，避免 starter 编译依赖 security）。</li>
 * </ul>
 * 所有 Bean 均遵循 {@code @ConditionalOnMissingBean}，业务方可随时用自定义实现覆盖默认行为。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(name = {
        "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration",
        "org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration"
})
@EnableConfigurationProperties(SaaLearningProperties.class)
@Import(GlobalExceptionHandler.class)
public class SaaLearningAutoConfiguration {

    /**
     * 仅当应用 classpath 含 Spring Security 时导入 AccessDenied→403 handler。
     * 使用 {@code name=} 字符串形式，starter 模块本身无需声明 security 依赖。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.security.access.AccessDeniedException")
    @Import(AccessDeniedExceptionHandler.class)
    static class AccessDeniedExceptionHandlerConfiguration {
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = {"dashScopeChatModel", "deepSeekChatModel"})
    public ModelRouter modelRouter(SaaLearningProperties properties,
                                    org.springframework.context.ApplicationContext applicationContext) {
        ChatModel primary = applicationContext.getBean(properties.primaryModel(), ChatModel.class);
        ChatModel fallback = applicationContext.getBean(properties.fallbackModel(), ChatModel.class);
        return new FallbackModelRouter(primary, fallback);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "saa.learning", name = "audit-enabled", havingValue = "true", matchIfMissing = true)
    public AuditLoggingAdvisor auditLoggingAdvisor() {
        return new AuditLoggingAdvisor();
    }

    @Bean
    @ConditionalOnMissingBean
    public CostRecorder costRecorder() {
        return new LoggingCostRecorder();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "saa.learning.cost-tracking", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CostTrackingObservationHandler costTrackingObservationHandler(SaaLearningProperties properties,
                                                                          CostRecorder costRecorder) {
        return new CostTrackingObservationHandler(properties.costTracking(), costRecorder);
    }
}
