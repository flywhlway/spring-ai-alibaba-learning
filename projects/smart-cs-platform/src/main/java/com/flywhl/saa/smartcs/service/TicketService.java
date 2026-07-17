package com.flywhl.saa.smartcs.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.model.TicketStatus;
import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.model.entity.CsTicketEvent;
import com.flywhl.saa.smartcs.repository.CsTicketEventRepository;
import com.flywhl.saa.smartcs.repository.CsTicketRepository;

/**
 * 工单服务（Wave 3 最小实现，供 {@code tool.TicketTools}/{@code tool.HandoffTools} 调用）：
 * 创建、按工单号查询、催单、创建或升级为待人工工单。完整状态机校验（合法转移图、坐席
 * approve/reject 恢复）在 Wave 5（06-05）落地；本 Wave 仅保证工单可创建、可查询、可流转
 * 至 {@link TicketStatus#PENDING_HUMAN}，供 Agent 编排层与 HITL Tool 消费。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class TicketService {

    private final CsTicketRepository ticketRepository;
    private final CsTicketEventRepository ticketEventRepository;

    public TicketService(CsTicketRepository ticketRepository, CsTicketEventRepository ticketEventRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
    }

    @Transactional
    public CsTicket createTicket(String conversationId, Long customerId, String summary, String priority,
            String actor) {
        CsTicket ticket = new CsTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setConversationId(conversationId);
        ticket.setCustomerId(customerId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(priority == null || priority.isBlank() ? "NORMAL" : priority.toUpperCase());
        ticket.setSummary(summary);
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket = ticketRepository.save(ticket);
        recordEvent(ticket.getId(), null, TicketStatus.OPEN, actor, "创建工单：" + summary);
        return ticket;
    }

    public Optional<CsTicket> findByTicketNo(String ticketNo) {
        return ticketRepository.findByTicketNo(ticketNo);
    }

    @Transactional
    public CsTicket urgeTicket(String ticketNo, String actor) {
        CsTicket ticket = ticketRepository.findByTicketNo(ticketNo)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "工单不存在：" + ticketNo));
        recordEvent(ticket.getId(), ticket.getStatus(), ticket.getStatus(), actor, "客户催单");
        return ticket;
    }

    /**
     * 工单状态机转移（Wave 5 stub — RED 阶段占位，GREEN 实现 ALLOWED_TRANSITIONS）。
     */
    @Transactional
    public CsTicket transition(Long ticketId, TicketStatus to, String actor, String reason) {
        throw new UnsupportedOperationException("TicketService.transition not implemented");
    }

    /**
     * 创建或将会话已有工单流转为 {@link TicketStatus#PENDING_HUMAN}，供
     * {@code HandoffTools.requestHumanHandoff} 调用；HITL 坐席 approve 后的
     * {@code HUMAN_HANDLING}/{@code RESOLVED} 流转由 Wave 5 {@code HumanHandoffController} 实现。
     */
    @Transactional
    public CsTicket createOrEscalate(String conversationId, Long customerId, String reason, String actor) {
        CsTicket ticket = ticketRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId)
                .orElseGet(() -> createTicket(conversationId, customerId, reason, "HIGH", actor));
        TicketStatus from = ticket.getStatus();
        if (from != TicketStatus.PENDING_HUMAN) {
            ticket.setStatus(TicketStatus.PENDING_HUMAN);
            ticket.setUpdatedAt(OffsetDateTime.now());
            ticket = ticketRepository.save(ticket);
            recordEvent(ticket.getId(), from, TicketStatus.PENDING_HUMAN, actor, reason);
        }
        return ticket;
    }

    private void recordEvent(Long ticketId, TicketStatus from, TicketStatus to, String actor, String reason) {
        CsTicketEvent event = new CsTicketEvent();
        event.setTicketId(ticketId);
        event.setFromStatus(from == null ? null : from.name());
        event.setToStatus(to.name());
        event.setActor(actor);
        event.setReason(reason);
        event.setCreatedAt(OffsetDateTime.now());
        ticketEventRepository.save(event);
    }

    private String generateTicketNo() {
        return "TK" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
