package com.flywhl.saa.knowledgeqa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.knowledgeqa.model.entity.AuditLog;

/**
 * audit_log JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
