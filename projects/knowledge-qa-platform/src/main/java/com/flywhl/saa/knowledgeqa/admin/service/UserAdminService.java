package com.flywhl.saa.knowledgeqa.admin.service;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.knowledgeqa.mapper.UserConverter;
import com.flywhl.saa.knowledgeqa.model.dto.UserCreateRequest;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.UserVO;
import com.flywhl.saa.knowledgeqa.repository.SysUserRepository;
import com.flywhl.saa.knowledgeqa.service.AuditLogService;
import com.flywhl.saa.knowledgeqa.service.AuthService;

/**
 * 用户管理服务：分页列表、新建用户（BCrypt）、启用/停用。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class UserAdminService {

    private final SysUserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public UserAdminService(
            SysUserRepository userRepository,
            UserConverter userConverter,
            PasswordEncoder passwordEncoder,
            AuthService authService,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    public PageResult<UserVO> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<SysUser> result = userRepository.findAll(pageable);
        return PageResult.of(page, size, result.getTotalElements(),
                result.getContent().stream().map(userConverter::toVo).toList());
    }

    @Transactional
    public UserVO create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "用户名已存在");
        }

        SysUser user = userConverter.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        OffsetDateTime now = OffsetDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        SysUser saved = userRepository.save(user);

        SysUser operator = authService.requireCurrentUser();
        auditLogService.save(
                operator.getId(),
                "CREATE_USER",
                "sys_user",
                String.valueOf(saved.getId()),
                Map.of("username", saved.getUsername(), "role", saved.getRole()));

        return userConverter.toVo(saved);
    }

    @Transactional
    public UserVO updateStatus(Long id, boolean enabled) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "用户不存在"));

        user.setEnabled(enabled);
        user.setUpdatedAt(OffsetDateTime.now());
        SysUser saved = userRepository.save(user);

        SysUser operator = authService.requireCurrentUser();
        auditLogService.save(
                operator.getId(),
                enabled ? "ENABLE_USER" : "DISABLE_USER",
                "sys_user",
                String.valueOf(id),
                Map.of("username", saved.getUsername(), "enabled", enabled));

        return userConverter.toVo(saved);
    }
}
