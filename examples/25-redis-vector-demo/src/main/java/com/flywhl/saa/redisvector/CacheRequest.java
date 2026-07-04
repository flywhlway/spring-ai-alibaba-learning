package com.flywhl.saa.redisvector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * 语义缓存写入请求：将 query 向量化入库，answer 写入 metadata，并记录 TTL。
 *
 * @author flywhl
 */
public record CacheRequest(
        @NotBlank String query,
        @NotBlank String answer,
        @Positive Integer ttlSeconds
) {
}
