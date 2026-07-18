package com.flywhl.saa.smartcs.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.service.TicketService;

import java.util.Optional;

/**
 * 工单操作 Tool：建单/查单/催单，委托 {@link TicketService}（Wave 3 最小实现，完整状态机
 * 见 Wave 5 06-05）。供 {@code ticket-agent}/{@code aftersales-agent}/{@code techsupport-agent}
 * 挂载。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class TicketTools {

    private final TicketService ticketService;

    public TicketTools(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Tool(description = "创建工单，需提供会话ID与问题摘要，可选优先级（NORMAL/HIGH/URGENT）")
    public String createTicket(
            @ToolParam(description = "会话ID，取自用户消息开头 [conversationId=...] 标记") String conversationId,
            @ToolParam(description = "问题摘要") String summary,
            @ToolParam(description = "优先级，NORMAL/HIGH/URGENT，可为空", required = false) String priority,
            ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "CUSTOMER", "AGENT", "ADMIN") == null) {
            return "权限不足：无法创建工单";
        }
        Long customerId = ToolSecuritySupport.userIdOf(toolContext);
        String actor = ToolSecuritySupport.roleOf(toolContext);
        CsTicket ticket = ticketService.createTicket(conversationId, customerId, summary, priority, actor);
        return "工单创建成功，工单号：" + ticket.getTicketNo() + "，状态：" + ticket.getStatus();
    }

    @Tool(description = "按工单号查询工单当前状态与摘要")
    public String queryTicketByNo(
            @ToolParam(description = "工单号") String ticketNo,
            ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "CUSTOMER", "AGENT", "ADMIN") == null) {
            return "权限不足：无法查询工单";
        }
        Optional<CsTicket> found = ticketService.findByTicketNo(ticketNo);
        if (found.isEmpty()) {
            return "未查询到工单：" + ticketNo;
        }
        CsTicket ticket = found.get();
        if (!canAccessTicket(ticket, toolContext)) {
            return "权限不足：无权查询该工单";
        }
        return "工单号：" + ticket.getTicketNo() + "，状态：" + ticket.getStatus() + "，摘要：" + ticket.getSummary();
    }

    @Tool(description = "催单：为已存在工单追加一条催促记录")
    public String urgeTicket(
            @ToolParam(description = "工单号") String ticketNo,
            ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "CUSTOMER", "AGENT", "ADMIN") == null) {
            return "权限不足：无法催单";
        }
        Optional<CsTicket> found = ticketService.findByTicketNo(ticketNo);
        if (found.isEmpty()) {
            return "未查询到工单：" + ticketNo;
        }
        if (!canAccessTicket(found.get(), toolContext)) {
            return "权限不足：无权催办该工单";
        }
        String actor = ToolSecuritySupport.roleOf(toolContext);
        CsTicket ticket = ticketService.urgeTicket(ticketNo, actor);
        return "已记录催单，工单号：" + ticket.getTicketNo() + "，当前状态：" + ticket.getStatus();
    }

    /** CUSTOMER 仅可操作本人工单；AGENT/ADMIN 放行。 */
    private static boolean canAccessTicket(CsTicket ticket, ToolContext toolContext) {
        String role = ToolSecuritySupport.roleOf(toolContext);
        if (!"CUSTOMER".equals(role)) {
            return true;
        }
        Long userId = ToolSecuritySupport.userIdOf(toolContext);
        return userId != null && userId.equals(ticket.getCustomerId());
    }
}
