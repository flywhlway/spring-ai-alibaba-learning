package com.flywhl.saa.knowledgeqa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.knowledgeqa.config.KqaProperties;
import com.flywhl.saa.knowledgeqa.mapper.UserConverter;
import com.flywhl.saa.knowledgeqa.model.dto.LoginRequest;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.LoginVO;
import com.flywhl.saa.knowledgeqa.model.vo.UserVO;
import com.flywhl.saa.knowledgeqa.repository.SysUserRepository;

/**
 * AuthService 单元测试：登录校验与 JWT 签发。
 *
 * @author flywhl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private SysUserRepository userRepository;
    @Mock
    private UserConverter userConverter;
    @Mock
    private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        KqaProperties properties = new KqaProperties(
                null,
                null,
                null,
                new KqaProperties.Security(new KqaProperties.Jwt("kqa-test", Duration.ofHours(1), "test-secret-32bytes-minimum!!")));
        authService = new AuthService(
                authenticationManager, jwtEncoder, properties, userRepository, userConverter, auditLogService);
    }

    @Test
    @DisplayName("凭据正确时返回 accessToken")
    void loginSuccessReturnsToken() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        SysUser user = buildUser();
        UserVO userVO = new UserVO(1L, "admin", "管理员", "ADMIN", "IT", true, OffsetDateTime.now());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", "admin123"));
        when(userRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(user));
        when(userConverter.toVo(user)).thenReturn(userVO);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("jwt-token-abc")
                        .header("alg", "none")
                        .claim("sub", "admin")
                        .build());

        LoginVO result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("jwt-token-abc");
        assertThat(result.user().username()).isEqualTo("admin");
        verify(auditLogService).save(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("密码错误时抛出未授权异常")
    void loginFailureThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("admin", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    private static SysUser buildUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("{noop}admin123");
        user.setDisplayName("管理员");
        user.setRole("ADMIN");
        user.setDepartment("IT");
        user.setEnabled(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }
}
