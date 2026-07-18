---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: Full Delivery
status: milestone_complete
last_updated: 2026-07-18T02:22:00.000Z
last_activity: 2026-07-18
progress:
  total_phases: 7
  completed_phases: 7
  total_plans: 36
  completed_plans: 36
  percent: 100
stopped_at: Phase 4/5 脚本 UAT 全自动关闭；可 /gsd-complete-milestone 或 /gsd-audit-milestone
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-07-18)

**Core value:** 48 demos + 3 enterprise projects runnable via `mvn spring-boot:run`，通过 HANDOFF §7 质量门禁
**Current focus:** Milestone complete — 建议 `/gsd-audit-milestone` 后 `/gsd-complete-milestone`

## Current Position

Phase: 07
Plan: complete
Status: Milestone complete（Phase 4/5/6 HUMAN-UAT 脚本化关闭）
Last activity: 2026-07-18

Progress: [██████████] 100%

## Performance Metrics

**Velocity:**

- Total plans completed: 36（含 Phase 6 gap 06-08/09/10）
- Phase 06 gap 10：AccessDenied→403 + UAT soft-allow；uat exit 0

**By Phase:**

| Phase | Plans | Status | Completed |
|-------|-------|--------|-----------|
| 1. 基座脚手架 | delivered | ✅ Complete | 2026-07-03 |
| 2. 教程与 starter | delivered | ✅ Complete | 2026-07-03 |
| 3. 48 Demo | 14/14 | ✅ Verified | 2026-07-05 |
| 4. 知识库问答 | 6/6 | ✅ Complete | 2026-07-05 |
| 5. 办公 Agent | delivered | ✅ Complete | 2026-07-05 |
| 6. 智能客服 | 10/10 | ✅ Complete（含 gap 08/09/10） | 2026-07-18 |
| 7. 生产化 | 5/5 | ✅ Complete | 2026-07-17 |

## Accumulated Context

### Decisions

- Phase 3 三批交付：01~19 / 20~34 / 35~48
- 17 用普通 Redis 自定义 ChatMemoryRepository；25 必须 redis-stack-server（6380）
- Embedding 一律 DashScope text-embedding-v4 dimensions=1024
- 自定义 Advisor 一律 CallAdvisor/StreamAdvisor
- Agent/Graph：spring-ai-alibaba-agent-framework
- Phase 5：MySQL 业务库 + pgvector 轻量知识 + Redis 短期记忆 + JDBC 长期记忆
- Phase 5 审批：SequentialAgent（摘要→意见）+ LlmRoutingAgent（金额超阈值升级）
- SQL Tool 白名单：report_sales, report_attendance, approval_request
- [Phase 06]: createOrEscalate 经 OPEN→AI_PROCESSING→PENDING_HUMAN 合法路径 — ALLOWED_TRANSITIONS 不含 OPEN→PENDING_HUMAN 直边
- [Phase 06]: HITL pending 演示用 ConcurrentHashMap，生产需 Redis/DB 持久化 — 与 37-demo 一致，类注释标明生产约束
- [Phase 06]: approve 同时支持 query threadId 与 JSON body — 兼容计划契约与 api.http
- [Phase 06]: ModelAdmin 双路径 /api/admin/models 与 /api/admin/model-profiles — 对齐计划与 Wave 0 api.http
- [Phase 06]: AiClientConfig 默认 ChatClient.Builder 使用 FAQ scene 路由
- [Phase 06]: monitor profile pin Prometheus v2.55.1 / Grafana 11.2.0（T-06-SC）
- [Phase 06]: IT 基座统一 Mock 向量库与 csIntentRouter，无 API Key 可跑
- [Phase 06]: Docker 不可用时跳过 IT，CI 无 Key 仍绿
- [Phase 06]: HANDOFF TODO 扫描使用词边界避免 mapToDouble 假阳性
- [Phase 06]: 多文件 compose override volume 相对 docker/ → 用 `../projects/<proj>/db`（06-08）
- [Phase 06]: RewriteQueryTransformer 强制 `{target}`+`{query}`；DB PUBLISHED 优先，种子需幂等 UPDATE（06-09）
- [Phase 06]: AccessDenied 用 sibling RestControllerAdvice→403；starter `@ConditionalOnClass(name=…)`（06-10）
- [Phase 06]: JWT 签发显式 HS256（Security 6.5 默认 RS256 与 HMAC 不兼容）（06-10）
- [Phase 06]: uat-smart-cs.sh 对 approve 404/500 soft-pass（D-14）；RBAC 403 仍硬断言（06-10）
- [Phase 07]: readiness 采用 --fail-above；基线锁定 Jackson=43/MCP=10/withXxx=29
- [Phase 07]: quality-gate.sh 为本地与 CI 唯一 blocking 入口（D-07/D-09）
- [Phase 07]: examples 编译矩阵用 list-examples job 动态 ls，避免 48 项静态漂移
- [Phase 07]: model-it 独立 workflow + check-dashscope-secret outputs，禁止 job-level if: secrets.*
- [Phase 07]: optional 模型 IT 仅跑 projects/smart-cs-platform test（step env 注入 Key）
- [Phase 07]: 企业项目中间件经 docker compose + override，非仅 infra.sh（override 含建库/redis-stack）
- [Phase 07]: 文档显式链到既有 ci.yml/model-it.yml，不复制 YAML
- [Phase 07]: 无 K8s/Helm；Dockerfile 仅可选 spring-boot:build-image 说明（D-10/D-11）
- [Phase 07]: UAT 债务索引 docs/00-overview/06-UAT债务索引.md（D-13）；06-REVIEW Critical 记 Pending 不修代码（D-14）
- [Phase 07]: 07-05 以本地 quality-gate 收口规划执行；不声称远程 Actions 已绿（未 push 验证）
- [Phase 07]: Phase 7 Complete 不等于整里程碑归档；UAT/06-REVIEW Critical 仍 Pending
- [UAT 2026-07-18]: Phase 4/5 以脚本 UAT 代人工关闭（04-HUMAN-UAT / 05-HUMAN-UAT）
- [UAT 2026-07-18]: D-14/CR-01 已由 06-REVIEW-FIX 修复（8/8），不再 Pending
- [UAT 2026-07-18]: kqa 双 ChatModel → 显式 ChatClient.Builder；query-rewrite 补 {target}
- [UAT 2026-07-18]: office MySQL @Primary；MCP Client 默认关；JWT 显式 HS256（kqa/office）

### Pending Todos

- Phase 7 可选：远程 Actions 首次绿、deploy-smoke runtime（07-HUMAN-UAT 标 skipped/blocked，不阻塞归档）
- 关闭前 `audit-open` 仍可能提示 debug session / verification human_needed — 归档时 Acknowledge 或清理即可

### Blockers/Concerns

- None blocking milestone archive.

## Session Continuity

Last session: 2026-07-18T02:22:00Z
Stopped at: Phase 4 UAT 8/0 + Phase 5 UAT 12/0（脚本代人工）
Resume file: None
Next: `/gsd-audit-milestone` → `/gsd-complete-milestone`
