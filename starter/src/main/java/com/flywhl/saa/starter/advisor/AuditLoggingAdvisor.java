package com.flywhl.saa.starter.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.regex.Pattern;

/**
 * 审计日志 Advisor（第 06 章 AuditLoggingAdvisor 的 Starter 化生产版本）。
 *
 * <p>记录脱敏后的请求摘要与响应耗时；脱敏仅作用于审计日志，不影响传递给
 * 下游/模型的原始请求内容（第 06 章已强调这一边界）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public class AuditLoggingAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    @Override
    public String getName() {
        return "AuditLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.AUDIT;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();
        logRequest(request);
        ChatClientResponse response = chain.nextCall(request);
        logResponse(System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long start = System.currentTimeMillis();
        logRequest(request);
        return chain.nextStream(request)
                .doOnComplete(() -> logResponse(System.currentTimeMillis() - start));
    }

    private void logRequest(ChatClientRequest request) {
        var userMessage = request.prompt().getUserMessage();
        String masked = mask(userMessage == null ? null : userMessage.getText());
        AUDIT_LOG.info("[audit] request user_text={}", masked);
    }

    private void logResponse(long costMs) {
        AUDIT_LOG.info("[audit] response cost={}ms", costMs);
    }

    private String mask(String text) {
        if (text == null) {
            return null;
        }
        return PHONE_PATTERN.matcher(text).replaceAll(m -> {
            String phone = m.group();
            return phone.substring(0, 3) + "****" + phone.substring(7);
        });
    }
}
