package com.flywhl.saa.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用错误码枚举。
 *
 * <p>覆盖 0 与 1000~1999、9000~9999 分段；AI / RAG / Tool / Agent 的领域错误码
 * 由各领域模块按 {@link ResultCode} 分段规范自行扩展。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum CommonResultCode implements ResultCode {

    /** 成功 */
    SUCCESS(0, "success"),

    /** 请求参数不合法 */
    BAD_REQUEST(1000, "请求参数不合法"),

    /** 未认证 */
    UNAUTHORIZED(1001, "未认证或认证已过期"),

    /** 无权限 */
    FORBIDDEN(1002, "无访问权限"),

    /** 资源不存在 */
    NOT_FOUND(1003, "资源不存在"),

    /** 触发限流 */
    TOO_MANY_REQUESTS(1004, "请求过于频繁，请稍后重试"),

    /** 系统内部错误 */
    INTERNAL_ERROR(9000, "系统内部错误，请稍后重试");

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
