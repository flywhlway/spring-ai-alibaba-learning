---
phase: 04-knowledge-qa-platform
plan: 05
subsystem: admin
tags: [admin-api, nacos, minio, audit-log, tool-security, dashboard]

requires:
  - phase: 04-04
    provides: 问答域 Auth/Qa/Conversation/Feedback API 与 JWT RBAC
provides:
  - 五组 admin API（文档/Prompt/用户/审计/看板）
  - PromptPublishService Nacos 热更新推送
  - AuditLogService + DbAuditLoggingAdvisor 双轨审计
  - KnowledgeOpsTools ADMIN @Tool 组件
affects:
  - 04-06

tech-stack:
  added: [nacos-client via starter, MinIO PutObject/RemoveObject]
  patterns: [AuditLogService 落库, DbAuditLoggingAdvisor 包装 starter 审计, ToolContext role 校验]

key-files:
  created:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/service/AuditLogService.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/DbAuditLoggingAdvisor.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/vo/AuditLogVO.java
  modified:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/admin/controller/*
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/admin/service/*
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/prompt/PromptPublishService.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/tool/KnowledgeOpsTools.java

key-decisions:
  - "DbAuditLoggingAdvisor 委托 AuditLoggingAdvisor 处理日志，额外落库 AI_CALL"
  - "Prompt 发布推送 Nacos 全量 PUBLISHED JSON 数组，单版本发布时归档同 key 旧 PUBLISHED"
  - "Dashboard 成本优先读 gen_ai.client.token.usage 指标，无指标时回退 qa_message token 估算"

patterns-established:
  - "后台 API 统一 @PreAuthorize hasRole ADMIN + Result/PageResult 响应"
  - "KnowledgeOpsTools roleOf 模式对齐 15-tool-security-demo"

requirements-completed: [REQ-phase-4-knowledge-qa]

duration: 22min
completed: 2026-07-05
---

# Phase 04 Plan 05: Admin 五组 API Summary

**后台五组 REST API + Nacos Prompt 发布 + audit_log 双轨审计 + ADMIN 专属 KnowledgeOpsTools**

## Performance

- **Duration:** 22 min
- **Started:** 2026-07-05T15:14:00Z
- **Completed:** 2026-07-05T15:36:00Z
- **Tasks:** 3
- **Files modified:** 21

## Accomplishments

- DocumentAdmin：multipart 上传 MinIO、分页列表、reindex、级联删除向量与 kb_chunk
- PromptAdmin：版本化 CRUD、publish 推 Nacos `spring.ai.alibaba.configurable.prompt`
- UserAdmin / AuditAdmin / DashboardAdmin 全量实现，`api.http` admin 段路径全覆盖
- AuditLogService 记录 LOGIN/UPLOAD/DELETE/PUBLISH/AI_CALL 等；DbAuditLoggingAdvisor 替换 ChatClient 审计链
- KnowledgeOpsTools：countDocuments、getDocumentStatus、triggerReindex，ToolContext ADMIN 校验

## Task Commits

1. **Task 1: DocumentAdmin + PromptAdmin** - `7b4a300` (feat)
2. **Task 2: UserAdmin + AuditAdmin + DashboardAdmin** - `4fc3e8b` (feat)
3. **Task 3: KnowledgeOpsTools** - `8c24519` (feat)

## Files Created/Modified

- `admin/controller/DocumentAdminController.java` - `/api/admin/documents` CRUD + reindex
- `admin/service/DocumentAdminService.java` - MinIO 上传 + ETL 编排 + 审计
- `admin/controller/PromptAdminController.java` - Prompt 列表/新建/发布
- `prompt/PromptPublishService.java` - Nacos publishConfig 封装
- `admin/service/DashboardStatsService.java` - 问答量/成本/满意度/知识规模聚合
- `config/DbAuditLoggingAdvisor.java` - AI 调用 audit_log 落库
- `tool/KnowledgeOpsTools.java` - 管理 @Tool 三方法

## Decisions Made

- DbAuditLoggingAdvisor 组合 starter AuditLoggingAdvisor，避免重复 Advisor 链调用逻辑
- Prompt 发布失败抛 BizException，不静默降级（保证 D-21 热更新可观测）
- Dashboard 成本双源：Micrometer gen_ai 指标优先，qa_message token 字段兜底

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

Nacos 需运行（`bash scripts/infra.sh up cloud`）才能验证 Prompt 发布热更新；其余 admin API 仅需 PostgreSQL/Redis/Milvus/MinIO 常规栈。

## Next Phase Readiness

- 04-06 可启动：单测 + Testcontainers + HANDOFF §7 门禁 + curl UAT
- KnowledgeOpsTools 未挂载 QaService ChatClient（计划标注可选），04-06 可按需补充

## Self-Check: PASSED

- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/prompt/PromptPublishService.java
- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/DbAuditLoggingAdvisor.java
- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/tool/KnowledgeOpsTools.java
- FOUND: commit 7b4a300
- FOUND: commit 4fc3e8b
- FOUND: commit 8c24519

---
*Phase: 04-knowledge-qa-platform*
*Completed: 2026-07-05*
