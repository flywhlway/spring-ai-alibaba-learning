package com.flywhl.saa.office.controller;
import org.springframework.web.bind.annotation.*;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.office.model.dto.TaskGenerateRequest;
import com.flywhl.saa.office.model.vo.TaskResultVO;
import com.flywhl.saa.office.service.TaskService;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    public TaskController(TaskService taskService) { this.taskService = taskService; }
    @PostMapping("/meeting-summary")
    public Result<TaskResultVO> meetingSummary(@Valid @RequestBody TaskGenerateRequest request) {
        return Result.ok(taskService.meetingSummary(request));
    }
    @PostMapping("/daily-report")
    public Result<TaskResultVO> dailyReport(@Valid @RequestBody TaskGenerateRequest request) {
        return Result.ok(taskService.dailyReport(request));
    }
    @PostMapping("/email-draft")
    public Result<TaskResultVO> emailDraft(@Valid @RequestBody TaskGenerateRequest request) {
        return Result.ok(taskService.emailDraft(request));
    }
}

