package com.flywhl.saa.knowledgeqa.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.flywhl.saa.knowledgeqa.service.AuditLogService;
import com.flywhl.saa.starter.advisor.AdvisorOrder;
import com.flywhl.saa.starter.advisor.AuditLoggingAdvisor;

import reactor.core.publisher.Flux;

/**
 * 审计 Advisor：复用 starter {@link AuditLoggingAdvisor} 日志行为，并额外落库 {@code audit_log}（action=AI_CALL）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class DbAuditLoggingAdvisor implements CallAdvisor, StreamAdvisor {

    private final AuditLoggingAdvisor delegate;
    private final AuditLogService auditLogService;

    public DbAuditLoggingAdvisor(AuditLoggingAdvisor delegate, AuditLogService auditLogService) {
        this.delegate = delegate;
        this.auditLogService = auditLogService;
    }

    @Override
    public String getName() {
        return "DbAuditLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.AUDIT;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();
        ChatClientResponse response = delegate.adviseCall(request, chain);
        persistAiCall(response, System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long start = System.currentTimeMillis();
        AtomicReference<ChatClientResponse> lastResponse = new AtomicReference<>();
        return delegate.adviseStream(request, chain)
                .doOnNext(lastResponse::set)
                .doOnComplete(() -> persistAiCall(lastResponse.get(), System.currentTimeMillis() - start));
    }

    private void persistAiCall(ChatClientResponse clientResponse, long latencyMs) {
        ChatResponse chatResponse = clientResponse != null ? clientResponse.chatResponse() : null;
        Map<String, Object> detail = new HashMap<>();
        detail.put("latencyMs", latencyMs);

        String model = "unknown";
        int promptTokens = 0;
        int completionTokens = 0;
        if (chatResponse != null && chatResponse.getMetadata() != null) {
            if (chatResponse.getMetadata().getModel() != null) {
                model = chatResponse.getMetadata().getModel();
            }
            Usage usage = chatResponse.getMetadata().getUsage();
            if (usage != null) {
                promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
            }
        }
        detail.put("model", model);
        detail.put("promptTokens", promptTokens);
        detail.put("completionTokens", completionTokens);

        Long userId = resolveCurrentUserId();
        auditLogService.save(userId, "AI_CALL", "chat_model", model, detail);
    }

    private static Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object uid = jwt.getClaim("uid");
            if (uid instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }
}
