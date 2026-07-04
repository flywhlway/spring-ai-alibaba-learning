package com.flywhl.saa.prompt.model;

import java.util.List;

/**
 * JSON 结构化输出目标类型，配合 {@code BeanOutputConverter} 约束模型输出格式。
 *
 * @author flywhl
 */
public record FaultDiagnosis(String dtcCode, List<String> possibleCauses, String severity, String suggestedAction) {
}
