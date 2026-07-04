package com.flywhl.saa.milvus;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * 文档入库请求。
 *
 * @author flywhl
 */
public record DocumentRequest(
        @NotBlank String content,
        Map<String, Object> metadata
) {
}
