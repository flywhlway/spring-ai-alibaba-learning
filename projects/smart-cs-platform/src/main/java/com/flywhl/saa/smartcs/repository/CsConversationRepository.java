package com.flywhl.saa.smartcs.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.CsConversation;

/**
 * cs_conversation JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface CsConversationRepository extends JpaRepository<CsConversation, Long> {

    Optional<CsConversation> findByConversationId(String conversationId);

    Page<CsConversation> findByCustomerId(Long customerId, Pageable pageable);

    Page<CsConversation> findByAssignedAgentId(Long agentId, Pageable pageable);
}
