package com.flywhl.saa.office.controller;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.web.bind.annotation.*;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.office.model.dto.ApprovalReviewRequest;
import com.flywhl.saa.office.model.vo.ApprovalReviewVO;
import com.flywhl.saa.office.service.ApprovalService;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService approvalService;
    public ApprovalController(ApprovalService approvalService) { this.approvalService = approvalService; }
    @PostMapping("/review")
    public Result<ApprovalReviewVO> review(@Valid @RequestBody ApprovalReviewRequest request) throws GraphRunnerException {
        return Result.ok(approvalService.review(request.approvalId()));
    }
}

