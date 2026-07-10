package com.flywhl.saa.smartcs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.model.dto.LoginRequest;
import com.flywhl.saa.smartcs.model.vo.LoginVO;
import com.flywhl.saa.smartcs.model.vo.UserVO;
import com.flywhl.saa.smartcs.service.AuthService;

import jakarta.validation.Valid;

/**
 * 认证入口：{@code POST /api/auth/login} 签发 JWT，{@code GET /api/auth/me} 返回当前用户。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.me());
    }
}
