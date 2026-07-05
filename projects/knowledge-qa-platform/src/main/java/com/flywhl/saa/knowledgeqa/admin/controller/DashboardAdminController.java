package com.flywhl.saa.knowledgeqa.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.admin.service.DashboardStatsService;
import com.flywhl.saa.knowledgeqa.model.vo.DashboardStatsVO;

/**
 * 后台-运营看板统计。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardAdminController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardAdminController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(dashboardStatsService.stats(days));
    }
}
