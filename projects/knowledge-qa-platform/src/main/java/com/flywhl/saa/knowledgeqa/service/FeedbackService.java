package com.flywhl.saa.knowledgeqa.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.knowledgeqa.model.dto.FeedbackRequest;
import com.flywhl.saa.knowledgeqa.model.entity.QaFeedback;
import com.flywhl.saa.knowledgeqa.model.entity.QaMessage;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.repository.QaFeedbackRepository;
import com.flywhl.saa.knowledgeqa.repository.QaMessageRepository;

/**
 * 反馈服务：qa_feedback 落库。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class FeedbackService {

    private final AuthService authService;
    private final QaMessageRepository messageRepository;
    private final QaFeedbackRepository feedbackRepository;

    public FeedbackService(
            AuthService authService,
            QaMessageRepository messageRepository,
            QaFeedbackRepository feedbackRepository) {
        this.authService = authService;
        this.messageRepository = messageRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public void save(FeedbackRequest request) {
        SysUser user = authService.requireCurrentUser();
        QaMessage message = messageRepository.findById(request.messageId())
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "消息不存在"));

        if (!"ASSISTANT".equals(message.getRole())) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "仅可对助手回复提交反馈");
        }

        QaFeedback feedback = feedbackRepository.findByMessageIdAndUserId(request.messageId(), user.getId())
                .orElseGet(QaFeedback::new);
        feedback.setMessage(message);
        feedback.setUser(user);
        feedback.setRating(request.rating().shortValue());
        feedback.setComment(request.comment());
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(OffsetDateTime.now());
        }
        feedbackRepository.save(feedback);
    }
}
