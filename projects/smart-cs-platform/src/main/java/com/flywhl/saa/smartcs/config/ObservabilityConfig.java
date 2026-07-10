package com.flywhl.saa.smartcs.config;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 可观测性补充配置：为全部 Micrometer 指标追加 {@code project=smart-cs-platform} 公共标签，
 * 便于多项目共用同一 Prometheus/Grafana 时按来源区分。
 *
 * <p>Prometheus 导出开关、health 端点暴露、成本采集（starter
 * {@code CostTrackingObservationHandler}）均由 {@code application.yml} 的
 * {@code management.*} / {@code saa.learning.cost-tracking.*} 配置驱动，为唯一真源，
 * 此处不重复声明。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilityConfig {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> scsMetricsCommonTags() {
        return registry -> registry.config().commonTags("project", "smart-cs-platform");
    }
}
