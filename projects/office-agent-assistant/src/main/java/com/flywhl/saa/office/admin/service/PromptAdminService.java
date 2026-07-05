package com.flywhl.saa.office.admin.service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flywhl.saa.office.mapper.PromptConverter;
import com.flywhl.saa.office.model.dto.PromptSaveRequest;
import com.flywhl.saa.office.model.entity.PromptTemplateEntity;
import com.flywhl.saa.office.model.vo.PromptTemplateVO;
import com.flywhl.saa.office.prompt.PromptPublishService;
import com.flywhl.saa.office.repository.PromptTemplateRepository;
import com.flywhl.saa.office.service.AuthService;
@Service
public class PromptAdminService {
    private final PromptTemplateRepository promptTemplateRepository;
    private final PromptConverter promptConverter;
    private final PromptPublishService promptPublishService;
    private final AuthService authService;
    public PromptAdminService(PromptTemplateRepository promptTemplateRepository, PromptConverter promptConverter,
            PromptPublishService promptPublishService, AuthService authService) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.promptConverter = promptConverter;
        this.promptPublishService = promptPublishService;
        this.authService = authService;
    }
    public List<PromptTemplateVO> list(String templateKey) {
        var entities = templateKey != null && !templateKey.isBlank()
                ? promptTemplateRepository.findByTemplateKeyOrderByVersionDesc(templateKey)
                : promptTemplateRepository.findAll();
        return entities.stream().map(promptConverter::toVo).toList();
    }
    @Transactional
    public PromptTemplateVO create(PromptSaveRequest request) {
        var creator = authService.requireCurrentUser();
        int nextVersion = promptTemplateRepository.findTopByTemplateKeyOrderByVersionDesc(request.templateKey())
                .map(e -> e.getVersion() + 1).orElse(1);
        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setTemplateKey(request.templateKey());
        entity.setVersion(nextVersion);
        entity.setContent(request.content());
        entity.setDescription(request.description());
        entity.setStatus("DRAFT");
        entity.setCreatedBy(creator);
        entity.setCreatedAt(LocalDateTime.now());
        return promptConverter.toVo(promptTemplateRepository.save(entity));
    }
    @Transactional
    public PromptTemplateVO publish(Long id) {
        return promptConverter.toVo(promptPublishService.publish(id));
    }
}

