package com.flywhl.saa.knowledgeqa.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.flywhl.saa.knowledgeqa.rag.DocumentEtlPipeline;
import com.flywhl.saa.knowledgeqa.rag.IngestStatusTracker;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;

/**
 * 管理类 @Tool 工具族：知识库统计、文档状态查询、触发重建索引（仅 ADMIN 经 ToolContext 校验）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class KnowledgeOpsTools {

    private final KbDocumentRepository documentRepository;
    private final DocumentEtlPipeline documentEtlPipeline;
    private final IngestStatusTracker statusTracker;

    public KnowledgeOpsTools(
            KbDocumentRepository documentRepository,
            DocumentEtlPipeline documentEtlPipeline,
            IngestStatusTracker statusTracker) {
        this.documentRepository = documentRepository;
        this.documentEtlPipeline = documentEtlPipeline;
        this.statusTracker = statusTracker;
    }

    @Tool(description = "查询知识库文档总数")
    public long countDocuments(ToolContext toolContext) {
        if (!isAdmin(toolContext)) {
            return -1;
        }
        return documentRepository.count();
    }

    @Tool(description = "查询指定文档的入库状态（UPLOADED/PARSING/INDEXED/FAILED）")
    public String getDocumentStatus(@ToolParam(description = "文档 ID") Long documentId, ToolContext toolContext) {
        if (!isAdmin(toolContext)) {
            return deniedMessage(toolContext);
        }
        if (documentId == null) {
            return "请提供有效的文档 ID";
        }
        String status = statusTracker.getStatus(documentId);
        if (status == null) {
            return "文档 " + documentId + " 不存在";
        }
        return "文档 " + documentId + " 当前状态: " + status;
    }

    @Tool(description = "触发指定文档的重建索引，仅管理员可执行", returnDirect = true)
    public String triggerReindex(@ToolParam(description = "文档 ID") Long documentId, ToolContext toolContext) {
        if (!isAdmin(toolContext)) {
            return deniedMessage(toolContext);
        }
        if (documentId == null || !documentRepository.existsById(documentId)) {
            return "文档 " + documentId + " 不存在";
        }
        documentEtlPipeline.reindex(documentId);
        return "已触发文档 " + documentId + " 的重建索引任务";
    }

    private static boolean isAdmin(ToolContext toolContext) {
        return "ADMIN".equals(roleOf(toolContext));
    }

    private static String roleOf(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return "USER";
        }
        Object role = toolContext.getContext().get("role");
        return role != null ? role.toString() : "USER";
    }

    private static String deniedMessage(ToolContext toolContext) {
        return "权限不足：该操作仅管理员可执行，当前角色为 " + roleOf(toolContext);
    }
}
