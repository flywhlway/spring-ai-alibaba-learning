package com.flywhl.saa.knowledgeqa.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.model.dto.QaRequest;
import com.flywhl.saa.knowledgeqa.model.entity.QaConversation;
import com.flywhl.saa.knowledgeqa.model.entity.QaMessage;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.CitationVO;
import com.flywhl.saa.knowledgeqa.model.vo.QaAnswerVO;
import com.flywhl.saa.knowledgeqa.rag.CitationPostProcessor;
import com.flywhl.saa.knowledgeqa.repository.QaConversationRepository;
import com.flywhl.saa.knowledgeqa.repository.QaMessageRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 问答核心服务：RAG 调用、citation 组装、消息归档与 SSE 流式输出。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class QaService {

    private static final int TITLE_MAX_LEN = 64;

    private final ChatClient chatClient;
    private final CitationPostProcessor citationPostProcessor;
    private final VectorStoreDocumentRetriever documentRetriever;
    private final AuthService authService;
    private final QaConversationRepository conversationRepository;
    private final QaMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public QaService(
            ChatClient chatClient,
            CitationPostProcessor citationPostProcessor,
            VectorStoreDocumentRetriever documentRetriever,
            AuthService authService,
            QaConversationRepository conversationRepository,
            QaMessageRepository messageRepository,
            ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.citationPostProcessor = citationPostProcessor;
        this.documentRetriever = documentRetriever;
        this.authService = authService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    public QaAnswerVO ask(QaRequest request) {
        SysUser user = authService.requireCurrentUser();
        long startedAt = System.currentTimeMillis();

        QaConversation conversation = ensureConversation(user, request.conversationId(), request.question());
        saveUserMessage(request.conversationId(), request.question());

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .user(request.question())
                .call()
                .chatResponse();

        String answer = extractText(response);
        List<CitationVO> citations = citationPostProcessor.retrieve(request.question(), documentRetriever);
        QaAnswerVO.TokenUsageVO usage = toUsage(response);
        String model = response.getMetadata() != null ? response.getMetadata().getModel() : null;
        long latencyMs = System.currentTimeMillis() - startedAt;

        saveAssistantMessage(request.conversationId(), answer, citations, model, usage, latencyMs);
        touchConversation(conversation, request.question());

        return new QaAnswerVO(answer, model, citations, usage);
    }

    public Flux<ServerSentEvent<String>> stream(String conversationId, String question) {
        SysUser user = authService.requireCurrentUser();
        QaConversation conversation = ensureConversation(user, conversationId, question);
        saveUserMessage(conversationId, question);

        AtomicReference<StringBuilder> answerBuffer = new AtomicReference<>(new StringBuilder());
        AtomicReference<String> modelRef = new AtomicReference<>();
        AtomicInteger inputTokens = new AtomicInteger();
        AtomicInteger outputTokens = new AtomicInteger();
        long startedAt = System.currentTimeMillis();

        Flux<ServerSentEvent<String>> messageFlux = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .stream()
                .chatResponse()
                .map(chatResponse -> {
                    accumulateUsage(chatResponse, modelRef, inputTokens, outputTokens);
                    String delta = extractText(chatResponse);
                    if (StringUtils.hasText(delta)) {
                        answerBuffer.get().append(delta);
                    }
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(delta == null ? "" : delta)
                            .build();
                });

        return messageFlux
                .concatWith(Mono.fromCallable(() -> {
                    List<CitationVO> citations = citationPostProcessor.retrieve(question, documentRetriever);
                    QaAnswerVO.TokenUsageVO usage = new QaAnswerVO.TokenUsageVO(
                            inputTokens.get(), outputTokens.get());
                    String answer = answerBuffer.get().toString();
                    long latencyMs = System.currentTimeMillis() - startedAt;
                    saveAssistantMessage(conversationId, answer, citations, modelRef.get(), usage, latencyMs);
                    touchConversation(conversation, question);
                    return buildMetaEvent(citations, modelRef.get(), usage);
                }))
                .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("").build()))
                .onErrorResume(ex -> Flux.just(buildErrorEvent(ex)));
    }

    private QaConversation ensureConversation(SysUser user, String conversationId, String question) {
        return conversationRepository.findByConversationId(conversationId)
                .map(existing -> {
                    assertOwner(existing, user);
                    return existing;
                })
                .orElseGet(() -> createConversation(user, conversationId, question));
    }

    private QaConversation createConversation(SysUser user, String conversationId, String question) {
        QaConversation conversation = new QaConversation();
        conversation.setConversationId(conversationId);
        conversation.setUser(user);
        conversation.setTitle(truncateTitle(question));
        conversation.setMessageCount(0);
        OffsetDateTime now = OffsetDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        return conversationRepository.save(conversation);
    }

    private void assertOwner(QaConversation conversation, SysUser user) {
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new BizException(CommonResultCode.FORBIDDEN, "无权访问该会话");
        }
    }

    private void touchConversation(QaConversation conversation, String question) {
        conversation.setMessageCount((int) messageRepository.countByConversationId(conversation.getConversationId()));
        if (!StringUtils.hasText(conversation.getTitle())) {
            conversation.setTitle(truncateTitle(question));
        }
        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepository.save(conversation);
    }

    private void saveUserMessage(String conversationId, String content) {
        QaMessage message = new QaMessage();
        message.setConversationId(conversationId);
        message.setRole("USER");
        message.setContent(content);
        message.setCreatedAt(OffsetDateTime.now());
        messageRepository.save(message);
    }

    private void saveAssistantMessage(
            String conversationId,
            String content,
            List<CitationVO> citations,
            String model,
            QaAnswerVO.TokenUsageVO usage,
            long latencyMs) {
        QaMessage message = new QaMessage();
        message.setConversationId(conversationId);
        message.setRole("ASSISTANT");
        message.setContent(content);
        message.setCitations(toCitationMaps(citations));
        message.setModel(model);
        message.setInputTokens(usage != null ? usage.inputTokens() : 0);
        message.setOutputTokens(usage != null ? usage.outputTokens() : 0);
        message.setLatencyMs(latencyMs);
        message.setCreatedAt(OffsetDateTime.now());
        messageRepository.save(message);
    }

    private static List<Map<String, Object>> toCitationMaps(List<CitationVO> citations) {
        if (citations == null || citations.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> maps = new ArrayList<>(citations.size());
        for (CitationVO citation : citations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("documentId", citation.documentId());
            map.put("documentTitle", citation.documentTitle());
            map.put("chunkId", citation.chunkId());
            map.put("snippet", citation.snippet());
            map.put("score", citation.score());
            maps.add(map);
        }
        return maps;
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

    private static QaAnswerVO.TokenUsageVO toUsage(ChatResponse response) {
        if (response == null || response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return new QaAnswerVO.TokenUsageVO(0, 0);
        }
        Usage usage = response.getMetadata().getUsage();
        return new QaAnswerVO.TokenUsageVO(
                usage.getPromptTokens() != null ? usage.getPromptTokens() : 0,
                usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0);
    }

    private static void accumulateUsage(
            ChatResponse response,
            AtomicReference<String> modelRef,
            AtomicInteger inputTokens,
            AtomicInteger outputTokens) {
        if (response == null || response.getMetadata() == null) {
            return;
        }
        if (StringUtils.hasText(response.getMetadata().getModel())) {
            modelRef.set(response.getMetadata().getModel());
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage == null) {
            return;
        }
        if (usage.getPromptTokens() != null) {
            inputTokens.set(usage.getPromptTokens());
        }
        if (usage.getCompletionTokens() != null) {
            outputTokens.set(usage.getCompletionTokens());
        }
    }

    private ServerSentEvent<String> buildMetaEvent(
            List<CitationVO> citations, String model, QaAnswerVO.TokenUsageVO usage) {
        QaAnswerVO meta = new QaAnswerVO(null, model, citations, usage);
        try {
            return ServerSentEvent.<String>builder()
                    .event("meta")
                    .data(objectMapper.writeValueAsString(meta))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化 meta 事件失败", ex);
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
