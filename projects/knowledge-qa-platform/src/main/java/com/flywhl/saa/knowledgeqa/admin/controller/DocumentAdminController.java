package com.flywhl.saa.knowledgeqa.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.admin.service.DocumentAdminService;
import com.flywhl.saa.knowledgeqa.model.dto.DocumentUploadRequest;
import com.flywhl.saa.knowledgeqa.model.vo.DocumentVO;

/**
 * 后台-知识管理：文档上传（MinIO）/ 列表 / 删除 / 触发重建索引。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/documents")
@PreAuthorize("hasRole('ADMIN')")
public class DocumentAdminController {

    private final DocumentAdminService documentAdminService;

    public DocumentAdminController(DocumentAdminService documentAdminService) {
        this.documentAdminService = documentAdminService;
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DocumentVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("category") String category) {
        DocumentUploadRequest request = new DocumentUploadRequest(title, category);
        return Result.ok(documentAdminService.upload(file, request));
    }

    @GetMapping
    public Result<PageResult<DocumentVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(documentAdminService.list(status, category, page, size));
    }

    @PostMapping("/{id}/reindex")
    public Result<DocumentVO> reindex(@PathVariable Long id) {
        return Result.ok(documentAdminService.reindex(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentAdminService.delete(id);
        return Result.ok();
    }
}
