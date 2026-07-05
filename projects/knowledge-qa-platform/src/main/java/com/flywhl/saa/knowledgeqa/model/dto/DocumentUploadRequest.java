package com.flywhl.saa.knowledgeqa.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 文档上传元数据 DTO（multipart 表单字段校验用）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record DocumentUploadRequest(
        @NotBlank String title,
        @NotBlank String category) {
}
