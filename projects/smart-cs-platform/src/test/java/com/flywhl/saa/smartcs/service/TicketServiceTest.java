package com.flywhl.saa.smartcs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.model.TicketStatus;
import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.model.entity.CsTicketEvent;
import com.flywhl.saa.smartcs.repository.CsTicketEventRepository;
import com.flywhl.saa.smartcs.repository.CsTicketRepository;

/**
 * {@link TicketService} 状态机单测：合法转移放行、非法转移抛 {@link BizException}、
 * createOrEscalate 无工单时创建并升至 {@link TicketStatus#PENDING_HUMAN}。
 *
 * @author flywhl
 * @since 1.0.0
 */
class TicketServiceTest {

    private final CsTicketRepository ticketRepository = mock(CsTicketRepository.class);
    private final CsTicketEventRepository ticketEventRepository = mock(CsTicketEventRepository.class);
    private TicketService ticketService;
    private final AtomicLong idSeq = new AtomicLong(1);
    private final Map<Long, CsTicket> store = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        store.clear();
        idSeq.set(1);
        ticketService = new TicketService(ticketRepository, ticketEventRepository);
        when(ticketRepository.save(any(CsTicket.class))).thenAnswer(invocation -> {
            CsTicket t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(idSeq.getAndIncrement());
            }
            store.put(t.getId(), t);
            return t;
        });
        when(ticketRepository.findById(anyLong())).thenAnswer(invocation ->
                Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(ticketEventRepository.save(any(CsTicketEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        String todayPrefix = "SCS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        when(ticketRepository.countByTicketNoStartingWith(todayPrefix)).thenReturn(0L);
    }

    @Test
    void transition_allowsOpenToAiProcessing() {
        CsTicket ticket = existingTicket(1L, TicketStatus.OPEN);
        store.put(1L, ticket);

        CsTicket result = ticketService.transition(1L, TicketStatus.AI_PROCESSING, "AGENT", "开始 AI 处理");

        assertThat(result.getStatus()).isEqualTo(TicketStatus.AI_PROCESSING);
        verifyEvent(1L, TicketStatus.OPEN, TicketStatus.AI_PROCESSING, "AGENT");
    }

    @Test
    void transition_allowsAiProcessingToPendingHuman() {
        CsTicket ticket = existingTicket(2L, TicketStatus.AI_PROCESSING);
        store.put(2L, ticket);

        CsTicket result = ticketService.transition(2L, TicketStatus.PENDING_HUMAN, "SYSTEM", "升级人工");

        assertThat(result.getStatus()).isEqualTo(TicketStatus.PENDING_HUMAN);
        verifyEvent(2L, TicketStatus.AI_PROCESSING, TicketStatus.PENDING_HUMAN, "SYSTEM");
    }

    @Test
    void transition_rejectsOpenToClosed() {
        CsTicket ticket = existingTicket(3L, TicketStatus.OPEN);
        store.put(3L, ticket);

        assertThatThrownBy(() -> ticketService.transition(3L, TicketStatus.CLOSED, "AGENT", "非法关闭"))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> {
                    BizException biz = (BizException) ex;
                    assertThat(biz.getResultCode()).isEqualTo(CommonResultCode.BAD_REQUEST);
                    assertThat(biz.getMessage()).contains("非法状态转移");
                });
    }

    @Test
    void createOrEscalate_createsOpenThenPendingHuman_whenNoTicket() {
        when(ticketRepository.findFirstByConversationIdOrderByCreatedAtDesc("conv-1"))
                .thenReturn(Optional.empty());

        CsTicket result = ticketService.createOrEscalate("conv-1", 10L, "客户要求人工", "CUSTOMER");

        assertThat(result.getStatus()).isEqualTo(TicketStatus.PENDING_HUMAN);
        assertThat(result.getTicketNo()).startsWith("SCS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        assertThat(result.getConversationId()).isEqualTo("conv-1");
    }

    private CsTicket existingTicket(Long id, TicketStatus status) {
        CsTicket ticket = new CsTicket();
        ticket.setId(id);
        ticket.setTicketNo("SCS-20260717-0001");
        ticket.setConversationId("conv-" + id);
        ticket.setCustomerId(1L);
        ticket.setStatus(status);
        ticket.setPriority("NORMAL");
        ticket.setSummary("测试工单");
        ticket.setCreatedAt(OffsetDateTime.now());
        ticket.setUpdatedAt(OffsetDateTime.now());
        return ticket;
    }

    private void verifyEvent(Long ticketId, TicketStatus from, TicketStatus to, String actor) {
        ArgumentCaptor<CsTicketEvent> captor = ArgumentCaptor.forClass(CsTicketEvent.class);
        verify(ticketEventRepository).save(captor.capture());
        CsTicketEvent event = captor.getValue();
        assertThat(event.getTicketId()).isEqualTo(ticketId);
        assertThat(event.getFromStatus()).isEqualTo(from.name());
        assertThat(event.getToStatus()).isEqualTo(to.name());
        assertThat(event.getActor()).isEqualTo(actor);
    }
}
