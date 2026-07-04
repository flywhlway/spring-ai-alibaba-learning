package com.flywhl.saa.mcpnacos.server;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

/**
 * 订单查询 MCP 工具，注册服务名 {@code order-service-mcp}。
 *
 * @author flywhl
 */
@Service
public class OrderTools {

    @McpTool(description = "根据订单号查询订单状态")
    public String getOrderStatus(
            @McpToolParam(description = "订单号", required = true) String orderId) {
        return "订单 " + orderId + " 当前状态：配送中，预计明日送达";
    }
}
