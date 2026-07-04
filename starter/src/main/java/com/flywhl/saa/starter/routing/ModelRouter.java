package com.flywhl.saa.starter.routing;

import org.springframework.ai.chat.model.ChatModel;

/**
 * 多模型路由抽象（第 20 章企业实践"多模型路由/降级/容灾"的可复用实现）。
 *
 * <p>业务代码面向本接口编程，不感知当前请求实际落在哪个 {@link ChatModel} 实现上；
 * 路由策略（主备切换、成本优先、健康度探测）由具体实现决定，符合本教程反复出现的
 * "面向接口、策略与实现分离"设计原则（见第 04/08/11 章 Facade 模式讨论）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface ModelRouter {

    /**
     * 选择本次调用应使用的模型。
     *
     * @return 路由决策后的 {@link ChatModel}
     */
    ChatModel route();

    /**
     * 上报一次调用失败，供实现类更新健康度/熔断状态。
     *
     * @param model 发生失败的模型
     * @param cause 失败原因
     */
    void reportFailure(ChatModel model, Throwable cause);
}
