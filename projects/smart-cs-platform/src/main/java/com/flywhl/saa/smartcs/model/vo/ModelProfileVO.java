package com.flywhl.saa.smartcs.model.vo;

import java.util.Map;

/**
 * 模型配置 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record ModelProfileVO(
        Long id,
        String profileKey,
        String provider,
        String modelName,
        String scene,
        int priority,
        boolean enabled,
        Map<String, Object> optionsJson) {
}
