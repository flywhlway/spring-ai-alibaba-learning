package com.flywhl.saa.knowledgeqa.admin.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.knowledgeqa.mapper.PromptConverter;
import com.flywhl.saa.knowledgeqa.model.dto.PromptSaveRequest;
import com.flywhl.saa.knowledgeqa.model.entity.PromptTemplateEntity;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.PromptTemplateVO;
import com.flywhl.saa.knowledgeqa.prompt.PromptPublishService;
import com.flywhl.saa.knowledgeqa.repository.PromptTemplateRepository;
import com.flywhl.saa.knowledgeqa.service.AuthService;

/**
 * Prompt 管理服务：prompt_template 版本化 CRUD 与发布编排。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class PromptAdminService {

    private static final String DRAFT = "DRAFT";

    private final PromptTemplateRepository promptTemplateRepository;
    private final PromptConverter promptConverter;
    private final PromptPublishService promptPublishService;
    private final AuthService authService;

    public PromptAdminService(
            PromptTemplateRepository promptTemplateRepository,
            PromptConverter promptConverter,
            PromptPublishService promptPublishService,
            AuthService authService) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.promptConverter = promptConverter;
        this.promptPublishService = promptPublishService;
        this.authService = authService;
    }

    public List<PromptTemplateVO> list(String templateKey) {
        List<PromptTemplateEntity> entities = templateKey != null && !templateKey.isBlank()
                ? promptTemplateRepository.findByTemplateKeyOrderByVersionDesc(templateKey)
                : promptTemplateRepository.findAll();
        return entities.stream().map(promptConverter::toVo).toList();
    }

    @Transactional
    public PromptTemplateVO create(PromptSaveRequest request) {
        SysUser creator = authService.requireCurrentUser();
        int nextVersion = promptTemplateRepository
                .findTopByTemplateKeyOrderByVersionDesc(request.templateKey())
                .map(entity -> entity.getVersion() + 1)
                .orElse(1);

        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setTemplateKey(request.templateKey());
        entity.setVersion(nextVersion);
        entity.setContent(request.content());
        entity.setDescription(request.description());
        entity.setStatus(DRAFT);
        entity.setCreatedBy(creator);
        entity.setCreatedAt(OffsetDateTime.now());
        return promptConverter.toVo(promptTemplateRepository.save(entity));
    }

    @Transactional
    public PromptTemplateVO publish(Long id) {
        PromptTemplateEntity published = promptPublishService.publish(id);
        return promptConverter.toVo(published);
    }
}
