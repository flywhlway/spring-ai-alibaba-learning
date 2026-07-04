package com.flywhl.saa.retry;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义 {@link RetryListener}：统计 open / onError 次数，供 {@code /retry/stats} 观测。
 *
 * <p>通过声明自定义 {@code RetryTemplate} Bean（见 {@link RetryConfig}）覆盖官方
 * {@code @ConditionalOnMissingBean} 默认实现，从而挂上本监听器。
 *
 * @author flywhl
 */
@Component
public class RetryAttemptCounter implements RetryListener {

    private final AtomicLong openCount = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final AtomicLong successCount = new AtomicLong();

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        openCount.incrementAndGet();
        return true;
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                                                  Throwable throwable) {
        errorCount.incrementAndGet();
    }

    @Override
    public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
        successCount.incrementAndGet();
    }

    public long openCount() {
        return openCount.get();
    }

    public long errorCount() {
        return errorCount.get();
    }

    public long successCount() {
        return successCount.get();
    }
}
