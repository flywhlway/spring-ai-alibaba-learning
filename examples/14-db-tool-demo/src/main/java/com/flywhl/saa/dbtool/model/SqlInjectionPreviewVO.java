package com.flywhl.saa.dbtool.model;

import java.util.List;

/**
 * SQL 注入风险教学对比视图：同时展示"危险写法会拼出什么 SQL 文本"与
 * "安全写法实际执行后的真实结果"，两者对同一个用户输入并列呈现。
 *
 * <p>{@code unsafeSqlPreview} 只是一段展示用的字符串，本 Demo 全程不会把它
 * 交给任何 JDBC 执行入口——见 {@link com.flywhl.saa.dbtool.tool.SqlInjectionRiskDemo}。
 *
 * @param userInput        原始用户输入（未经任何转义）
 * @param unsafeSqlPreview 若采用字符串拼接会拼出的 SQL 文本（仅展示，不执行）
 * @param injectionDetected 是否命中常见注入特征
 * @param warning          风险提示文案
 * @param safeQueryResult  同一输入经参数化查询实际执行后的真实结果（安全）
 * @author flywhl
 */
public record SqlInjectionPreviewVO(String userInput, String unsafeSqlPreview, boolean injectionDetected,
                                     String warning, List<Product> safeQueryResult) {
}
