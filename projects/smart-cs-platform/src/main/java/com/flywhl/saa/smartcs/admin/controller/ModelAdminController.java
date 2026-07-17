package com.flywhl.saa.smartcs.admin.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.admin.service.ModelAdminService;
import com.flywhl.saa.smartcs.model.dto.ModelProfileSaveRequest;
import com.flywhl.saa.smartcs.model.vo.ModelProfileVO;

import jakarta.validation.Valid;

/**
 * 后台-模型配置 CRUD + Nacos 发布（{@code scs.model.profiles}）。
 *
 * <p>路径同时提供 {@code /api/admin/models}（计划契约）与 {@code /api/admin/model-profiles}
 * （Wave 0 {@code api.http} 契约）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping({"/api/admin/models", "/api/admin/model-profiles"})
@PreAuthorize("hasRole('ADMIN')")
public class ModelAdminController {

    private final ModelAdminService modelAdminService;

    public ModelAdminController(ModelAdminService modelAdminService) {
        this.modelAdminService = modelAdminService;
    }

    @GetMapping
    public Result<List<ModelProfileVO>> list(@RequestParam(required = false) String scene) {
        return Result.ok(modelAdminService.list(scene));
    }

    @GetMapping("/{id}")
    public Result<ModelProfileVO> get(@PathVariable Long id) {
        return Result.ok(modelAdminService.get(id));
    }

    @PostMapping
    public Result<ModelProfileVO> create(@Valid @RequestBody ModelProfileSaveRequest request) {
        return Result.ok(modelAdminService.create(request));
    }

    @PutMapping("/{id}")
    public Result<ModelProfileVO> update(
            @PathVariable Long id, @Valid @RequestBody ModelProfileSaveRequest request) {
        return Result.ok(modelAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        modelAdminService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/publish")
    public Result<ModelProfileVO> publish(@PathVariable Long id) {
        return Result.ok(modelAdminService.publish(id));
    }
}
