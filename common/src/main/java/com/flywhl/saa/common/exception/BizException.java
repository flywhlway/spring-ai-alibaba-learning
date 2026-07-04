package com.flywhl.saa.common.exception;

import com.flywhl.saa.common.result.ResultCode;
import lombok.Getter;

/**
 * 统一业务异常（SSOT）。
 *
 * <p>全仓库业务代码中显式抛出的可预期异常均使用本类型，
 * 由 {@link GlobalExceptionHandler} 统一转换为 {@code Result} 响应。
 * 不可预期异常（NPE、IO 等）不要包装为本类型，交由兜底处理并保留原始堆栈。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Getter
public class BizException extends RuntimeException {

    /** 关联错误码 */
    private final transient ResultCode resultCode;

    /**
     * 使用错误码默认信息构造。
     *
     * @param resultCode 错误码
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.message());
        this.resultCode = resultCode;
    }

    /**
     * 覆盖提示信息构造。
     *
     * @param resultCode 错误码
     * @param message    覆盖后的提示信息
     */
    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 携带根因构造（保留底层异常堆栈，便于日志排查）。
     *
     * @param resultCode 错误码
     * @param message    覆盖后的提示信息
     * @param cause      根因
     */
    public BizException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }
}
