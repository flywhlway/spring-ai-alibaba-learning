package com.flywhl.saa.jsonschema.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 单条检查发现（嵌套 Record 叶子节点）。
 *
 * @author flywhl
 */
public record InspectionFinding(
        @JsonPropertyDescription("检查项名称") String item,
        @JsonPropertyDescription("严重程度：LOW / MEDIUM / HIGH") String severity,
        @JsonPropertyDescription("发现描述") String detail) {
}
