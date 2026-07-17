package com.flywhl.saa.smartcs.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.smartcs.model.UserRole;
import com.flywhl.saa.smartcs.model.dto.ChatRequest;
import com.flywhl.saa.smartcs.model.entity.CsConversation;
import com.flywhl.saa.smartcs.model.entity.CsMessage;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.ChatAnswerVO;
import com.flywhl.saa.smartcs.repository.CsConversationRepository;
import com.flywhl.saa.smartcs.repository.CsMessageRepository;
import com.flywhl.saa.smartcs.service.CsOrchestratorService.OrchestratorResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 客服会话网关：ensureConversation / 消息归档 / 委托 {@link CsOrchestratorService}，
 * 同步问答与 SSE 流式（message / meta / interrupt / done / error）。
 *
 * <p>{@code conversationId} 与 {@code RunnableConfig.threadId} 全链路一致（UUID）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class ChatService {

    private static final int TITLE_MAX_LEN = 64;
    private static final int SSE_CHUNK_SIZE = 32;

    private final AuthService authService;
    private final CsOrchestratorService orchestratorService;
    private final FaqAnswerService faqAnswerService;
    private final CsConversationRepository conversationRepository;
    private final CsMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public ChatService(
            AuthService authService,
            CsOrchestratorService orchestratorService,
            FaqAnswerService faqAnswerService,
            CsConversationRepository conversationRepository,
            CsMessageRepository messageRepository,
            ObjectMapper objectMapper) {
        this.authService = authService;
        this.orchestratorService = orchestratorService;
        this.faqAnswerService = faqAnswerService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    public ChatAnswerVO ask(ChatRequest request) {
        SysUser user = authService.requireCurrentUser();
        String conversationId = resolveConversationId(request.conversationId());
        long startedAt = System.currentTimeMillis();

        CsConversation conversation = ensureConversation(user, conversationId, request.question());
        saveUserMessage(conversationId, request.question());

        OrchestratorResult result = orchestratorService.invoke(conversationId, request.question());
        long latencyMs = System.currentTimeMillis() - startedAt;

        if (result.interrupted()) {
            saveAssistantMessage(conversationId, "[INTERRUPT] 等待人工确认", result.routeAgent(), false,
                    new ChatAnswerVO.TokenUsageVO(0, 0), latencyMs);
            touchConversation(conversation, request.question());
            return new ChatAnswerVO(
                    "已触发人工接管，请坐席确认 threadId=" + conversationId,
                    result.routeAgent(),
                    false,
                    new ChatAnswerVO.TokenUsageVO(0, 0));
        }

        String answer = result.answer() == null ? "" : result.answer();
        // FAQ 路由且编排答案为空时，回退直调 FaqAnswerService（语义缓存 + RAG）
        boolean cacheHit = false;
        ChatAnswerVO.TokenUsageVO usage = new ChatAnswerVO.TokenUsageVO(0, 0);
        if (CsOrchestratorService.RouteAgent.FAQ.equals(result.routeAgent()) && answer.isBlank()) {
            ChatAnswerVO faq = faqAnswerService.answer(request.question());
            answer = faq.answer();
            cacheHit = faq.cacheHit();
            usage = faq.usage();
        }

        saveAssistantMessage(conversationId, answer, result.routeAgent(), cacheHit, usage, latencyMs);
        touchConversation(conversation, request.question());
        return new ChatAnswerVO(answer, result.routeAgent(), cacheHit, usage);
    }

    public Flux<ServerSentEvent<String>> stream(String conversationId, String question) {
        SysUser user = authService.requireCurrentUser();
        String resolvedId = resolveConversationId(conversationId);
        long startedAt = System.currentTimeMillis();

        CsConversation conversation = ensureConversation(user, resolvedId, question);
        saveUserMessage(resolvedId, question);

        return Mono.fromCallable(() -> orchestratorService.invoke(resolvedId, question))
                .flatMapMany(result -> {
                    if (result.interrupted()) {
                        long latencyMs = System.currentTimeMillis() - startedAt;
                        saveAssistantMessage(resolvedId, "[INTERRUPT] 等待人工确认", result.routeAgent(), false,
                                new ChatAnswerVO.TokenUsageVO(0, 0), latencyMs);
                        touchConversation(conversation, question);
                        return Flux.just(
                                buildInterruptEvent(resolvedId),
                                buildMetaEvent(result.routeAgent(), false, new ChatAnswerVO.TokenUsageVO(0, 0)),
                                ServerSentEvent.<String>builder().event("done").data("").build());
                    }

                    String answer = result.answer() == null ? "" : result.answer();
                    boolean cacheHit = false;
                    ChatAnswerVO.TokenUsageVO usage = new ChatAnswerVO.TokenUsageVO(0, 0);
                    if (CsOrchestratorService.RouteAgent.FAQ.equals(result.routeAgent()) && answer.isBlank()) {
                        ChatAnswerVO faq = faqAnswerService.answer(question);
                        answer = faq.answer();
                        cacheHit = faq.cacheHit();
                        usage = faq.usage();
                    }

                    String finalAnswer = answer;
                    boolean finalCacheHit = cacheHit;
                    ChatAnswerVO.TokenUsageVO finalUsage = usage;
                    String routeAgent = result.routeAgent();

                    Flux<ServerSentEvent<String>> messageFlux = Flux.fromIterable(chunkText(finalAnswer))
                            .map(chunk -> ServerSentEvent.<String>builder()
                                    .event("message")
                                    .data(chunk)
                                    .build());

                    return messageFlux
                            .concatWith(Mono.fromCallable(() -> {
                                long latencyMs = System.currentTimeMillis() - startedAt;
                                saveAssistantMessage(resolvedId, finalAnswer, routeAgent, finalCacheHit,
                                        finalUsage, latencyMs);
                                touchConversation(conversation, question);
                                return buildMetaEvent(routeAgent, finalCacheHit, finalUsage);
                            }))
                            .concatWith(Flux.just(
                                    ServerSentEvent.<String>builder().event("done").data("").build()));
                })
                .onErrorResume(ex -> Flux.just(buildErrorEvent(ex)));
    }

    public String resolveConversationId(String conversationId) {
        if (StringUtils.hasText(conversationId)) {
            return conversationId.trim();
        }
        return UUID.randomUUID().toString();
    }

    CsConversation ensureConversation(SysUser user, String conversationId, String question) {
        return conversationRepository.findByConversationId(conversationId)
                .map(existing -> {
                    assertAccess(existing, user);
                    return existing;
                })
                .orElseGet(() -> createConversation(user, conversationId, question));
    }

    private CsConversation createConversation(SysUser user, String conversationId, String question) {
        CsConversation conversation = new CsConversation();
        conversation.setConversationId(conversationId);
        conversation.setCustomerId(user.getId());
        conversation.setChannel("WEB");
        conversation.setTitle(truncateTitle(question));
        conversation.setMessageCount(0);
        OffsetDateTime now = OffsetDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        return conversationRepository.save(conversation);
    }

    private void assertAccess(CsConversation conversation, SysUser user) {
        if (user.getRole() == UserRole.CUSTOMER
                && !conversation.getCustomerId().equals(user.getId())) {
            throw new BizException(CommonResultCode.FORBIDDEN, "无权访问该会话");
        }
    }

    private void touchConversation(CsConversation conversation, String question) {
        conversation.setMessageCount((int) messageRepository.countByConversationId(conversation.getConversationId()));
        if (!StringUtils.hasText(conversation.getTitle())) {
            conversation.setTitle(truncateTitle(question));
        }
        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepository.save(conversation);
    }

    void saveUserMessage(String conversationId, String content) {
        CsMessage message = new CsMessage();
        message.setConversationId(conversationId);
        message.setRole("USER");
        message.setContent(content);
        message.setCacheHit(false);
        message.setInputTokens(0);
        message.setOutputTokens(0);
        message.setLatencyMs(0L);
        message.setCreatedAt(OffsetDateTime.now());
        messageRepository.save(message);
    }

    void saveAssistantMessage(
            String conversationId,
            String content,
            String routeAgent,
            boolean cacheHit,
            ChatAnswerVO.TokenUsageVO usage,
            long latencyMs) {
        CsMessage message = new CsMessage();
        message.setConversationId(conversationId);
        message.setRole("ASSISTANT");
        message.setContent(content);
        message.setRouteAgent(routeAgent);
        message.setCacheHit(cacheHit);
        message.setInputTokens(usage != null ? usage.inputTokens() : 0);
        message.setOutputTokens(usage != null ? usage.outputTokens() : 0);
        message.setLatencyMs(latencyMs);
        message.setCreatedAt(OffsetDateTime.now());
        messageRepository.save(message);
    }

    private static java.util.List<String> chunkText(String text) {
        if (!StringUtils.hasText(text)) {
            return java.util.List.of("");
        }
        java.util.List<String> chunks = new java.util.ArrayList<>();
        for (int i = 0; i < text.length(); i += SSE_CHUNK_SIZE) {
            chunks.add(text.substring(i, Math.min(i + SSE_CHUNK_SIZE, text.length())));
        }
        return chunks;
    }

    private ServerSentEvent<String> buildMetaEvent(
            String routeAgent, boolean cacheHit, ChatAnswerVO.TokenUsageVO usage) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("routeAgent", routeAgent);
        meta.put("cacheHit", cacheHit);
        meta.put("usage", usage);
        try {
            return ServerSentEvent.<String>builder()
                    .event("meta")
                    .data(objectMapper.writeValueAsString(meta))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化 meta 事件失败", ex);
        }
    }

    private ServerSentEvent<String> buildInterruptEvent(String threadId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("threadId", threadId);
        payload.put("status", "PENDING_HUMAN");
        payload.put("message", "等待坐席确认后调用 POST /api/handoff/approve");
        try {
            return ServerSentEvent.<String>builder()
                    .event("interrupt")
                    .data(objectMapper.writeValueAsString(payload))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化 interrupt 事件失败", ex);
        }
    }

    private ServerSentEvent<String> buildErrorEvent(Throwable ex) {
        Result<Void> errorPayload = Result.fail(CommonResultCode.INTERNAL_ERROR, safeMessage(ex));
        try {
            return ServerSentEvent.<String>builder()
                    .event("error")
                    .data(objectMapper.writeValueAsString(errorPayload))
                    .build();
        } catch (JsonProcessingException jsonEx) {
            return ServerSentEvent.<String>builder()
                    .event("error")
                    .data("{\"code\":9000,\"message\":\"系统内部错误，请稍后重试\"}")
                    .build();
        }
    }

    private static String safeMessage(Throwable ex) {
        if (ex instanceof BizException biz) {
            return biz.getMessage();
        }
        String message = ex.getMessage();
        return message == null || message.isBlank() ? CommonResultCode.INTERNAL_ERROR.message() : message;
    }

    private static String truncateTitle(String question) {
        if (!StringUtils.hasText(question)) {
            return "新会话";
        }
        String trimmed = question.trim();
        if (trimmed.length() <= TITLE_MAX_LEN) {
            return trimmed;
        }
        return trimmed.substring(0, TITLE_MAX_LEN) + "…";
    }
}
