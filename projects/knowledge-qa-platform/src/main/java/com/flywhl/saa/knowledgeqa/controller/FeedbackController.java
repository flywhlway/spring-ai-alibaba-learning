package com.flywhl.saa.knowledgeqa.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.model.dto.FeedbackRequest;
import com.flywhl.saa.knowledgeqa.service.FeedbackService;

import jakarta.validation.Valid;

/**
 * 答案反馈：POST /api/qa/feedback。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/qa")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback")
    public Result<Void> feedback(@Valid @RequestBody FeedbackRequest request) {
        feedbackService.save(request);
        return Result.ok();
    }
}
