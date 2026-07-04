package com.flywhl.saa.retry.model;

/**
 * 自定义 RetryListener 累计统计。
 *
 * @param openCount    进入重试上下文次数
 * @param errorCount   失败回调次数（含将触发退避的错误）
 * @param successCount 成功回调次数
 * @author flywhl
 */
public record RetryStatsVO(long openCount, long errorCount, long successCount) {
}
