package com.flywhl.saa.fallback;

/**
 * 降级路由状态摘要（仅暴露布尔，不含内部密钥或堆栈）。
 *
 * @param fallbackActive 是否当前路由到备用模型
 * @author flywhl
 */
public record FallbackStatus(boolean fallbackActive) {
}
