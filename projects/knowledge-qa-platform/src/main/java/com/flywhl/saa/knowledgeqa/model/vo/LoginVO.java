package com.flywhl.saa.knowledgeqa.model.vo;

/**
 * 登录响应 VO：JWT + 当前用户概要。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record LoginVO(String accessToken, UserVO user) {
}
