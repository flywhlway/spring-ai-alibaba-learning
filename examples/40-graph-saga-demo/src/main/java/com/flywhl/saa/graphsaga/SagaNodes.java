package com.flywhl.saa.graphsaga;

import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存 Map 模拟库存与扣款，无真实支付副作用。
 *
 * @author flywhl
 */
@Component
public class SagaNodes {

    private static final String SKU = "SKU-001";

    private final ConcurrentHashMap<String, AtomicInteger> inventory = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> payments = new ConcurrentHashMap<>();

    public SagaNodes() {
        inventory.put(SKU, new AtomicInteger(10));
    }

    public Map<String, Object> deductInventory(OverAllState state) {
        String orderId = state.value("orderId", "");
        int remaining = inventory.computeIfAbsent(SKU, k -> new AtomicInteger(10)).decrementAndGet();
        return Map.of(
                "inventory", remaining,
                "message", "已扣库存 orderId=" + orderId + " remaining=" + remaining);
    }

    public Map<String, Object> chargePayment(OverAllState state) {
        String orderId = state.value("orderId", "");
        boolean forceFail = Boolean.TRUE.equals(state.value("forceFail", false));
        if (forceFail) {
            return Map.of(
                    "paymentSuccess", false,
                    "compensated", false,
                    "message", "扣款失败（forceFail），将触发补偿");
        }
        payments.put(orderId, 100);
        return Map.of(
                "paymentSuccess", true,
                "compensated", false,
                "message", "扣款成功 orderId=" + orderId);
    }

    public Map<String, Object> compensateInventory(OverAllState state) {
        String orderId = state.value("orderId", "");
        int remaining = inventory.computeIfAbsent(SKU, k -> new AtomicInteger(10)).incrementAndGet();
        return Map.of(
                "compensated", true,
                "inventory", remaining,
                "message", "已补偿库存 orderId=" + orderId + " remaining=" + remaining);
    }

    int currentInventory() {
        return inventory.getOrDefault(SKU, new AtomicInteger(0)).get();
    }
}
