package com.flywhl.saa.smartcs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.mapper.TicketConverter;
import com.flywhl.saa.smartcs.model.dto.TicketCreateRequest;
import com.flywhl.saa.smartcs.model.dto.TicketTransitionRequest;
import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.TicketVO;
import com.flywhl.saa.smartcs.service.AuthService;
import com.flywhl.saa.smartcs.service.TicketService;

import jakarta.validation.Valid;

/**
 * 工单 REST：创建 / 按工单号查询 / 状态机转移（AGENT/ADMIN）。
 * 禁止客户端直写 status 字段绕过状态机。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketConverter ticketConverter;
    private final AuthService authService;

    public TicketController(TicketService ticketService, TicketConverter ticketConverter, AuthService authService) {
        this.ticketService = ticketService;
        this.ticketConverter = ticketConverter;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public Result<TicketVO> create(@Valid @RequestBody TicketCreateRequest request) {
        SysUser user = authService.requireCurrentUser();
        CsTicket ticket = ticketService.createTicket(
                request.conversationId(),
                user.getId(),
                request.summary(),
                request.priority(),
                user.getRole().name());
        return Result.ok(ticketConverter.toVo(ticket));
    }

    @GetMapping("/{no}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public Result<TicketVO> getByNo(@PathVariable("no") String ticketNo) {
        CsTicket ticket = ticketService.findByTicketNo(ticketNo)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "工单不存在：" + ticketNo));
        return Result.ok(ticketConverter.toVo(ticket));
    }

    @PatchMapping("/{id}/transition")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public Result<TicketVO> transition(
            @PathVariable Long id,
            @Valid @RequestBody TicketTransitionRequest request) {
        SysUser user = authService.requireCurrentUser();
        String reason = request.reason() == null || request.reason().isBlank()
                ? "坐席状态转移"
                : request.reason();
        CsTicket ticket = ticketService.transition(id, request.to(), user.getRole().name(), reason);
        return Result.ok(ticketConverter.toVo(ticket));
    }
}
