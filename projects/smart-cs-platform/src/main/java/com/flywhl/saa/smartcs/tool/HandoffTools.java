package com.flywhl.saa.smartcs.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.service.TicketService;

/**
 * 人工接管 Tool：由 {@code human-escalation-agent} 挂载，触发前经
 * {@code HumanInTheLoopHook.approvalOn("requestHumanHandoff", ...)} 拦截，需坐席审批后
 * 才会真正执行（禁止使用模型执行前中断钩子等教程伪 API）。执行时委托
 * {@link TicketService#createOrEscalate} 将工单流转为 {@code PENDING_HUMAN}。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class HandoffTools {

    private final TicketService ticketService;

    public HandoffTools(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Tool(description = "请求人工接管，需提供会话ID与升级原因，将工单流转为 PENDING_HUMAN 等待坐席确认")
    public String requestHumanHandoff(
            @ToolParam(description = "会话ID，取自用户消息开头 [conversationId=...] 标记") String conversationId,
            @ToolParam(description = "升级原因") String reason,
            ToolContext toolContext) {
        // approve resume 时调用方为 AGENT/ADMIN；发起升级可为 CUSTOMER/AGENT
        if (ToolSecuritySupport.requireRole(toolContext, "CUSTOMER", "AGENT", "ADMIN") == null) {
            return "权限不足：无权请求人工接管";
        }
        String resolvedConversationId = ToolSecuritySupport.conversationIdOf(toolContext, conversationId);
        Long customerId = ToolSecuritySupport.userIdOf(toolContext);
        String actor = ToolSecuritySupport.roleOf(toolContext);
        CsTicket ticket = ticketService.createOrEscalate(resolvedConversationId, customerId, reason, actor);
        return "已提交人工接管请求，工单号：" + ticket.getTicketNo() + "，状态：" + ticket.getStatus() + "，等待坐席确认";
    }
}
