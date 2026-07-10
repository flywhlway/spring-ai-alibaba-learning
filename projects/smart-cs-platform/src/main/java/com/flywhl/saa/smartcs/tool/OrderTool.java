package com.flywhl.saa.smartcs.tool;

import java.util.Locale;
import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 订单查询 Tool（演示级）：固定演示数据模拟物流状态，供 {@code business-supervisor} 的
 * {@code order-agent} 子 Agent 调用。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class OrderTool {

    private static final Map<String, String> DEMO_ORDERS = Map.of(
            "SO2024001",
            "{\"orderNo\":\"SO2024001\",\"status\":\"SHIPPED\",\"carrier\":\"顺丰速运\","
                    + "\"trackingNo\":\"SF1234567890\",\"eta\":\"2026-07-12\"}",
            "SO2024002",
            "{\"orderNo\":\"SO2024002\",\"status\":\"DELIVERED\",\"carrier\":\"中通快递\","
                    + "\"trackingNo\":\"ZT9876543210\",\"eta\":\"2026-07-08\"}",
            "SO2024003",
            "{\"orderNo\":\"SO2024003\",\"status\":\"PROCESSING\",\"carrier\":\"\","
                    + "\"trackingNo\":\"\",\"eta\":\"2026-07-15\"}");

    @Tool(description = "查询订单物流状态，入参订单号，返回 JSON 字符串（status/carrier/trackingNo/eta）")
    public String queryOrderStatus(
            @ToolParam(description = "订单号，如 SO2024001") String orderNo,
            ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "CUSTOMER", "AGENT", "ADMIN") == null) {
            return "权限不足：仅登录用户可查询订单";
        }
        if (orderNo == null || orderNo.isBlank()) {
            return "订单号不能为空";
        }
        String result = DEMO_ORDERS.get(orderNo.trim().toUpperCase(Locale.ROOT));
        return result != null ? result : "未查询到订单：" + orderNo + "（演示数据仅含 SO2024001~SO2024003）";
    }
}
