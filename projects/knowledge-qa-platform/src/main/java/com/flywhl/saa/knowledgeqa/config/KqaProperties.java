package com.flywhl.saa.knowledgeqa.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 项目自有配置属性（前缀 {@code kqa}）：MinIO / RAG / 会话记忆 / JWT 安全分组绑定。
 *
 * @author flywhl
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "kqa")
public record KqaProperties(
        Minio minio,
        Rag rag,
        Memory memory,
        Security security) {

    public KqaProperties {
        if (minio == null) {
            minio = new Minio("http://localhost:9000", "minioadmin", "minioadmin", "kqa-documents");
        }
        if (rag == null) {
            rag = new Rag(5, 0.35, 512, 64, true);
        }
        if (memory == null) {
            memory = new Memory(20, Duration.ofDays(7));
        }
        if (security == null) {
            security = new Security(new Jwt("knowledge-qa-platform", Duration.ofHours(2)));
        }
    }

    public record Minio(String endpoint, String accessKey, String secretKey, String bucket) {
    }

    public record Rag(int topK, double similarityThreshold, int chunkSize, int chunkOverlap, boolean citationEnabled) {
    }

    public record Memory(int maxMessages, Duration ttl) {
    }

    public record Security(Jwt jwt) {
    }

    public record Jwt(String issuer, Duration accessTokenTtl) {
    }
}
