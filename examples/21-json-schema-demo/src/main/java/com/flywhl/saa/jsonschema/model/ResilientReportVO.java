package com.flywhl.saa.jsonschema.model;

/**
 * 容错路径结果：报告本体 + 是否走了宽松回退。
 *
 * @author flywhl
 */
public record ResilientReportVO(InspectionReport report, boolean usedFallback, String path) {
}
