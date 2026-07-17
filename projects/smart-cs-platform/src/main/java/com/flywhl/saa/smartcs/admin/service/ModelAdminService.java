package com.flywhl.saa.smartcs.admin.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.config.ModelProfileNacosPublisher;
import com.flywhl.saa.smartcs.model.dto.ModelProfileSaveRequest;
import com.flywhl.saa.smartcs.model.entity.ModelProfile;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.ModelProfileVO;
import com.flywhl.saa.smartcs.repository.ModelProfileRepository;
import com.flywhl.saa.smartcs.service.AuditLogService;
import com.flywhl.saa.smartcs.service.AuthService;

/**
 * 模型配置管理：CRUD + 保存/发布时推送 Nacos {@code scs.model.profiles}。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class ModelAdminService {

    private static final Set<String> ALLOWED_SCENES = Set.of("FAQ", "BUSINESS", "TICKET");

    private final ModelProfileRepository modelProfileRepository;
    private final ModelProfileNacosPublisher nacosPublisher;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public ModelAdminService(
            ModelProfileRepository modelProfileRepository,
            ModelProfileNacosPublisher nacosPublisher,
            AuthService authService,
            AuditLogService auditLogService) {
        this.modelProfileRepository = modelProfileRepository;
        this.nacosPublisher = nacosPublisher;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    public List<ModelProfileVO> list(String scene) {
        List<ModelProfile> entities = scene != null && !scene.isBlank()
                ? modelProfileRepository.findByScene(scene.trim().toUpperCase(Locale.ROOT))
                : modelProfileRepository.findAll();
        return entities.stream().map(this::toVo).toList();
    }

    public ModelProfileVO get(Long id) {
        return toVo(requireProfile(id));
    }

    @Transactional
    public ModelProfileVO create(ModelProfileSaveRequest request) {
        validateScene(request.scene());
        if (modelProfileRepository.existsByProfileKey(request.profileKey())) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "profileKey 已存在");
        }
        OffsetDateTime now = OffsetDateTime.now();
        ModelProfile entity = new ModelProfile();
        applyRequest(entity, request);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        ModelProfile saved = modelProfileRepository.save(entity);
        publishAndAudit(saved, "CREATE_MODEL_PROFILE");
        return toVo(saved);
    }

    @Transactional
    public ModelProfileVO update(Long id, ModelProfileSaveRequest request) {
        validateScene(request.scene());
        ModelProfile entity = requireProfile(id);
        modelProfileRepository.findByProfileKey(request.profileKey())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BizException(CommonResultCode.BAD_REQUEST, "profileKey 已被其它记录占用");
                });
        applyRequest(entity, request);
        entity.setUpdatedAt(OffsetDateTime.now());
        ModelProfile saved = modelProfileRepository.save(entity);
        publishAndAudit(saved, "UPDATE_MODEL_PROFILE");
        return toVo(saved);
    }

    @Transactional
    public void delete(Long id) {
        ModelProfile entity = requireProfile(id);
        modelProfileRepository.delete(entity);
        SysUser operator = authService.requireCurrentUser();
        nacosPublisher.publishAll();
        auditLogService.save(
                operator.getId(),
                "DELETE_MODEL_PROFILE",
                "model_profile",
                String.valueOf(id),
                Map.of("profileKey", entity.getProfileKey(), "scene", entity.getScene()));
    }

    /**
     * 显式发布：将当前全部 model_profile 推送 Nacos（与保存后自动推送等价，供运营手动触发）。
     */
    @Transactional
    public ModelProfileVO publish(Long id) {
        ModelProfile entity = requireProfile(id);
        SysUser operator = authService.requireCurrentUser();
        nacosPublisher.publishAll();
        auditLogService.save(
                operator.getId(),
                "PUBLISH_MODEL_PROFILE",
                "model_profile",
                String.valueOf(id),
                Map.of("profileKey", entity.getProfileKey(), "scene", entity.getScene()));
        return toVo(entity);
    }

    private void publishAndAudit(ModelProfile saved, String action) {
        SysUser operator = authService.requireCurrentUser();
        nacosPublisher.publishAll();
        auditLogService.save(
                operator.getId(),
                action,
                "model_profile",
                String.valueOf(saved.getId()),
                Map.of(
                        "profileKey", saved.getProfileKey(),
                        "scene", saved.getScene(),
                        "provider", saved.getProvider(),
                        "enabled", saved.getEnabled()));
    }

    private ModelProfile requireProfile(Long id) {
        return modelProfileRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "模型配置不存在"));
    }

    private static void validateScene(String scene) {
        String normalized = scene == null ? "" : scene.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_SCENES.contains(normalized)) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "scene 仅支持 FAQ|BUSINESS|TICKET");
        }
    }

    private static void applyRequest(ModelProfile entity, ModelProfileSaveRequest request) {
        entity.setProfileKey(request.profileKey().trim());
        entity.setProvider(request.provider().trim().toUpperCase(Locale.ROOT));
        entity.setModelName(request.modelName().trim());
        entity.setScene(request.scene().trim().toUpperCase(Locale.ROOT));
        entity.setPriority(request.priority());
        entity.setEnabled(request.enabled());
        entity.setOptionsJson(request.optionsJson());
    }

    private ModelProfileVO toVo(ModelProfile entity) {
        return new ModelProfileVO(
                entity.getId(),
                entity.getProfileKey(),
                entity.getProvider(),
                entity.getModelName(),
                entity.getScene(),
                entity.getPriority() != null ? entity.getPriority() : 0,
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getOptionsJson());
    }
}
