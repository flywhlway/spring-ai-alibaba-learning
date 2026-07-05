package com.flywhl.saa.office.admin.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.office.admin.service.UserAdminService;
import com.flywhl.saa.office.model.dto.UserCreateRequest;
import com.flywhl.saa.office.model.vo.UserVO;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminService userAdminService;
    public UserAdminController(UserAdminService userAdminService) { this.userAdminService = userAdminService; }
    @GetMapping
    public Result<PageResult<UserVO>> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userAdminService.list(page, size));
    }
    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request) { return Result.ok(userAdminService.create(request)); }
    @PutMapping("/{id}/status")
    public Result<UserVO> updateStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        return Result.ok(userAdminService.updateStatus(id, enabled));
    }
}

