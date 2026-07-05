package com.flywhl.saa.knowledgeqa.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.admin.service.UserAdminService;
import com.flywhl.saa.knowledgeqa.model.dto.UserCreateRequest;
import com.flywhl.saa.knowledgeqa.model.vo.UserVO;

import jakarta.validation.Valid;

/**
 * 后台-用户管理：用户 CRUD、角色分配、启用停用。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userAdminService.list(page, size));
    }

    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userAdminService.create(request));
    }

    @PutMapping("/{id}/status")
    public Result<UserVO> updateStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        return Result.ok(userAdminService.updateStatus(id, enabled));
    }
}
