package com.flywhl.saa.starter.advisor;

import org.springframework.core.Ordered;

/**
 * 全仓库 Advisor 顺序号集中管理（SSOT）。
 *
 * <p>第 06 章强调"栈"语义——order 越小，请求方向越先执行、响应方向越后执行。
 * 本类把顺序号常量化，避免各工程各自定义魔法数字导致顺序冲突或语义不清。
 *
 * @author flywhl
 * @since 1.0.0
 */
public final class AdvisorOrder {

    /** 审计日志：全链路最外层，最先记录请求意图、最后记录最终响应 */
    public static final int AUDIT = Ordered.HIGHEST_PRECEDENCE + 100;

    /** 安全防护（敏感词/内容安全）：尽量靠前，让请求有机会在消耗模型配额前被拦截 */
    public static final int SAFETY = Ordered.HIGHEST_PRECEDENCE + 200;

    /** 会话记忆：官方为 Memory 类 Advisor 预留的顺序区间起点，见 Advisor 官方文档 */
    public static final int MEMORY = Ordered.HIGHEST_PRECEDENCE + 1000;

    /** 检索增强：晚于记忆（可利用对话历史做查询改写），早于日志 */
    public static final int RETRIEVAL = Ordered.HIGHEST_PRECEDENCE + 2000;

    /** 框架内置日志：默认基线，业务自定义 Advisor 一般不与此值冲突 */
    public static final int LOGGER = 0;

    private AdvisorOrder() {
    }
}
