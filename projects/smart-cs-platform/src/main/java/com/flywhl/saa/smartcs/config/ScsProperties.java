package com.flywhl.saa.smartcs.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 项目自有配置属性（前缀 {@code scs}）：RAG / 语义缓存 / 会话记忆 / JWT 安全 / 工单升级分组绑定。
 *
 * @author flywhl
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "scs")
public record ScsProperties(
        Rag rag,
        Cache cache,
        Memory memory,
        Security security,
        Ticket ticket) {

    public ScsProperties {
        if (rag == null) {
            rag = new Rag(5, 0.35, 512, 64);
        }
        if (cache == null) {
            cache = new Cache(0.95, 300, "redis://localhost:6380");
        }
        if (memory == null) {
            memory = new Memory(20, Duration.ofDays(7));
        }
        if (security == null) {
            security = new Security(new Jwt("smart-cs-platform", Duration.ofHours(2), null));
        }
        if (ticket == null) {
            ticket = new Ticket(3, "NORMAL");
        }
    }

    /**
     * 混合检索 / RAG 参数：召回条数、阈值过滤、TokenTextSplitter 分块。
     */
    public record Rag(int topK, double similarityThreshold, int chunkSize, int chunkOverlap) {
    }

    /**
     * FAQ 语义缓存参数：Redis Stack（6380）命中阈值宁缺毋滥，默认 0.95。
     */
    public record Cache(double similarityThreshold, int ttlSeconds, String redisUri) {
    }

    /**
     * 会话记忆参数：Redis（6379）滚动窗口条数与过期时间。
     */
    public record Memory(int maxMessages, Duration ttl) {
    }

    public record Security(Jwt jwt) {
    }

    public record Jwt(String issuer, Duration accessTokenTtl, String secret) {

        public Jwt {
            if (secret == null || secret.isBlank()) {
                secret = "dev-only-scs-jwt-secret-key-32bytes!!";
            }
        }
    }

    /**
     * 工单升级策略：AI 连续失败 N 次自动转 {@code PENDING_HUMAN}，新建工单默认优先级。
     */
    public record Ticket(int autoEscalateAfterFailures, String defaultPriority) {
    }
}
