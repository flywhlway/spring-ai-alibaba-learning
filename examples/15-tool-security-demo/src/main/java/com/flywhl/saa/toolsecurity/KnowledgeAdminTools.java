package com.flywhl.saa.toolsecurity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 演示工具调用的权限边界：查询类工具默认放开，
 * 变更类工具强制要求 {@link ToolContext} 携带的角色为 ADMIN，否则拒绝执行。
 *
 * <p>每次调用无论成功与否都写一条审计日志（调用方角色、工具名、参数摘要、
 * 结果状态、耗时），对应 README「Tool 权限控制与调用审计」的第二个演示要点。
 *
 * @author flywhl
 */
@Component
public class KnowledgeAdminTools {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    @Tool(description = "查询知识库文档数量")
    public int countDocuments(ToolContext toolContext) {
        long start = System.currentTimeMillis();
        String role = roleOf(toolContext);
        int result = 128; // 示例返回值
        audit(role, "countDocuments", "-", "OK", System.currentTimeMillis() - start);
        return result;
    }

    @Tool(description = "删除指定 ID 的知识库文档，仅管理员可执行", returnDirect = true)
    public String deleteDocument(@ToolParam(description = "文档 ID") String docId, ToolContext toolContext) {
        long start = System.currentTimeMillis();
        String role = roleOf(toolContext);

        if (!"ADMIN".equals(role)) {
            // 不抛异常中断整个链路，而是把"权限不足"作为工具结果返回，
            // 由模型用自然语言告知用户——用户体验更友好
            String denied = "权限不足：删除操作仅管理员可执行，当前角色为 " + role;
            audit(role, "deleteDocument", docId, "DENIED", System.currentTimeMillis() - start);
            return denied;
        }

        // 真实删除逻辑（示例中省略数据库操作）
        String result = "文档 " + docId + " 已删除";
        audit(role, "deleteDocument", docId, "OK", System.currentTimeMillis() - start);
        return result;
    }

    private String roleOf(ToolContext toolContext) {
        return (String) toolContext.getContext().getOrDefault("role", "USER");
    }

    private void audit(String role, String toolName, String argsSummary, String status, long costMs) {
        AUDIT_LOG.info("[audit] role={} tool={} args={} status={} cost={}ms", role, toolName, argsSummary, status, costMs);
    }
}
