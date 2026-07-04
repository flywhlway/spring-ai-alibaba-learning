package com.flywhl.saa.customadvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;

import java.util.regex.Pattern;

/**
 * 审计 Advisor：记录脱敏后的请求摘要与响应耗时。
 *
 * <p>脱敏只影响日志，{@code chain.nextCall(request)} 传递原始 request，模型仍看到明文。
 *
 * @author flywhl
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
        return Ordered.HIGHEST_PRECEDENCE + 100;
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
                .doOnComplete(() -> AUDIT_LOG.info("[audit] stream completed, cost={}ms",
                        System.currentTimeMillis() - start));
    }

    private void logRequest(ChatClientRequest request) {
        String text = request.prompt().getUserMessage().getText();
        AUDIT_LOG.info("[audit] request user_text={}", mask(text));
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
