package com.flywhl.saa.starter.routing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@link FallbackModelRouter} 单元测试。
 *
 * @author flywhl
 */
class FallbackModelRouterTest {

    private final ChatModel primary = mock(ChatModel.class);
    private final ChatModel fallback = mock(ChatModel.class);

    @Test
    @DisplayName("初始状态下路由到主模型")
    void shouldRouteToPrimaryByDefault() {
        FallbackModelRouter router = new FallbackModelRouter(primary, fallback);
        assertThat(router.route()).isSameAs(primary);
        assertThat(router.isFallbackActive()).isFalse();
    }

    @Test
    @DisplayName("连续失败达到阈值后切换到备用模型")
    void shouldSwitchToFallbackAfterThresholdFailures() {
        FallbackModelRouter router = new FallbackModelRouter(primary, fallback, 3, Duration.ofMinutes(1));

        router.reportFailure(primary, new RuntimeException("timeout-1"));
        assertThat(router.route()).isSameAs(primary);

        router.reportFailure(primary, new RuntimeException("timeout-2"));
        assertThat(router.route()).isSameAs(primary);

        router.reportFailure(primary, new RuntimeException("timeout-3"));
        assertThat(router.route()).isSameAs(fallback);
        assertThat(router.isFallbackActive()).isTrue();
    }

    @Test
    @DisplayName("冷却期结束后自动尝试恢复主模型")
    void shouldRecoverToPrimaryAfterCooldown() {
        FallbackModelRouter router = new FallbackModelRouter(primary, fallback, 1, Duration.ofMillis(1));

        router.reportFailure(primary, new RuntimeException("timeout"));
        assertThat(router.route()).isSameAs(fallback);

        // 等待冷却窗口过期
        await(10);
        assertThat(router.route()).isSameAs(primary);
        assertThat(router.isFallbackActive()).isFalse();
    }

    @Test
    @DisplayName("备用模型自身失败不触发进一步降级")
    void fallbackFailureShouldNotCascade() {
        FallbackModelRouter router = new FallbackModelRouter(primary, fallback, 1, Duration.ofMinutes(1));
        router.reportFailure(primary, new RuntimeException("timeout"));
        assertThat(router.route()).isSameAs(fallback);

        // 备用模型失败不应抛异常也不应改变路由状态
        router.reportFailure(fallback, new RuntimeException("fallback also failing"));
        assertThat(router.route()).isSameAs(fallback);
    }

    private void await(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
