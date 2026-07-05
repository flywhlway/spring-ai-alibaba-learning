package com.flywhl.saa.knowledgeqa.model.vo;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 审计日志视图 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record AuditLogVO(
        Long id,
        Long userId,
        String username,
        String action,
        String target,
        Map<String, Object> detail,
        String clientIp,
        Boolean success,
        OffsetDateTime createdAt) {
}
