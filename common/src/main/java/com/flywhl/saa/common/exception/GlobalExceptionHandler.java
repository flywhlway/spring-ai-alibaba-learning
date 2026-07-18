package com.flywhl.saa.common.exception;

import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器（SSOT）。
 *
 * <p>各应用通过组件扫描或 {@code @Import(GlobalExceptionHandler.class)} 启用，
 * 保证全仓库错误响应结构完全一致：
 * <ul>
 *   <li>业务异常 {@link BizException} → 对应错误码，HTTP 200（错误语义由 code 表达）</li>
 *   <li>参数校验异常 → {@code 1000 BAD_REQUEST}，HTTP 400</li>
 *   <li>其余未捕获异常 → {@code 9000 INTERNAL_ERROR}，HTTP 500，且不向调用方泄露堆栈细节</li>
 * </ul>
 *
 * <p>{@code AccessDeniedException} / {@code AuthorizationDeniedException} 由 sibling
 * {@link AccessDeniedExceptionHandler} 映射为 HTTP 403，不进入本类 catch-all。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：可预期，warn 级别记录，不打堆栈（有 cause 时打）。
     *
     * @param ex 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        if (ex.getCause() != null) {
            log.warn("业务异常 code={} message={}", ex.getResultCode().code(), ex.getMessage(), ex.getCause());
        } else {
            log.warn("业务异常 code={} message={}", ex.getResultCode().code(), ex.getMessage());
        }
        return Result.fail(ex.getResultCode(), ex.getMessage());
    }

    /**
     * {@code @RequestBody} 字段校验失败。
     *
     * @param ex 校验异常
     * @return 统一失败响应，message 聚合所有字段错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + messageOf(fe))
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", detail);
        return Result.fail(CommonResultCode.BAD_REQUEST, detail);
    }

    /**
     * 方法级参数（{@code @RequestParam} / {@code @PathVariable}）校验失败。
     *
     * @param ex 约束违反异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", detail);
        return Result.fail(CommonResultCode.BAD_REQUEST, detail);
    }

    /**
     * 请求体不可读（JSON 语法错误、类型不匹配等）。
     *
     * @param ex 消息不可读异常
     * @return 统一失败响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return Result.fail(CommonResultCode.BAD_REQUEST, "请求体格式不正确");
    }

    /**
     * 兜底：未预期异常。error 级别记录完整堆栈，响应不泄露内部细节。
     *
     * @param ex 未预期异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnexpected(Exception ex) {
        log.error("未预期异常", ex);
        return Result.fail(CommonResultCode.INTERNAL_ERROR);
    }

    private String messageOf(FieldError fe) {
        String msg = fe.getDefaultMessage();
        return msg == null ? "invalid" : msg;
    }
}
