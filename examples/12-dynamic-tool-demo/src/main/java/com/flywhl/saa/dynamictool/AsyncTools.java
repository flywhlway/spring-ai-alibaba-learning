package com.flywhl.saa.dynamictool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 异步 Tool 演示。
 *
 * <p>{@code MethodToolCallback} 反射调用工具方法后同步取其返回值做 JSON 序列化，
 * 并不会为你 {@code await} 一个 {@link CompletableFuture}——因此工具方法本身仍需返回
 * 可直接序列化的类型；这里用 {@link CompletableFuture#supplyAsync} 把慢查询派发到独立线程池
 * 执行并施加超时（{@code orTimeout}），再 {@code join()} 取结果，既避免了调用方线程被慢 IO
 * 独占，也为慢查询提供了统一超时兜底，这是工具方法内部做"异步编排"的典型写法。
 *
 * @author flywhl
 */
@Component
public class AsyncTools {

    @Tool(description = "异步查询商品库存（内部经独立线程池 + 超时控制，避免慢查询拖垮调用线程）")
    public String queryStockAsync(@ToolParam(description = "商品 SKU，如 SKU-1001") String sku) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(300); // 模拟慢查询（如跨机房库存服务调用）
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            int stock = 100 + Math.floorMod(sku.hashCode(), 50);
            return "SKU " + sku + " 当前库存：" + stock;
        });
        return future.orTimeout(2, TimeUnit.SECONDS).join();
    }
}
