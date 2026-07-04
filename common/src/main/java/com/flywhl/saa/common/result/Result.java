package com.flywhl.saa.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应体（SSOT）。
 *
 * <p>全仓库所有 HTTP 同步接口统一返回本结构；SSE / 流式接口返回事件流，
 * 但其终止错误事件的 payload 同样复用本结构，保证前端错误处理逻辑唯一。
 *
 * <p>采用 Java 21 record 实现不可变响应体，序列化时忽略 null 字段。
 *
 * @param code    错误码，0 表示成功，见 {@link ResultCode} 分段规范
 * @param message 提示信息
 * @param data    业务数据，成功时可为 null（如无返回体的写操作）
 * @param traceId 链路追踪 ID，便于日志与可观测平台关联排查
 * @param <T>     业务数据类型
 * @author flywhl
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Result<T>(int code, String message, T data, String traceId) {

    /**
     * 成功响应（无数据）。
     *
     * @param <T> 业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（携带数据）。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CommonResultCode.SUCCESS.code(), CommonResultCode.SUCCESS.message(), data, currentTraceId());
    }

    /**
     * 失败响应（使用错误码默认信息）。
     *
     * @param resultCode 错误码
     * @param <T>        业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode, resultCode.message());
    }

    /**
     * 失败响应（覆盖提示信息）。
     *
     * @param resultCode 错误码
     * @param message    覆盖后的提示信息
     * @param <T>        业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.code(), message, null, currentTraceId());
    }

    /**
     * 是否成功。
     *
     * @return code == 0 时为 true
     */
    public boolean isSuccess() {
        return code == CommonResultCode.SUCCESS.code();
    }

    /**
     * 读取当前线程 MDC 中的 traceId（由日志/追踪组件写入）。
     *
     * @return traceId，不存在时返回 null（配合 NON_NULL 不参与序列化）
     */
    private static String currentTraceId() {
        return org.slf4j.MDC.get("traceId");
    }
}
