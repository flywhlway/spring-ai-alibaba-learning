package com.flywhl.saa.graphsaga;

/**
 * Saga 执行结果（同包小 DTO，标明是否走补偿）。
 *
 * @param orderId        订单号
 * @param paymentSuccess 扣款是否成功
 * @param compensated    是否执行了库存补偿
 * @param message        流程摘要
 * @param inventory      当前库存余量（内存模拟）
 * @author flywhl
 */
public record SagaOutcome(
        String orderId,
        boolean paymentSuccess,
        boolean compensated,
        String message,
        int inventory) {
}
