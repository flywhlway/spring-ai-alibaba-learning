package com.flywhl.saa.starter.metrics;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认成本记录实现：结构化日志输出，便于日志采集链路（ELK/Loki）二次聚合。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Slf4j
public class LoggingCostRecorder implements CostRecorder {

    @Override
    public void record(String model, int promptTokens, int completionTokens, double estimatedCost) {
        log.info("[ai-cost] model={} promptTokens={} completionTokens={} estimatedCost={}",
                model, promptTokens, completionTokens, String.format("%.6f", estimatedCost));
    }
}
