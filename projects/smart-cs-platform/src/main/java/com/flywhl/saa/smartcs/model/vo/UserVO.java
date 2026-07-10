package com.flywhl.saa.smartcs.model.vo;

/**
 * 用户信息 VO（登录响应 / 当前用户查询共用）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record UserVO(
        Long id,
        String username,
        String displayName,
        String role,
        boolean enabled) {
}
