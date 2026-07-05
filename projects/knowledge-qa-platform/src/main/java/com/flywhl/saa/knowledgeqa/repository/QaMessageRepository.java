package com.flywhl.saa.knowledgeqa.repository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.knowledgeqa.model.entity.QaMessage;

/**
 * qa_message JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface QaMessageRepository extends JpaRepository<QaMessage, Long> {

    Page<QaMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId, Pageable pageable);

    long countByConversationId(String conversationId);

    @Modifying
    @Transactional
    void deleteByConversationId(String conversationId);

    long countByRoleAndCreatedAtAfter(String role, OffsetDateTime after);

    java.util.List<QaMessage> findByRoleAndCreatedAtAfter(String role, OffsetDateTime after);
}
