package com.flywhl.saa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;

/**
 * {@link AccessDeniedExceptionHandler} 单元测试：AccessDenied / AuthorizationDenied → FORBIDDEN。
 *
 * @author flywhl
 */
class AccessDeniedExceptionHandlerTest {

    private final AccessDeniedExceptionHandler handler = new AccessDeniedExceptionHandler();

    @Test
    @DisplayName("AccessDeniedException → code=1002 FORBIDDEN，不回传异常 message")
    void accessDeniedShouldReturnForbidden() {
        Result<Void> result = handler.handleAccessDenied(new AccessDeniedException("secret internals"));

        assertThat(result.code()).isEqualTo(CommonResultCode.FORBIDDEN.code());
        assertThat(result.message()).isEqualTo(CommonResultCode.FORBIDDEN.message());
        assertThat(result.message()).doesNotContain("secret internals");
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("AuthorizationDeniedException（@PreAuthorize 子类）命中同一 handler")
    void authorizationDeniedShouldReturnForbidden() {
        Result<Void> result = handler.handleAccessDenied(new AuthorizationDeniedException("Access Denied"));

        assertThat(result.code()).isEqualTo(1002);
        assertThat(result.message()).isEqualTo("无访问权限");
    }
}
