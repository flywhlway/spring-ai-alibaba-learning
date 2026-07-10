package com.flywhl.saa.smartcs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import com.flywhl.saa.smartcs.config.ScsProperties;

/**
 * {@link SemanticCacheService} 单测：mock {@code redisStackVectorStore}，验证命中未过期返回答案、
 * 过期条目跳过、无命中返回空三种分支。
 *
 * @author flywhl
 * @since 1.0.0
 */
class SemanticCacheServiceTest {

    private final VectorStore redisStackVectorStore = mock(VectorStore.class);
    private final ScsProperties properties = new ScsProperties(
            null, new ScsProperties.Cache(0.95, 300, "redis://localhost:6380"), null, null, null);
    private final SemanticCacheService service = new SemanticCacheService(redisStackVectorStore, properties);

    @Test
    void lookup_returnsAnswer_whenCacheHitNotExpired() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("type", "semantic-cache");
        metadata.put("answer", "自签收之日起 7 天内可申请无理由退货");
        metadata.put("expiresAt", Instant.now().plusSeconds(60).toString());
        Document hit = Document.builder()
                .id("cache-1")
                .text("多久可以退货")
                .metadata(metadata)
                .score(0.97)
                .build();

        when(redisStackVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(hit));

        Optional<SemanticCacheService.CacheHit> result = service.lookup("多久可以退货？");

        assertThat(result).isPresent();
        assertThat(result.get().answer()).isEqualTo("自签收之日起 7 天内可申请无理由退货");
    }

    @Test
    void lookup_skipsExpiredEntry_andReturnsEmpty() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("type", "semantic-cache");
        metadata.put("answer", "过期答案");
        metadata.put("expiresAt", Instant.now().minusSeconds(60).toString());
        Document expired = Document.builder()
                .id("cache-2")
                .text("过期问题")
                .metadata(metadata)
                .score(0.96)
                .build();

        when(redisStackVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(expired));

        Optional<SemanticCacheService.CacheHit> result = service.lookup("过期问题？");

        assertThat(result).isEmpty();
    }

    @Test
    void lookup_returnsEmpty_whenNoResults() {
        when(redisStackVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        Optional<SemanticCacheService.CacheHit> result = service.lookup("从未见过的问题");

        assertThat(result).isEmpty();
    }
}
