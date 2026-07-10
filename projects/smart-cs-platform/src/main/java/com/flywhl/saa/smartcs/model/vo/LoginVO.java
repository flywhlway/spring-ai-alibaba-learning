package com.flywhl.saa.smartcs.model.vo;

/**
 * 登录响应 VO：签发的 JWT 与用户信息。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record LoginVO(
        String accessToken,
        long expiresIn,
        UserVO user) {
}
