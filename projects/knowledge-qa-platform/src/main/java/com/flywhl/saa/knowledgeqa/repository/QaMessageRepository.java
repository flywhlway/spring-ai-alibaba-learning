package com.flywhl.saa.knowledgeqa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
