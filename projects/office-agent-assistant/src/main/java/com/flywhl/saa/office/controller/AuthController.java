package com.flywhl.saa.office.controller;
import org.springframework.web.bind.annotation.*;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.office.model.dto.LoginRequest;
import com.flywhl.saa.office.model.vo.LoginVO;
import com.flywhl.saa.office.model.vo.UserVO;
import com.flywhl.saa.office.service.AuthService;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) { return Result.ok(authService.login(request)); }
    @GetMapping("/me")
    public Result<UserVO> me() { return Result.ok(authService.me()); }
}

