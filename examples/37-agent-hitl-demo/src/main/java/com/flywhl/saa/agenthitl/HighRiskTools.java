package com.flywhl.saa.agenthitl;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 高风险工具：execute_payment 执行前由 HumanInTheLoopHook 拦截审批。
 *
 * @author flywhl
 */
@Component
public class HighRiskTools {

    @Tool(description = "执行支付操作，扣款并完成订单。调用前必须获得人工确认。")
    public String execute_payment(
            @ToolParam(description = "支付金额，单位元，如 99.00") String amount,
            @ToolParam(description = "收款账户或商户名") String account) {
        return "支付成功：向 " + account + " 支付 " + amount + " 元";
    }
}
