package com.flywhl.saa.dynamictool;

import com.flywhl.saa.dynamictool.model.CalcRequest;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

/**
 * 运行时动态构造 {@link ToolCallback}：不在 {@code ChatClient.Builder} 级固定绑定，
 * 而是按调用方请求参数决定"这一次要不要把计算器工具带上"，体现工具集可以按场景
 * 动态挑选而非全局常驻（第 07 章 §7.2 编程式工具定义的典型用法）。
 *
 * @author flywhl
 */
@Component
public class DynamicToolFactory {

    public ToolCallback calculatorTool() {
        return FunctionToolCallback.builder("calculate", this::calculate)
                .description("执行两个数字的四则运算，op 取值 add/sub/mul/div")
                .inputType(CalcRequest.class)
                .build();
    }

    private Double calculate(CalcRequest request) {
        return switch (request.op()) {
            case "add" -> request.a() + request.b();
            case "sub" -> request.a() - request.b();
            case "mul" -> request.a() * request.b();
            case "div" -> request.a() / request.b();
            default -> throw new IllegalArgumentException("不支持的运算符：" + request.op());
        };
    }
}
