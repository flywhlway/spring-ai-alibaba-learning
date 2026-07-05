package com.flywhl.saa.knowledgeqa.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.knowledgeqa.model.entity.QaConversation;

/**
 * qa_conversation JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface QaConversationRepository extends JpaRepository<QaConversation, Long> {

    Optional<QaConversation> findByConversationId(String conversationId);

    Page<QaConversation> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
