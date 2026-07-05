package com.flywhl.saa.knowledgeqa.admin.controller;

import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.admin.service.AuditQueryService;
import com.flywhl.saa.knowledgeqa.model.vo.AuditLogVO;

/**
 * 后台-审计日志查询。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/audits")
@PreAuthorize("hasRole('ADMIN')")
public class AuditAdminController {

    private final AuditQueryService auditQueryService;

    public AuditAdminController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    public Result<PageResult<AuditLogVO>> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(auditQueryService.search(action, userId, from, to, page, size));
    }
}
