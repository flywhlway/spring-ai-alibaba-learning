package com.flywhl.saa.structured;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * 故障诊断结构化结果。
 *
 * @author flywhl
 */
@JsonPropertyOrder({"rootCause", "confidenceScore", "suggestedActions"})
public record DiagnosisResult(
        @JsonPropertyDescription("故障根本原因，一句话概括") String rootCause,
        @JsonPropertyDescription("诊断置信度，0-100的整数") int confidenceScore,
        @JsonPropertyDescription("建议的排查或处理措施，按优先级排序") List<String> suggestedActions) {
}
