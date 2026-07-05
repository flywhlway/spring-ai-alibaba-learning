package com.flywhl.saa.knowledgeqa.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.knowledgeqa.config.RedisChatMemoryRepository;
import com.flywhl.saa.knowledgeqa.mapper.MessageConverter;
import com.flywhl.saa.knowledgeqa.model.entity.QaConversation;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.ChatMessageVO;
import com.flywhl.saa.knowledgeqa.model.vo.ConversationVO;
import com.flywhl.saa.knowledgeqa.repository.QaConversationRepository;
import com.flywhl.saa.knowledgeqa.repository.QaMessageRepository;

/**
 * 会话服务：分页列表、历史消息、删除会话（清 Redis + PG 归档）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class ConversationService {

    private final AuthService authService;
    private final QaConversationRepository conversationRepository;
    private final QaMessageRepository messageRepository;
    private final RedisChatMemoryRepository chatMemoryRepository;
    private final MessageConverter messageConverter;

    public ConversationService(
            AuthService authService,
            QaConversationRepository conversationRepository,
            QaMessageRepository messageRepository,
            RedisChatMemoryRepository chatMemoryRepository,
            MessageConverter messageConverter) {
        this.authService = authService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.chatMemoryRepository = chatMemoryRepository;
        this.messageConverter = messageConverter;
    }

    public PageResult<ConversationVO> list(int page, int size) {
        SysUser user = authService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<QaConversation> result = conversationRepository.findByUserIdOrderByUpdatedAtDesc(
                user.getId(), pageable);
        return PageResult.of(page, size, result.getTotalElements(),
                result.getContent().stream().map(messageConverter::toVo).toList());
    }

    public PageResult<ChatMessageVO> getMessages(String conversationId, int page, int size) {
        SysUser user = authService.requireCurrentUser();
        QaConversation conversation = requireOwnedConversation(conversationId, user);
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<com.flywhl.saa.knowledgeqa.model.entity.QaMessage> result =
                messageRepository.findByConversationIdOrderByCreatedAtAsc(
                        conversation.getConversationId(), pageable);
        return PageResult.of(page, size, result.getTotalElements(),
                result.getContent().stream().map(messageConverter::toMessageVo).toList());
    }

    @Transactional
    public void delete(String conversationId) {
        SysUser user = authService.requireCurrentUser();
        QaConversation conversation = requireOwnedConversation(conversationId, user);
        chatMemoryRepository.deleteByConversationId(conversation.getConversationId());
        messageRepository.deleteByConversationId(conversation.getConversationId());
        conversationRepository.delete(conversation);
    }

    private QaConversation requireOwnedConversation(String conversationId, SysUser user) {
        QaConversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "会话不存在"));
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new BizException(CommonResultCode.FORBIDDEN, "无权访问该会话");
        }
        return conversation;
    }
}
