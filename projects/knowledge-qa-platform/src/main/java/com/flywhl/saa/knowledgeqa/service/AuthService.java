package com.flywhl.saa.knowledgeqa.service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.knowledgeqa.config.KqaProperties;
import com.flywhl.saa.knowledgeqa.mapper.UserConverter;
import com.flywhl.saa.knowledgeqa.model.dto.LoginRequest;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.LoginVO;
import com.flywhl.saa.knowledgeqa.model.vo.UserVO;
import com.flywhl.saa.knowledgeqa.repository.SysUserRepository;

/**
 * 认证服务：账号校验 + JWT 签发；当前用户解析。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final KqaProperties properties;
    private final SysUserRepository userRepository;
    private final UserConverter userConverter;
    private final AuditLogService auditLogService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            KqaProperties properties,
            SysUserRepository userRepository,
            UserConverter userConverter,
            AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.auditLogService = auditLogService;
    }

    public LoginVO login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception ex) {
            throw new BizException(CommonResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        SysUser user = userRepository.findByUsername(request.username())
                .filter(SysUser::getEnabled)
                .orElseThrow(() -> new BizException(CommonResultCode.UNAUTHORIZED, "用户不存在或已停用"));

        String accessToken = encodeToken(user);
        auditLogService.save(
                user.getId(),
                "LOGIN",
                "sys_user",
                String.valueOf(user.getId()),
                java.util.Map.of("username", user.getUsername(), "role", user.getRole()));
        return new LoginVO(accessToken, userConverter.toVo(user));
    }

    public UserVO me() {
        return userConverter.toVo(requireCurrentUser());
    }

    public SysUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException(CommonResultCode.UNAUTHORIZED);
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object uid = jwt.getClaim("uid");
            if (uid instanceof Number number) {
                return userRepository.findById(number.longValue())
                        .filter(SysUser::getEnabled)
                        .orElseThrow(() -> new BizException(CommonResultCode.UNAUTHORIZED));
            }
        }

        return userRepository.findByUsername(authentication.getName())
                .filter(SysUser::getEnabled)
                .orElseThrow(() -> new BizException(CommonResultCode.UNAUTHORIZED));
    }

    private String encodeToken(SysUser user) {
        KqaProperties.Jwt jwtProps = properties.security().jwt();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProps.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProps.issuer())
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
