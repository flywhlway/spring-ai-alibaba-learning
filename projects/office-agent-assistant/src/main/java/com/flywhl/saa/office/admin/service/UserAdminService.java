package com.flywhl.saa.office.admin.service;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.office.mapper.UserConverter;
import com.flywhl.saa.office.model.dto.UserCreateRequest;
import com.flywhl.saa.office.model.entity.SysUser;
import com.flywhl.saa.office.model.vo.UserVO;
import com.flywhl.saa.office.repository.SysUserRepository;
import com.flywhl.saa.office.service.AuthService;
@Service
public class UserAdminService {
    private final SysUserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    public UserAdminService(SysUserRepository userRepository, UserConverter userConverter,
            PasswordEncoder passwordEncoder, AuthService authService) {
        this.userRepository = userRepository; this.userConverter = userConverter;
        this.passwordEncoder = passwordEncoder; this.authService = authService;
    }
    public PageResult<UserVO> list(int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        var result = userRepository.findAll(pageable);
        return PageResult.of(page, size, result.getTotalElements(), result.getContent().stream().map(userConverter::toVo).toList());
    }
    @Transactional
    public UserVO create(UserCreateRequest request) {
        authService.requireCurrentUser();
        if (userRepository.existsByUsername(request.username())) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "用户名已存在");
        }
        SysUser user = userConverter.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now); user.setUpdatedAt(now);
        return userConverter.toVo(userRepository.save(user));
    }
    @Transactional
    public UserVO updateStatus(Long id, boolean enabled) {
        authService.requireCurrentUser();
        SysUser user = userRepository.findById(id).orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "用户不存在"));
        user.setEnabled(enabled); user.setUpdatedAt(LocalDateTime.now());
        return userConverter.toVo(userRepository.save(user));
    }
}

