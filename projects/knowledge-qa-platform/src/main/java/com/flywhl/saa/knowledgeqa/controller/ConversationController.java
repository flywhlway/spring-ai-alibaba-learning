package com.flywhl.saa.knowledgeqa.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.knowledgeqa.model.vo.ChatMessageVO;
import com.flywhl.saa.knowledgeqa.model.vo.ConversationVO;
import com.flywhl.saa.knowledgeqa.service.ConversationService;

/**
 * 会话管理：列表 / 历史消息 / 删除（员工侧）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/conversations")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public Result<PageResult<ConversationVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(conversationService.list(page, size));
    }

    @GetMapping("/{conversationId}/messages")
    public Result<PageResult<ChatMessageVO>> messages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(conversationService.getMessages(conversationId, page, size));
    }

    @DeleteMapping("/{conversationId}")
    public Result<Void> delete(@PathVariable String conversationId) {
        conversationService.delete(conversationId);
        return Result.ok();
    }
}
