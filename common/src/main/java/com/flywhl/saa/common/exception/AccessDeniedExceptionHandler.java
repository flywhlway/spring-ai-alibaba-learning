package com.flywhl.saa.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;

import lombok.extern.slf4j.Slf4j;

/**
 * 访问拒绝异常处理器：将 {@link AccessDeniedException}（含 {@code AuthorizationDeniedException}）
 * 映射为 HTTP 403 + {@link CommonResultCode#FORBIDDEN}。
 *
 * <p>响应契约对齐 {@link GlobalExceptionHandler}：{@code @ExceptionHandler} +
 * {@code @ResponseStatus} + {@code Result.fail}，warn 日志，<strong>不</strong>把
 * {@code ex.getMessage()} / 堆栈回传客户端。
 *
 * <p>方法级 {@code @PreAuthorize} 拒绝在 DispatcherServlet/AOP 内抛出，不经
 * {@code ExceptionTranslationFilter}；若无本 advice，会被
 * {@link GlobalExceptionHandler#handleUnexpected} 兜成 HTTP 500。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class AccessDeniedExceptionHandler {

    /**
     * 访问被拒：可预期的鉴权失败，warn 级别，固定 FORBIDDEN 文案。
     *
     * @param ex 访问拒绝异常（含 AuthorizationDeniedException 子类）
     * @return 统一失败响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException ex) {
        log.warn("访问被拒绝 type={}", ex.getClass().getSimpleName());
        return Result.fail(CommonResultCode.FORBIDDEN);
    }
}
