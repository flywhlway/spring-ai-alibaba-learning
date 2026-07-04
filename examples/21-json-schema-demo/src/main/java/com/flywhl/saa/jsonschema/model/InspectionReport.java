package com.flywhl.saa.jsonschema.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * 嵌套结构化报告：主题 + 结论 + 发现列表。
 *
 * @author flywhl
 */
@JsonPropertyOrder({"topic", "summary", "findings"})
public record InspectionReport(
        @JsonPropertyDescription("检查主题") String topic,
        @JsonPropertyDescription("总体结论，一句话") String summary,
        @JsonPropertyDescription("具体发现列表，按严重程度降序") List<InspectionFinding> findings) {
}
