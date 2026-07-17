package com.flywhl.saa.smartcs.admin.service;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.smartcs.model.entity.AuditLog;
import com.flywhl.saa.smartcs.model.vo.AuditLogVO;
import com.flywhl.saa.smartcs.repository.AuditLogRepository;

/**
 * 审计日志查询服务。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public AuditQueryService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public PageResult<AuditLogVO> search(
            String action,
            Long userId,
            OffsetDateTime fromTime,
            OffsetDateTime toTime,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<AuditLog> result = auditLogRepository.search(
                blankToNull(action),
                userId,
                fromTime,
                toTime,
                pageable);
        return PageResult.of(page, size, result.getTotalElements(),
                result.getContent().stream().map(this::toVo).toList());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private AuditLogVO toVo(AuditLog entity) {
        return new AuditLogVO(
                entity.getId(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getAction(),
                entity.getTarget(),
                entity.getDetail(),
                entity.getClientIp(),
                entity.getSuccess(),
                entity.getCreatedAt());
    }
}
