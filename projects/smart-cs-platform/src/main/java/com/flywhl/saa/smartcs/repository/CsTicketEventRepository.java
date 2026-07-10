package com.flywhl.saa.smartcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.CsTicketEvent;

/**
 * cs_ticket_event JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface CsTicketEventRepository extends JpaRepository<CsTicketEvent, Long> {

    List<CsTicketEvent> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
