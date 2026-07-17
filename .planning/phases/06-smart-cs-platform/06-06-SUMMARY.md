---
phase: 06-smart-cs-platform
plan: 06
subsystem: admin
tags: [nacos, model-profile, prompt, dashboard, prometheus, grafana, micrometer]

requires:
  - phase: 06-smart-cs-platform/06-05
    provides: Chat/Ticket/HITL API + cs_message 落库字段（route_agent/cache_hit/token）
provides:
  - model_profile CRUD + ConfigurableModelRouter scene 路由 + Nacos scs.model.profiles
  - Prompt CRUD/publish → spring.ai.alibaba.configurable.prompt
  - FAQ 后台列表/新建/reindex + Audit 查询
  - Dashboard stats + Prometheus/Grafana monitor profile 文档
affects:
  - 06-07 UAT

tech-stack:
  added: [prom/prometheus:v2.55.1, grafana/grafana:11.2.0]
  patterns:
    - Nacos publishConfig 骨架复用（Prompt + model_profile）
    - scene 驱动 model_profile → ChatModel Bean 映射，无匹配回退 FallbackModelRouter
    - Dashboard Token 成本 Micrometer 优先、DB 回退

key-files:
  created:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/ModelAdminController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/service/ModelAdminService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ConfigurableModelRouter.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ModelProfileNacosPublisher.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/prompt/PromptPublishService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/PromptAdminController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/FaqAdminController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/DashboardAdminController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/service/DashboardStatsService.java
    - projects/smart-cs-platform/monitor/prometheus.yml
  modified:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/AiClientConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/FaqEtlPipeline.java
    - projects/smart-cs-platform/docker-compose.override.yml
    - projects/smart-cs-platform/README.md

key-decisions:
  - "ModelAdmin 双路径 /api/admin/models 与 /api/admin/model-profiles，对齐计划与 Wave 0 api.http"
  - "AiClientConfig 默认 ChatClient.Builder 使用 FAQ scene 路由"
  - "monitor profile pin Prometheus v2.55.1 / Grafana 11.2.0（T-06-SC）"

patterns-established:
  - "后台发布：DB 变更 → Nacos publishConfig → audit_log"
  - "运营看板：JPA 聚合 + MeterRegistry gen_ai.client.token.usage"

requirements-completed: [REQ-phase-6-smart-cs]

duration: 6min
completed: 2026-07-17
---

# Phase 06 Plan 06: Admin/Ops 后台 + 可观测 Summary

**ADMIN model_profile/Prompt/FAQ/Dashboard CRUD 与 Nacos 热更新，外加 Prometheus/Grafana monitor profile 文档**

## Performance

- **Duration:** 6 min
- **Started:** 2026-07-17T14:21:43Z
- **Completed:** 2026-07-17T14:27:42Z
- **Tasks:** 3
- **Files modified:** 29

## Accomplishments

- model_profile CRUD + `ConfigurableModelRouter` 按 scene 选 DashScope/DeepSeek，推送 `scs.model.profiles`
- Prompt 发布推送 `spring.ai.alibaba.configurable.prompt`；FAQ 后台支持 reindex；审计分页查询
- Dashboard `/api/admin/dashboard/stats` 聚合工单/缓存命中/route_agent/token 成本；compose `--profile monitor` + README 监控节

## Task Commits

Each task was committed atomically:

1. **Task 1: ModelAdmin + ConfigurableModelRouter + Nacos 发布** - `ab906c7` (feat)
2. **Task 2: PromptAdmin + FaqAdmin + PromptPublishService** - `0fe59a6` (feat)
3. **Task 3: Dashboard + Prometheus/Grafana 文档** - `f53c9e2` (feat)

**Plan metadata:** （本提交）

## Files Created/Modified

- `admin/controller/ModelAdminController.java` - models + model-profiles CRUD/publish
- `admin/service/ModelAdminService.java` - 保存后推 Nacos + audit
- `config/ConfigurableModelRouter.java` - scene → ChatModel，回退 ModelRouter
- `config/ModelProfileNacosPublisher.java` - Data ID `scs.model.profiles`
- `prompt/PromptPublishService.java` - DRAFT→PUBLISHED + Nacos Prompt
- `admin/*` Prompt/Faq/Audit/Dashboard Controllers & Services
- `rag/FaqEtlPipeline.java` - `reindexArticle` / `deleteIndex`
- `docker-compose.override.yml` + `monitor/prometheus.yml` - monitor profile
- `README.md` - 监控节（datasource / 示例指标）

## Decisions Made

- Controller 同时映射 `/api/admin/models` 与 `/api/admin/model-profiles`，兼顾计划契约与 api.http
- 默认 ChatClient.Builder 走 FAQ scene；其它 scene 经 `routeForScene`
- Grafana 依赖 Prometheus；镜像 pin LTS tag

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] ModelAdmin 双路径映射**
- **Found during:** Task 1
- **Issue:** 计划写 `/api/admin/models`，Wave 0 `api.http`/README 写 `/api/admin/model-profiles`
- **Fix:** `@RequestMapping` 同时绑定两条路径
- **Files modified:** ModelAdminController.java
- **Verification:** compile 绿
- **Committed in:** ab906c7

**2. [Rule 2 - Missing Critical] AuditLogService + AuditAdmin**
- **Found during:** Task 1/2（威胁 T-06-10）
- **Issue:** 发布需写 audit_log，仓库尚无 AuditLogService
- **Fix:** 新增 AuditLogService + 可选 AuditAdminController
- **Files modified:** AuditLogService.java, AuditAdminController.java, AuditQueryService.java
- **Verification:** compile 绿
- **Committed in:** ab906c7 / 0fe59a6

---

**Total deviations:** 2 auto-fixed (2 missing critical)
**Impact on plan:** 对齐既有 HTTP 契约与威胁缓解，无范围膨胀

## Issues Encountered

None

## User Setup Required

Nacos（`--profile cloud`）需可达方可真机验证 Prompt/model 发布；监控可选 `--profile monitor`。密钥仍经 `AI_DASHSCOPE_API_KEY`。

## Next Phase Readiness

- 运营后台与可观测文档就绪，可供 06-07 全量 UAT
- 无阻塞项

## Self-Check: PASSED

- FOUND: DashboardStatsService.java, ModelAdminController.java, PromptPublishService.java, ModelProfileNacosPublisher.java
- FOUND commits: ab906c7, 0fe59a6, f53c9e2
- FOUND: `scs.model.profiles`, `spring.ai.alibaba.configurable.prompt`, `gen_ai.client.token.usage`, README prometheus

---
*Phase: 06-smart-cs-platform*
*Completed: 2026-07-17*
