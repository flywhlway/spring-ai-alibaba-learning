package com.flywhl.saa.knowledgeqa.admin.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.admin.service.PromptAdminService;
import com.flywhl.saa.knowledgeqa.model.dto.PromptSaveRequest;
import com.flywhl.saa.knowledgeqa.model.vo.PromptTemplateVO;

import jakarta.validation.Valid;

/**
 * 后台-Prompt 管理：模板 CRUD、版本列表、发布（推 Nacos 热更新）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
public class PromptAdminController {

    private final PromptAdminService promptAdminService;

    public PromptAdminController(PromptAdminService promptAdminService) {
        this.promptAdminService = promptAdminService;
    }

    @GetMapping
    public Result<List<PromptTemplateVO>> list(@RequestParam(required = false) String templateKey) {
        return Result.ok(promptAdminService.list(templateKey));
    }

    @PostMapping
    public Result<PromptTemplateVO> create(@Valid @RequestBody PromptSaveRequest request) {
        return Result.ok(promptAdminService.create(request));
    }

    @PostMapping("/{id}/publish")
    public Result<PromptTemplateVO> publish(@PathVariable Long id) {
        return Result.ok(promptAdminService.publish(id));
    }
}
