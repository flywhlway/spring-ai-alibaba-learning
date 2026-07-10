package com.flywhl.saa.smartcs.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import com.flywhl.saa.smartcs.config.ScsProperties;
import com.flywhl.saa.smartcs.model.vo.ChatAnswerVO;
import com.flywhl.saa.smartcs.prompt.PromptTemplateProvider;
import com.flywhl.saa.smartcs.rag.HybridSearchService;

/**
 * FAQ 问答核心服务：语义缓存优先命中秒回 → 未命中时 Milvus+ES 混合检索取上下文 → RAG 生成
 * → 回写缓存。链路顺序严格遵循 {@code 06-03-PLAN.md} must_haves 契约：
 * {@link SemanticCacheService#lookup} 优先；未命中时调用 {@link HybridSearchService#hybridSearch}
 * 取候选片段拼装为上下文，渲染 {@code faq-answer-system} Prompt（{@code {context}} 占位符）后交由
 * ChatClient 生成，最终经 {@link SemanticCacheService#put} 回写。
 *
 * <p>混合检索结果为空时直接返回转人工提示，不调用大模型，也不写入缓存，避免无依据编造答案
 * （威胁登记 T-06-04，与 {@code RagPipelineFactory} 的 {@code allowEmptyContext=false} 语义等价）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class FaqAnswerService {

    private static final String ROUTE_AGENT = "faq-agent";
    private static final String EMPTY_CONTEXT_FALLBACK = "暂未找到相关内容，建议转人工";

    private final SemanticCacheService semanticCacheService;
    private final HybridSearchService hybridSearchService;
    private final ChatClient chatClient;
    private final PromptTemplateProvider promptTemplateProvider;
    private final ScsProperties properties;

    public FaqAnswerService(
            SemanticCacheService semanticCacheService,
            HybridSearchService hybridSearchService,
            ChatClient.Builder chatClientBuilder,
            PromptTemplateProvider promptTemplateProvider,
            ScsProperties properties) {
        this.semanticCacheService = semanticCacheService;
        this.hybridSearchService = hybridSearchService;
        this.chatClient = chatClientBuilder.build();
        this.promptTemplateProvider = promptTemplateProvider;
        this.properties = properties;
    }

    /**
     * FAQ 问答主入口：缓存 → 混合检索 → RAG 生成 → 回写缓存。
     */
    public ChatAnswerVO answer(String query) {
        Optional<SemanticCacheService.CacheHit> cacheHit = semanticCacheService.lookup(query);
        if (cacheHit.isPresent()) {
            return new ChatAnswerVO(cacheHit.get().answer(), ROUTE_AGENT, true,
                    new ChatAnswerVO.TokenUsageVO(0, 0));
        }

        List<Map<String, Object>> hits = hybridSearchService.hybridSearch(query, properties.rag().topK());
        String context = buildContext(hits);
        if (context.isBlank()) {
            return new ChatAnswerVO(EMPTY_CONTEXT_FALLBACK, ROUTE_AGENT, false,
                    new ChatAnswerVO.TokenUsageVO(0, 0));
        }

        String systemPrompt = new PromptTemplate(promptTemplateProvider.getFaqAnswerSystemTemplate())
                .render(Map.of("context", context));

        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(query)
                .call()
                .chatResponse();

        String answer = extractText(response);
        ChatAnswerVO.TokenUsageVO usage = toUsage(response);
        semanticCacheService.put(query, answer, properties.cache().ttlSeconds());

        return new ChatAnswerVO(answer, ROUTE_AGENT, false, usage);
    }

    private static String buildContext(List<Map<String, Object>> hits) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (Map<String, Object> hit : hits) {
            Object content = hit.get("content");
            if (content == null || content.toString().isBlank()) {
                continue;
            }
            idx++;
            sb.append('[').append(idx).append("] ").append(content).append('\n');
        }
        return sb.toString().trim();
    }

    private static String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return "";
        }
        Generation generation = response.getResult();
        if (generation.getOutput() == null || generation.getOutput().getText() == null) {
            return "";
        }
        return generation.getOutput().getText();
    }

    private static ChatAnswerVO.TokenUsageVO toUsage(ChatResponse response) {
        if (response == null || response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return new ChatAnswerVO.TokenUsageVO(0, 0);
        }
        Usage usage = response.getMetadata().getUsage();
        return new ChatAnswerVO.TokenUsageVO(
                usage.getPromptTokens() != null ? usage.getPromptTokens() : 0,
                usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0);
    }
}
