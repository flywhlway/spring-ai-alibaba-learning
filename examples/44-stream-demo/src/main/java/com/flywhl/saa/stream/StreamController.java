package com.flywhl.saa.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 统一 SSE 事件协议：{@code message} / {@code error} / {@code done}。
 *
 * @author flywhl
 */
@RestController
public class StreamController {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public StreamController(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/chat/stream-unified", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamUnified(@RequestParam String message) {
        return chatClient.prompt().user(message).stream().chatResponse()
                .map(chatResponse -> {
                    String text = chatResponse.getResult().getOutput().getText();
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(text == null ? "" : text)
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("").build()))
                .onErrorResume(ex -> Flux.just(buildErrorEvent(ex)));
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
        String msg = ex.getMessage();
        return msg == null || msg.isBlank() ? CommonResultCode.INTERNAL_ERROR.message() : msg;
    }
}
