package com.flywhl.saa.starter.routing;

import org.springframework.ai.chat.model.ChatModel;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 主备降级路由实现：默认使用主模型，主模型连续失败达到阈值后，
 * 在冷却时间窗口内自动切换到备用模型，冷却结束后尝试恢复使用主模型。
 *
 * <p>这是一个朴素但生产可用的熔断降级实现（对标 Spring Cloud Circuit Breaker
 * 的简化版语义），复杂场景可替换为 Resilience4j 等专业熔断库，
 * 但业务代码只依赖 {@link ModelRouter} 接口，替换实现不影响调用方代码。
 *
 * @author flywhl
 * @since 1.0.0
 */
public class FallbackModelRouter implements ModelRouter {

    private final ChatModel primary;
    private final ChatModel fallback;
    private final int failureThreshold;
    private final Duration cooldown;

    private final AtomicReference<State> state = new AtomicReference<>(State.initial());

    public FallbackModelRouter(ChatModel primary, ChatModel fallback) {
        this(primary, fallback, 3, Duration.ofMinutes(1));
    }

    public FallbackModelRouter(ChatModel primary, ChatModel fallback, int failureThreshold, Duration cooldown) {
        this.primary = primary;
        this.fallback = fallback;
        this.failureThreshold = failureThreshold;
        this.cooldown = cooldown;
    }

    @Override
    public ChatModel route() {
        State current = state.get();
        if (current.usingFallback() && Instant.now().isAfter(current.cooldownUntil())) {
            // 冷却期结束，尝试恢复主模型（下一次失败会立即重新触发降级）
            state.compareAndSet(current, State.initial());
            return primary;
        }
        return current.usingFallback() ? fallback : primary;
    }

    @Override
    public void reportFailure(ChatModel model, Throwable cause) {
        if (model != primary) {
            // 备用模型失败不触发进一步降级（已经是兜底），交由上层重试/告警处理
            return;
        }
        state.updateAndGet(s -> {
            int failures = s.consecutiveFailures() + 1;
            if (failures >= failureThreshold) {
                return new State(true, Instant.now().plus(cooldown), failures);
            }
            return new State(false, s.cooldownUntil(), failures);
        });
    }

    /**
     * 是否当前正处于降级状态（供监控/健康检查读取）。
     *
     * @return true 表示当前路由到备用模型
     */
    public boolean isFallbackActive() {
        return state.get().usingFallback();
    }

    private record State(boolean usingFallback, Instant cooldownUntil, int consecutiveFailures) {
        static State initial() {
            return new State(false, Instant.EPOCH, 0);
        }
    }
}
