package com.flywhl.saa.mcpserver;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

/**
 * 订单查询 MCP 工具（固定演示数据）。
 *
 * <p>注解包为 Spring AI 1.1.2 实际路径 {@code org.springaicommunity.mcp.annotation}
 * （教程中的 {@code org.springframework.ai.tool.annotation.McpTool} 在 1.1.2 不存在）。
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
