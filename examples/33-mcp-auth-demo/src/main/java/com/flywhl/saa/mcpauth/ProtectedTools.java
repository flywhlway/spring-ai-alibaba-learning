package com.flywhl.saa.mcpauth;

import io.modelcontextprotocol.common.McpTransportContext;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

/**
 * 受保护资源：Token 仅来自传输层 {@link McpTransportContext}，不接受模型参数中的 token。
 *
 * @author flywhl
 */
@Service
public class ProtectedTools {

    /** 演示用固定密钥，生产环境须替换为真实凭证校验。 */
    static final String DEMO_BEARER = "Bearer demo-secret";

    @McpTool(description = "访问受保护资源，需在 HTTP Authorization 头携带 Bearer Token")
    public String accessProtectedResource(McpSyncRequestContext requestContext) {
        McpTransportContext context = requestContext.transportContext();
        Object authorization = context == null ? null : context.get(McpAuthConfig.AUTHORIZATION_KEY);

        if (authorization == null || authorization.toString().isBlank()) {
            return "鉴权失败：缺少 Authorization 头，请使用 Authorization: Bearer demo-secret";
        }
        if (!DEMO_BEARER.equals(authorization.toString())) {
            return "鉴权失败：Token 无效，期望 Authorization: Bearer demo-secret";
        }
        return "Successfully accessed protected resource.";
    }
}
