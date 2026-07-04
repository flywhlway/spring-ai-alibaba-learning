package com.flywhl.saa.dynamictool.model;

/**
 * 动态计算器工具入参：{@code op} 取值 add/sub/mul/div。
 *
 * @author flywhl
 */
public record CalcRequest(String op, double a, double b) {
}
