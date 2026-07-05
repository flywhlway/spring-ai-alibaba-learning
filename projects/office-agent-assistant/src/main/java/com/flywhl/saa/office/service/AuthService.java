package com.flywhl.saa.office.service;

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
import com.flywhl.saa.office.config.OfficeProperties;
import com.flywhl.saa.office.mapper.UserConverter;
import com.flywhl.saa.office.model.dto.LoginRequest;
import com.flywhl.saa.office.model.entity.SysUser;
import com.flywhl.saa.office.model.vo.LoginVO;
import com.flywhl.saa.office.model.vo.UserVO;
import com.flywhl.saa.office.repository.SysUserRepository;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final OfficeProperties properties;
    private final SysUserRepository userRepository;
    private final UserConverter userConverter;
    public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
            OfficeProperties properties, SysUserRepository userRepository, UserConverter userConverter) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }
    public LoginVO login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception ex) {
            throw new BizException(CommonResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        SysUser user = userRepository.findByUsername(request.username())
                .filter(SysUser::getEnabled)
                .orElseThrow(() -> new BizException(CommonResultCode.UNAUTHORIZED, "用户不存在或已停用"));
        return new LoginVO(encodeToken(user), userConverter.toVo(user));
    }
    public UserVO me() { return userConverter.toVo(requireCurrentUser()); }
    public SysUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException(CommonResultCode.UNAUTHORIZED);
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object uid = jwtAuth.getToken().getClaim("uid");
            if (uid instanceof Number number) {
                return userRepository.findById(number.longValue()).filter(SysUser::getEnabled)
                        .orElseThrow(() -> new BizException(CommonResultCode.UNAUTHORIZED));
            }
        }
        return userRepository.findByUsername(authentication.getName()).filter(SysUser::getEnabled)
                .orElseThrow(() -> new BizException(CommonResultCode.UNAUTHORIZED));
    }
    private String encodeToken(SysUser user) {
        OfficeProperties.Jwt jwtProps = properties.security().jwt();
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProps.issuer()).subject(user.getUsername())
                .claim("uid", user.getId()).claim("role", user.getRole())
                .issuedAt(now).expiresAt(now.plus(jwtProps.accessTokenTtl())).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

