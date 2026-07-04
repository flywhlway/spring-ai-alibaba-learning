package com.flywhl.saa.starter.metrics;

import com.flywhl.saa.starter.autoconfigure.SaaLearningProperties;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.observation.ChatModelObservationContext;

/**
 * 基于 {@code gen_ai.usage.*} 指标估算调用成本（第 18 章可观测体系的生产化实现）。
 *
 * <p>单价来自 {@link SaaLearningProperties.CostTracking}，生产环境建议改为从
 * Nacos 动态配置读取（第 05 章 Prompt 热更新同源思路），本实现保留了这个演进路径——
 * 只需将单价字段来源替换为配置中心监听即可，无需改动本类的观测逻辑。
 *
 * @author flywhl
 * @since 1.0.0
 */
public class CostTrackingObservationHandler implements ObservationHandler<ChatModelObservationContext> {

    private final SaaLearningProperties.CostTracking config;
    private final CostRecorder recorder;

    public CostTrackingObservationHandler(SaaLearningProperties.CostTracking config, CostRecorder recorder) {
        this.config = config;
        this.recorder = recorder;
    }

    @Override
    public void onStop(ChatModelObservationContext context) {
        if (context.getResponse() == null || context.getResponse().getMetadata() == null) {
            return;
        }
        Usage usage = context.getResponse().getMetadata().getUsage();
        if (usage == null || usage.getPromptTokens() == null) {
            return;
        }
        int promptTokens = usage.getPromptTokens();
        int completionTokens = usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens();

        double cost = (promptTokens / 1000.0) * config.pricePer1kInputTokens()
                + (completionTokens / 1000.0) * config.pricePer1kOutputTokens();

        String model = context.getRequest() != null && context.getRequest().getOptions() != null
                ? context.getRequest().getOptions().getModel()
                : "unknown";

        recorder.record(model, promptTokens, completionTokens, cost);
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ChatModelObservationContext;
    }
}
