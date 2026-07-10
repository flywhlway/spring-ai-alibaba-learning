package com.flywhl.saa.smartcs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.AuditLog;

/**
 * audit_log JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByAction(String action, Pageable pageable);
}
