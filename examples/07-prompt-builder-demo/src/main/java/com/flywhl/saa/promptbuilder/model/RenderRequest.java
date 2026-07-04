package com.flywhl.saa.promptbuilder.model;

import java.util.Map;

/**
 * 渲染/调用指定版本模板所需的变量。
 *
 * @author flywhl
 */
public record RenderRequest(Map<String, Object> params) {
}
