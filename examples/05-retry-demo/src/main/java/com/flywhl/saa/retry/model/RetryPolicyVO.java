package com.flywhl.saa.retry.model;

/**
 * 当前生效的重试策略摘要。
 *
 * @param maxAttempts     最大尝试次数（含首次）
 * @param initialInterval 初始退避间隔（毫秒）
 * @param multiplier      退避倍数
 * @param maxInterval     最大退避间隔（毫秒）
 * @author flywhl
 */
public record RetryPolicyVO(
        int maxAttempts,
        long initialInterval,
        int multiplier,
        long maxInterval) {
}
