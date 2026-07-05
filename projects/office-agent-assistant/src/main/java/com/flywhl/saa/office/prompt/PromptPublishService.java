package com.flywhl.saa.office.prompt;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.office.model.entity.PromptTemplateEntity;
import com.flywhl.saa.office.repository.PromptTemplateRepository;
import com.flywhl.saa.office.service.AuthService;

@Service
public class PromptPublishService {
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String ARCHIVED = "ARCHIVED";
    private final PromptTemplateRepository promptTemplateRepository;
    private final AuthService authService;
    public PromptPublishService(PromptTemplateRepository promptTemplateRepository, AuthService authService) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.authService = authService;
    }
    @Transactional
    public PromptTemplateEntity publish(Long templateId) {
        authService.requireCurrentUser();
        PromptTemplateEntity template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "Prompt 模板不存在"));
        if (!DRAFT.equals(template.getStatus())) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "仅草稿版本可发布");
        }
        promptTemplateRepository.findFirstByTemplateKeyAndStatusOrderByVersionDesc(template.getTemplateKey(), PUBLISHED)
                .ifPresent(existing -> {
                    existing.setStatus(ARCHIVED);
                    promptTemplateRepository.save(existing);
                });
        template.setStatus(PUBLISHED);
        template.setPublishedAt(LocalDateTime.now());
        return promptTemplateRepository.save(template);
    }
}

