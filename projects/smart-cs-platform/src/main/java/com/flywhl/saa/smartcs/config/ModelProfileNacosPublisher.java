package com.flywhl.saa.smartcs.config;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.smartcs.model.entity.ModelProfile;
import com.flywhl.saa.smartcs.repository.ModelProfileRepository;

/**
 * model_profile 发布：将全部启用配置推送至 Nacos Data ID {@code scs.model.profiles}。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class ModelProfileNacosPublisher {

    private static final Logger log = LoggerFactory.getLogger(ModelProfileNacosPublisher.class);
    static final String DATA_ID = "scs.model.profiles";
    private static final String GROUP = "DEFAULT_GROUP";

    private final ModelProfileRepository modelProfileRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.nacos.server-addr:127.0.0.1:8848}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.username:nacos}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.password:nacos}")
    private String nacosPassword;

    public ModelProfileNacosPublisher(ModelProfileRepository modelProfileRepository, ObjectMapper objectMapper) {
        this.modelProfileRepository = modelProfileRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 推送全部 model_profile 行（含 disabled，便于对端完整热更新）至 Nacos。
     */
    public void publishAll() {
        List<ModelProfile> profiles = modelProfileRepository.findAll();
        List<Map<String, Object>> payload = profiles.stream().map(this::toPayload).toList();
        try {
            String json = objectMapper.writeValueAsString(payload);
            ConfigService configService = createConfigService();
            boolean ok = configService.publishConfig(DATA_ID, GROUP, json, "json");
            if (!ok) {
                throw new BizException(CommonResultCode.INTERNAL_ERROR, "Nacos model_profile 配置推送失败");
            }
            log.info("已推送 {} 条 model_profile 至 Nacos Data ID={}", payload.size(), DATA_ID);
        } catch (JsonProcessingException ex) {
            throw new BizException(CommonResultCode.INTERNAL_ERROR, "model_profile 配置序列化失败");
        } catch (NacosException ex) {
            log.error("Nacos 推送 model_profile 失败", ex);
            throw new BizException(CommonResultCode.INTERNAL_ERROR, "Nacos 不可用，model_profile 发布失败: " + ex.getMessage());
        }
    }

    private Map<String, Object> toPayload(ModelProfile profile) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("profileKey", profile.getProfileKey());
        map.put("provider", profile.getProvider());
        map.put("modelName", profile.getModelName());
        map.put("scene", profile.getScene());
        map.put("priority", profile.getPriority());
        map.put("enabled", profile.getEnabled());
        map.put("optionsJson", profile.getOptionsJson());
        return map;
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
