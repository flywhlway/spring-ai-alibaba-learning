package com.flywhl.saa.office.admin.controller;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.office.admin.service.PromptAdminService;
import com.flywhl.saa.office.model.dto.PromptSaveRequest;
import com.flywhl.saa.office.model.vo.PromptTemplateVO;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
public class PromptAdminController {
    private final PromptAdminService promptAdminService;
    public PromptAdminController(PromptAdminService promptAdminService) { this.promptAdminService = promptAdminService; }
    @GetMapping
    public Result<List<PromptTemplateVO>> list(@RequestParam(required = false) String templateKey) {
        return Result.ok(promptAdminService.list(templateKey));
    }
    @PostMapping
    public Result<PromptTemplateVO> create(@Valid @RequestBody PromptSaveRequest request) {
        return Result.ok(promptAdminService.create(request));
    }
    @PostMapping("/{id}/publish")
    public Result<PromptTemplateVO> publish(@PathVariable Long id) { return Result.ok(promptAdminService.publish(id)); }
}

