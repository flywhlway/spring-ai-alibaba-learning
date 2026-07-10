package com.flywhl.saa.smartcs.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * @Tool 方法权限支持：从 {@link ToolContext} 或当前 {@code SecurityContext} 解析 userId/role，
 * 供各 Tool 类做 {@code requireRole} 越权校验（威胁登记 T-06-06）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public final class ToolSecuritySupport {

    private ToolSecuritySupport() {
    }

    public static String roleOf(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext().containsKey("role")) {
            return String.valueOf(toolContext.getContext().get("role"));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object role = jwtAuth.getToken().getClaim("role");
            if (role != null) {
                return String.valueOf(role);
            }
        }
        return "CUSTOMER";
    }

    public static Long userIdOf(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext().containsKey("userId")) {
            Object v = toolContext.getContext().get("userId");
            if (v instanceof Number n) {
                return n.longValue();
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object uid = jwt.getClaim("uid");
            if (uid instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }

    /**
     * @return 命中的角色；越权时返回 {@code null}
     */
    public static String requireRole(ToolContext ctx, String... allowed) {
        String role = roleOf(ctx);
        for (String a : allowed) {
            if (a.equals(role)) {
                return role;
            }
        }
        return null;
    }
}
