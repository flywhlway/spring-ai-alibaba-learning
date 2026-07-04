package com.flywhl.saa.starter.metrics;

/**
 * 成本记录扩展点。
 *
 * <p>默认实现（{@link LoggingCostRecorder}）仅打日志；生产环境可实现本接口
 * 将成本明细写入数据库/消息队列，供第 20 章成本看板消费。
 * 应用只需注册自己的 {@link CostRecorder} Bean，按第 03 章
 * {@code @ConditionalOnMissingBean} 规则会自动替代默认实现。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface CostRecorder {

    /**
     * 记录一次模型调用的成本。
     *
     * @param model            模型名（如 qwen-plus）
     * @param promptTokens     输入 token 数
     * @param completionTokens 输出 token 数
     * @param estimatedCost    估算成本（元）
     */
    void record(String model, int promptTokens, int completionTokens, double estimatedCost);
}
