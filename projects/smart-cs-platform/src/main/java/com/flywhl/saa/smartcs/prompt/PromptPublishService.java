package com.flywhl.saa.smartcs.prompt;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.model.entity.PromptTemplateEntity;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.repository.PromptTemplateRepository;
import com.flywhl.saa.smartcs.service.AuditLogService;
import com.flywhl.saa.smartcs.service.AuthService;

/**
 * Prompt 发布服务：DRAFT→PUBLISHED 版本流转并推送 Nacos（Data ID: spring.ai.alibaba.configurable.prompt）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class PromptPublishService {

    private static final Logger log = LoggerFactory.getLogger(PromptPublishService.class);
    private static final String DATA_ID = "spring.ai.alibaba.configurable.prompt";
    private static final String GROUP = "DEFAULT_GROUP";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String ARCHIVED = "ARCHIVED";
    private static final String DRAFT = "DRAFT";

    private final PromptTemplateRepository promptTemplateRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.nacos.server-addr:127.0.0.1:8848}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.username:nacos}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.password:nacos}")
    private String nacosPassword;

    public PromptPublishService(
            PromptTemplateRepository promptTemplateRepository,
            AuditLogService auditLogService,
            AuthService authService,
            ObjectMapper objectMapper) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.auditLogService = auditLogService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PromptTemplateEntity publish(Long templateId) {
        SysUser operator = authService.requireCurrentUser();
        PromptTemplateEntity template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "Prompt 模板不存在"));

        if (!DRAFT.equals(template.getStatus())) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "仅草稿版本可发布");
        }

        promptTemplateRepository.findByTemplateKeyAndStatusOrderByVersionDesc(template.getTemplateKey(), PUBLISHED)
                .ifPresent(existing -> {
                    existing.setStatus(ARCHIVED);
                    promptTemplateRepository.save(existing);
                });

        template.setStatus(PUBLISHED);
        template.setPublishedAt(OffsetDateTime.now());
        PromptTemplateEntity saved = promptTemplateRepository.save(template);

        publishConfigToNacos();

        auditLogService.save(
                operator.getId(),
                "PUBLISH_PROMPT",
                "prompt_template",
                String.valueOf(saved.getId()),
                Map.of(
                        "templateKey", saved.getTemplateKey(),
                        "version", saved.getVersion()));

        return saved;
    }

    void publishConfigToNacos() {
        List<Map<String, String>> payload = promptTemplateRepository.findByStatusOrderByTemplateKeyAsc(PUBLISHED)
                .stream()
                .collect(Collectors.groupingBy(PromptTemplateEntity::getTemplateKey, LinkedHashMap::new,
                        Collectors.maxBy((a, b) -> Integer.compare(a.getVersion(), b.getVersion()))))
                .values()
                .stream()
                .flatMap(java.util.Optional::stream)
                .map(entity -> Map.of(
                        "name", entity.getTemplateKey(),
                        "template", entity.getContent()))
                .toList();

        try {
            String json = objectMapper.writeValueAsString(payload);
            ConfigService configService = createConfigService();
            boolean ok = configService.publishConfig(DATA_ID, GROUP, json, "json");
            if (!ok) {
                throw new BizException(CommonResultCode.INTERNAL_ERROR, "Nacos Prompt 配置推送失败");
            }
            log.info("已推送 {} 个 PUBLISHED Prompt 模板至 Nacos", payload.size());
        } catch (JsonProcessingException ex) {
            throw new BizException(CommonResultCode.INTERNAL_ERROR, "Prompt 配置序列化失败");
        } catch (NacosException ex) {
            log.error("Nacos 推送失败", ex);
            throw new BizException(CommonResultCode.INTERNAL_ERROR, "Nacos 不可用，Prompt 发布失败: " + ex.getMessage());
        }
    }

    private ConfigService createConfigService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", nacosServerAddr);
        properties.put("username", nacosUsername);
        properties.put("password", nacosPassword);
        properties.put("encode", StandardCharsets.UTF_8.name());
        return NacosFactory.createConfigService(properties);
    }
}
