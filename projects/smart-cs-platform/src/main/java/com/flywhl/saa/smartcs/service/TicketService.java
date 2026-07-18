package com.flywhl.saa.smartcs.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.model.TicketStatus;
import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.model.entity.CsTicketEvent;
import com.flywhl.saa.smartcs.repository.CsTicketEventRepository;
import com.flywhl.saa.smartcs.repository.CsTicketRepository;

/**
 * 工单服务：完整状态机校验（{@link #ALLOWED_TRANSITIONS}）+ ticket_event 审计。
 * 禁止客户端直写 status；所有流转经 {@link #transition}。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class TicketService {

    private static final int TICKET_NO_MAX_RETRIES = 5;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<TicketStatus, Set<TicketStatus>> map = new EnumMap<>(TicketStatus.class);
        map.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.AI_PROCESSING));
        map.put(TicketStatus.AI_PROCESSING, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.PENDING_HUMAN));
        map.put(TicketStatus.PENDING_HUMAN, EnumSet.of(TicketStatus.HUMAN_HANDLING, TicketStatus.AI_PROCESSING));
        map.put(TicketStatus.HUMAN_HANDLING, EnumSet.of(TicketStatus.RESOLVED));
        map.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED));
        map.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
        ALLOWED_TRANSITIONS = Map.copyOf(map);
    }

    private final CsTicketRepository ticketRepository;
    private final CsTicketEventRepository ticketEventRepository;
    private final TicketService self;

    public TicketService(
            CsTicketRepository ticketRepository,
            CsTicketEventRepository ticketEventRepository,
            @Lazy TicketService self) {
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.self = self;
    }

    /**
     * 创建工单。序号生成非原子，捕获 {@code ticket_no} UNIQUE 冲突后以新事务重试。
     */
    public CsTicket createTicket(String conversationId, Long customerId, String summary, String priority,
            String actor) {
        String normalizedPriority = priority == null || priority.isBlank() ? "NORMAL" : priority.toUpperCase();
        TicketService proxy = self != null ? self : this;
        DataIntegrityViolationException lastConflict = null;
        for (int attempt = 1; attempt <= TICKET_NO_MAX_RETRIES; attempt++) {
            try {
                return proxy.createTicketInNewTx(conversationId, customerId, summary, normalizedPriority, actor);
            } catch (DataIntegrityViolationException ex) {
                lastConflict = ex;
            }
        }
        throw new BizException(CommonResultCode.INTERNAL_ERROR, "工单号生成冲突，请稍后重试", lastConflict);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CsTicket createTicketInNewTx(
            String conversationId, Long customerId, String summary, String priority, String actor) {
        CsTicket ticket = new CsTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setConversationId(conversationId);
        ticket.setCustomerId(customerId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(priority);
        ticket.setSummary(summary);
        OffsetDateTime now = OffsetDateTime.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket = ticketRepository.saveAndFlush(ticket);
        recordEvent(ticket.getId(), null, TicketStatus.OPEN, actor, "创建工单：" + summary);
        return ticket;
    }

    public Optional<CsTicket> findByTicketNo(String ticketNo) {
        return ticketRepository.findByTicketNo(ticketNo);
    }

    public Optional<CsTicket> findById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    public Optional<CsTicket> findLatestByConversationId(String conversationId) {
        return ticketRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    @Transactional
    public CsTicket urgeTicket(String ticketNo, String actor) {
        CsTicket ticket = ticketRepository.findByTicketNo(ticketNo)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "工单不存在：" + ticketNo));
        recordEvent(ticket.getId(), ticket.getStatus(), ticket.getStatus(), actor, "客户催单");
        return ticket;
    }

    /**
     * 校验 {@link #ALLOWED_TRANSITIONS} 后转移状态并写 {@code cs_ticket_event}。
     */
    @Transactional
    public CsTicket transition(Long ticketId, TicketStatus to, String actor, String reason) {
        CsTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "工单不存在：" + ticketId));
        TicketStatus from = ticket.getStatus();
        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "非法状态转移: " + from + " → " + to);
        }
        ticket.setStatus(to);
        ticket.setUpdatedAt(OffsetDateTime.now());
        ticket = ticketRepository.save(ticket);
        recordEvent(ticket.getId(), from, to, actor, reason);
        return ticket;
    }

    /**
     * 创建或将会话已有工单经合法路径升至 {@link TicketStatus#PENDING_HUMAN}。
     * 无工单时：创建 OPEN → AI_PROCESSING → PENDING_HUMAN；已有工单按当前状态逐步推进。
     */
    @Transactional
    public CsTicket createOrEscalate(String conversationId, Long customerId, String reason, String actor) {
        CsTicket ticket = ticketRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId)
                .orElseGet(() -> createTicket(conversationId, customerId, reason, "HIGH", actor));
        return escalateToPendingHuman(ticket, actor, reason);
    }

    /**
     * 按 conversationId（threadId）将会话工单升至 {@link TicketStatus#PENDING_HUMAN}。
     */
    @Transactional
    public CsTicket transitionToPendingHuman(String conversationId, Long customerId, String reason, String actor) {
        return createOrEscalate(conversationId, customerId, reason, actor);
    }

    /**
     * 坐席 approve 后：{@link TicketStatus#PENDING_HUMAN} → {@link TicketStatus#HUMAN_HANDLING}。
     */
    @Transactional
    public CsTicket transitionToHumanHandling(String conversationId, Long agentId, String actor) {
        CsTicket ticket = ticketRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND,
                        "会话无关联工单：" + conversationId));
        if (ticket.getStatus() != TicketStatus.PENDING_HUMAN) {
            throw new BizException(CommonResultCode.BAD_REQUEST,
                    "工单非 PENDING_HUMAN，无法进入 HUMAN_HANDLING：" + ticket.getStatus());
        }
        ticket.setAssignedAgentId(agentId);
        ticket = ticketRepository.save(ticket);
        return transition(ticket.getId(), TicketStatus.HUMAN_HANDLING, actor, "坐席接管");
    }

    private CsTicket escalateToPendingHuman(CsTicket ticket, String actor, String reason) {
        TicketStatus status = ticket.getStatus();
        if (status == TicketStatus.PENDING_HUMAN) {
            return ticket;
        }
        if (status == TicketStatus.OPEN) {
            ticket = transition(ticket.getId(), TicketStatus.AI_PROCESSING, actor, "升级前进入 AI 处理");
            status = ticket.getStatus();
        }
        if (status == TicketStatus.AI_PROCESSING) {
            return transition(ticket.getId(), TicketStatus.PENDING_HUMAN, actor, reason);
        }
        if (status == TicketStatus.HUMAN_HANDLING) {
            return ticket;
        }
        throw new BizException(CommonResultCode.BAD_REQUEST,
                "当前状态无法升级人工：" + status);
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

    /**
     * 生成规则：SCS-yyyyMMdd-序号（当日自增 4 位）。
     * 单机内 synchronized 收窄并发窗口；跨实例仍依赖 UNIQUE + REQUIRES_NEW 重试。
     */
    private String generateTicketNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "SCS-" + datePart + "-";
        synchronized (TicketService.class) {
            long seq = ticketRepository.countByTicketNoStartingWith(prefix) + 1;
            return prefix + String.format("%04d", seq);
        }
    }
}
