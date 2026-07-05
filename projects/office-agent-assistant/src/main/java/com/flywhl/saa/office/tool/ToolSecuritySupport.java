package com.flywhl.saa.office.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class ToolSecuritySupport {
    private ToolSecuritySupport() {}
    public static String roleOf(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext().containsKey("role")) {
            return String.valueOf(toolContext.getContext().get("role"));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object role = jwtAuth.getToken().getClaim("role");
            if (role != null) return String.valueOf(role);
        }
        return "EMPLOYEE";
    }
    public static Long userIdOf(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext().containsKey("userId")) {
            Object v = toolContext.getContext().get("userId");
            if (v instanceof Number n) return n.longValue();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object uid = jwt.getClaim("uid");
            if (uid instanceof Number n) return n.longValue();
        }
        return null;
    }
    public static String requireRole(ToolContext ctx, String... allowed) {
        String role = roleOf(ctx);
        for (String a : allowed) {
            if (a.equals(role)) return role;
        }
        return null;
    }
}

