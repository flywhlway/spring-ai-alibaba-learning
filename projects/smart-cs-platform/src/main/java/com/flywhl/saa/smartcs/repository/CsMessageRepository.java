package com.flywhl.saa.smartcs.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.smartcs.model.entity.CsMessage;

/**
 * cs_message JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface CsMessageRepository extends JpaRepository<CsMessage, Long> {

    Page<CsMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId, Pageable pageable);

    long countByConversationId(String conversationId);

    long countByRoleAndCreatedAtAfter(String role, OffsetDateTime after);

    List<CsMessage> findByRoleAndCreatedAtAfter(String role, OffsetDateTime after);

    long countByCacheHitTrueAndCreatedAtAfter(OffsetDateTime after);

    @Modifying
    @Transactional
    void deleteByConversationId(String conversationId);
}
