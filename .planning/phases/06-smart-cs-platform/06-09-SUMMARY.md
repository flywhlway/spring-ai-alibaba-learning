---
phase: 06-smart-cs-platform
plan: 09
subsystem: rag
tags: [query-rewrite, prompt-template, spring-ai, gap-closure]

requires:
  - phase: 06-smart-cs-platform
    provides: scs-db-init 正确挂载（06-08）与 RagPipelineFactory/PromptTemplateProvider
provides:
  - query-rewrite 含 {target}+{query}（classpath + DB 种子 + 幂等 UPDATE）
  - README 已有库冷启动排障
  - actuator/health UP 冷启动验证
affects: [uat-smart-cs, phase-6-human-uat]

tech-stack:
  added: []
  patterns: [promptassert-target-query, idempotent-prompt-update]

key-files:
  created: []
  modified:
    - projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st
    - projects/smart-cs-platform/db/data.sql
    - projects/smart-cs-platform/src/test/resources/db/data.sql
    - projects/smart-cs-platform/README.md

key-decisions:
  - "仅改模板正文，不改 RagPipelineFactory 注入；DB PUBLISHED 优先故必须种子+幂等 UPDATE"

patterns-established:
  - "RewriteQueryTransformer 模板必须同时含 {target} 与 {query}"

requirements-completed:
  - REQ-phase-6-smart-cs

duration: 12min
completed: 2026-07-17
---

# Phase 06: Plan 09 Summary

**关闭 UAT Gap：query-rewrite 补齐 Spring AI `RewriteQueryTransformer` 强制占位符 `{target}`+`{query}`；冷启动 `actuator/health` 为 UP。**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-17T16:07:00Z
- **Completed:** 2026-07-17T16:12:00Z
- **Tasks:** 2/2
- **Files modified:** 4

## Accomplishments
- classpath `.st`、主库/测试 `data.sql` INSERT 统一为含 `{target}`+`{query}` 的中文模板
- 追加幂等 `UPDATE ... WHERE content NOT LIKE '%{target}%'`，修复已有库 ON CONFLICT 不刷新问题
- README §4.2 排障：volume 路径、重跑 init、health 检查
- Runtime Health Gate：force-recreate scs-db-init → spring-boot:run → `{"status":"UP"}`；无 `placeholders ... target` 错误

## Task Commits

1. **Task 1: 补齐 classpath 与种子库 query-rewrite 的 {target}+{query}** - `1a530b8` (fix)
2. **Task 2: README 已有库修复说明 + 冷启动门禁** - `ee7284c` (docs)

## Files Created/Modified
- `projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st` - 合规模板
- `projects/smart-cs-platform/db/data.sql` - INSERT + 幂等 UPDATE
- `projects/smart-cs-platform/src/test/resources/db/data.sql` - 与主库一致
- `projects/smart-cs-platform/README.md` - 冷启动排障

## Decisions Made
- 不改 Java 注入路径；模板合规即可通过 PromptAssert

## Deviations from Plan
None

## Verification
- G-06-09-ph：三处均含 `{target}`+`{query}` — PASS
- G-06-09-upd：主/test data.sql 幂等 UPDATE — PASS
- G-06-09-compile：`mvn -DskipTests compile` — PASS
- G-06-09-health：Docker 可用，`curl .../actuator/health` → UP — PASS（硬门禁达成，非 residual）

## Self-Check: PASSED

- [x] key-files.modified 存在且含 `{target}`
- [x] `git log --grep=06-09` 有提交
- [x] health UP；日志无 target placeholder 错误
