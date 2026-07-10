package com.flywhl.saa.smartcs.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.TicketStatus;
import com.flywhl.saa.smartcs.model.entity.CsTicket;

/**
 * cs_ticket JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface CsTicketRepository extends JpaRepository<CsTicket, Long> {

    Optional<CsTicket> findByTicketNo(String ticketNo);

    Optional<CsTicket> findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);

    Page<CsTicket> findByStatus(TicketStatus status, Pageable pageable);

    Page<CsTicket> findByCustomerId(Long customerId, Pageable pageable);

    long countByStatus(TicketStatus status);
}
