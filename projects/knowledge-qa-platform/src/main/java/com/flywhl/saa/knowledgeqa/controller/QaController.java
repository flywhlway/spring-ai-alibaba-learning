package com.flywhl.saa.knowledgeqa.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.model.dto.QaRequest;
import com.flywhl.saa.knowledgeqa.model.vo.QaAnswerVO;
import com.flywhl.saa.knowledgeqa.service.QaService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

/**
 * 问答入口：POST /api/qa/ask（同步 Result）与 GET /api/qa/stream（SSE：message/meta/error/done）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/qa")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    @PostMapping("/ask")
    public Result<QaAnswerVO> ask(@Valid @RequestBody QaRequest request) {
        return Result.ok(qaService.ask(request));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam String conversationId,
            @RequestParam String question) {
        return qaService.stream(conversationId, question);
    }
}
