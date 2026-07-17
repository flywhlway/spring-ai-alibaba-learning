package com.flywhl.saa.smartcs.service;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.smartcs.model.entity.AuditLog;
import com.flywhl.saa.smartcs.repository.AuditLogRepository;
import com.flywhl.saa.smartcs.repository.SysUserRepository;

/**
 * 审计日志落库服务：后台操作与 AI 调用双轨审计的 DB 端。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SysUserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, SysUserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void save(Long userId, String action, String resourceType, String resourceId, Map<String, Object> detail) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> log.setUsername(user.getUsername()));
        }
        log.setAction(action);
        if (resourceType != null && !resourceType.isBlank()) {
            String target = resourceId != null && !resourceId.isBlank()
                    ? resourceType + "/" + resourceId
                    : resourceType;
            log.setTarget(target);
        }
        log.setDetail(detail);
        log.setSuccess(true);
        log.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(log);
    }
}
