package com.flywhl.saa.smartcs.admin.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.smartcs.model.dto.PromptSaveRequest;
import com.flywhl.saa.smartcs.model.entity.PromptTemplateEntity;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.PromptTemplateVO;
import com.flywhl.saa.smartcs.prompt.PromptPublishService;
import com.flywhl.saa.smartcs.repository.PromptTemplateRepository;
import com.flywhl.saa.smartcs.service.AuthService;

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
    private final PromptPublishService promptPublishService;
    private final AuthService authService;

    public PromptAdminService(
            PromptTemplateRepository promptTemplateRepository,
            PromptPublishService promptPublishService,
            AuthService authService) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.promptPublishService = promptPublishService;
        this.authService = authService;
    }

    public List<PromptTemplateVO> list(String templateKey) {
        List<PromptTemplateEntity> entities = templateKey != null && !templateKey.isBlank()
                ? promptTemplateRepository.findByTemplateKeyOrderByVersionDesc(templateKey)
                : promptTemplateRepository.findAll();
        return entities.stream().map(this::toVo).toList();
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
        entity.setCreatedBy(creator.getId());
        entity.setCreatedAt(OffsetDateTime.now());
        return toVo(promptTemplateRepository.save(entity));
    }

    @Transactional
    public PromptTemplateVO publish(Long id) {
        return toVo(promptPublishService.publish(id));
    }

    private PromptTemplateVO toVo(PromptTemplateEntity entity) {
        return new PromptTemplateVO(
                entity.getId(),
                entity.getTemplateKey(),
                entity.getVersion(),
                entity.getContent(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPublishedAt(),
                entity.getCreatedBy(),
                entity.getCreatedAt());
    }
}
