package com.flywhl.saa.knowledgeqa.model.vo;

import java.time.OffsetDateTime;

/**
 * 用户视图 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record UserVO(
        Long id,
        String username,
        String displayName,
        String role,
        String department,
        Boolean enabled,
        OffsetDateTime createdAt) {
}
