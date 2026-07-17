package com.flywhl.saa.smartcs.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.admin.service.FaqAdminService;
import com.flywhl.saa.smartcs.model.dto.FaqSaveRequest;
import com.flywhl.saa.smartcs.model.vo.FaqArticleVO;

import jakarta.validation.Valid;

/**
 * 后台-FAQ 管理：列表/详情/新建（触发 ETL）/手动 reindex。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/faq")
@PreAuthorize("hasRole('ADMIN')")
public class FaqAdminController {

    private final FaqAdminService faqAdminService;

    public FaqAdminController(FaqAdminService faqAdminService) {
        this.faqAdminService = faqAdminService;
    }

    @GetMapping
    public Result<PageResult<FaqArticleVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(faqAdminService.list(category, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<FaqArticleVO> get(@PathVariable Long id) {
        return Result.ok(faqAdminService.get(id));
    }

    @PostMapping
    public Result<FaqArticleVO> create(@Valid @RequestBody FaqSaveRequest request) {
        return Result.ok(faqAdminService.create(request));
    }

    @PostMapping("/{id}/reindex")
    public Result<FaqArticleVO> reindex(@PathVariable Long id) {
        return Result.ok(faqAdminService.reindex(id));
    }
}
