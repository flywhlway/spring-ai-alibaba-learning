package com.flywhl.saa.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 统一 AI Starter 配置属性（SSOT）。
 *
 * <p>前缀 {@code saa.learning}，覆盖三个企业项目共用的模型路由、默认 Advisor
 * 开关、成本估算单价等配置项。属性类采用 record（第 03 章推荐写法），
 * 紧凑构造器承担默认值兜底与基本校验。
 *
 * @param primaryModel   主模型 Bean 名（默认 {@code dashScopeChatModel}）
 * @param fallbackModel  降级模型 Bean 名，主模型调用失败时切换（默认 {@code deepSeekChatModel}）
 * @param auditEnabled   是否启用默认审计 Advisor（第 06 章 AuditLoggingAdvisor 的 Starter 化版本）
 * @param costTracking   成本采集配置
 * @author flywhl
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "saa.learning")
public record SaaLearningProperties(
        String primaryModel,
        String fallbackModel,
        boolean auditEnabled,
        CostTracking costTracking) {

    public SaaLearningProperties {
        if (primaryModel == null || primaryModel.isBlank()) {
            primaryModel = "dashScopeChatModel";
        }
        if (fallbackModel == null || fallbackModel.isBlank()) {
            fallbackModel = "deepSeekChatModel";
        }
        if (costTracking == null) {
            costTracking = new CostTracking(true, 0.0008, 0.002);
        }
    }

    /**
     * 成本采集配置。
     *
     * @param enabled                是否启用成本采集 ObservationHandler
     * @param pricePer1kInputTokens  每千输入 token 单价（元），示例值，生产应从配置中心动态获取
     * @param pricePer1kOutputTokens 每千输出 token 单价（元）
     */
    public record CostTracking(boolean enabled, double pricePer1kInputTokens, double pricePer1kOutputTokens) {
    }
}
