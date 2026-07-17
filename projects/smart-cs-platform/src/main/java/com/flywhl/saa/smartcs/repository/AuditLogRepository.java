package com.flywhl.saa.smartcs.repository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flywhl.saa.smartcs.model.entity.AuditLog;

/**
 * audit_log JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByAction(String action, Pageable pageable);

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:action IS NULL OR a.action = :action)
              AND (:userId IS NULL OR a.userId = :userId)
              AND (:fromTime IS NULL OR a.createdAt >= :fromTime)
              AND (:toTime IS NULL OR a.createdAt <= :toTime)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(
            @Param("action") String action,
            @Param("userId") Long userId,
            @Param("fromTime") OffsetDateTime fromTime,
            @Param("toTime") OffsetDateTime toTime,
            Pageable pageable);
}
