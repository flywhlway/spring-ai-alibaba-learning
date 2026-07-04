package com.flywhl.saa.common.result;

/**
 * 统一错误码契约。
 *
 * <p>全仓库所有模块的错误码均需实现本接口。错误码分段规范（SSOT，禁止各模块自行定义分段）：
 * <ul>
 *   <li>0        —— 成功</li>
 *   <li>1000~1999 —— 通用错误（参数、认证、限流等）</li>
 *   <li>2000~2999 —— AI 模型层错误（调用失败、超时、Token 超限、内容安全等）</li>
 *   <li>3000~3999 —— RAG / 向量检索层错误</li>
 *   <li>4000~4999 —— Tool / MCP 层错误</li>
 *   <li>5000~5999 —— Agent / Workflow 层错误</li>
 *   <li>9000~9999 —— 系统内部错误</li>
 * </ul>
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface ResultCode {

    /**
     * 业务错误码。
     *
     * @return 错误码数值
     */
    int code();

    /**
     * 面向调用方的默认提示信息。
     *
     * @return 提示信息
     */
    String message();
}
