package com.flywhl.saa.smartcs.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.alibaba.cloud.ai.graph.agent.tools.ToolContextHelper;
import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;

/**
 * @Tool 方法权限支持：从 {@link ToolContext}、{@link RunnableConfig} metadata 或当前
 * {@code SecurityContext} 解析 userId/role，供各 Tool 做 {@code requireRole} 越权校验
 * （威胁登记 T-06-06）。缺少身份时拒绝，禁止默认为 CUSTOMER。
 *
 * @author flywhl
 * @since 1.0.0
 */
public final class ToolSecuritySupport {

    public static final String META_USER_ID = "userId";
    public static final String META_ROLE = "role";
    public static final String META_CONVERSATION_ID = "conversationId";

    private ToolSecuritySupport() {
    }

    public static String roleOf(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext().containsKey(META_ROLE)) {
            Object role = toolContext.getContext().get(META_ROLE);
            if (role != null && !String.valueOf(role).isBlank()) {
                return String.valueOf(role);
            }
        }
        Object fromConfig = metadataFromConfig(toolContext, META_ROLE);
        if (fromConfig != null && !String.valueOf(fromConfig).isBlank()) {
            return String.valueOf(fromConfig);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object role = jwtAuth.getToken().getClaim("role");
            if (role != null && !String.valueOf(role).isBlank()) {
                return String.valueOf(role);
            }
        }
        throw new BizException(CommonResultCode.UNAUTHORIZED, "Tool 调用缺少身份上下文");
    }

    public static Long userIdOf(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext().containsKey(META_USER_ID)) {
            Long parsed = toLong(toolContext.getContext().get(META_USER_ID));
            if (parsed != null) {
                return parsed;
            }
        }
        Long fromConfig = toLong(metadataFromConfig(toolContext, META_USER_ID));
        if (fromConfig != null) {
            return fromConfig;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Long uid = toLong(jwt.getClaim("uid"));
            if (uid != null) {
                return uid;
            }
        }
        throw new BizException(CommonResultCode.UNAUTHORIZED, "Tool 调用缺少 userId");
    }

    /**
     * 优先取服务端注入的 conversationId（RunnableConfig/ToolContext），避免模型伪造。
     */
    public static String conversationIdOf(ToolContext toolContext, String modelProvided) {
        if (toolContext != null && toolContext.getContext().containsKey(META_CONVERSATION_ID)) {
            Object v = toolContext.getContext().get(META_CONVERSATION_ID);
            if (v != null && !String.valueOf(v).isBlank()) {
                return String.valueOf(v).trim();
            }
        }
        Object fromConfig = metadataFromConfig(toolContext, META_CONVERSATION_ID);
        if (fromConfig != null && !String.valueOf(fromConfig).isBlank()) {
            return String.valueOf(fromConfig).trim();
        }
        if (modelProvided != null && !modelProvided.isBlank()) {
            return modelProvided.trim();
        }
        throw new BizException(CommonResultCode.BAD_REQUEST, "Tool 调用缺少 conversationId");
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

    private static Object metadataFromConfig(ToolContext toolContext, String key) {
        return ToolContextHelper.getConfig(toolContext)
                .flatMap(config -> config.metadata(key))
                .orElse(null);
    }

    private static Long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
