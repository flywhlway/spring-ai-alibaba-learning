---
phase: 03-48-demo
plan: 08
subsystem: examples-batch2-gate
tags: [compile-gate, convention-scan, demos-20-34, batch-2]

requires:
  - phase: 03-48-demo
    provides: demos 20-21 structured output（03-04）
  - phase: 03-48-demo
    provides: demos 22-26 embedding/vector（03-05）
  - phase: 03-48-demo
    provides: demos 27-30 RAG（03-06）
  - phase: 03-48-demo
    provides: demos 31-34 MCP（03-07）
provides:
  - Batch 2（20~34）compile 硬门禁全绿（D-17）
  - 约定扫描通过（无密钥/废弃 API/TODO，端口 18020~18034/18134）
affects: [gsd-verify-work, phase-3-batch-3, UAT]

tech-stack:
  added: []
  patterns:
    - 次批硬门禁：common install → 16 pom 逐个 compile → 约定扫描
    - 门禁零修复即绿，不触碰 01~19

key-files:
  created:
    - .planning/phases/03-48-demo/03-08-SUMMARY.md
  modified: []

key-decisions:
  - "门禁全绿无需代码修复，Task 1/2 无源码 commit"
  - "D-10：20~34 仅依赖 saa-learning-common，未强制引入 starter"
  - "D-19：examples 保持独立应用，未挂父 POM modules"

patterns-established:
  - "Batch gate：mvn -pl common -am install + for-each mvn -f examples/NN compile"
  - "约定扫描：废弃 API / 2.0 MCP 包 / TODO / 硬编码密钥 / server.port"

requirements-completed: [REQ-phase-3-demos]

duration: 3min
completed: 2026-07-04
---

# Phase 03 Plan 08: Batch 2 编译门禁（20–34）Summary

**次批硬门禁全绿：16 个 pom（20~34）compile 通过，约定扫描零违规，端口 18020~18034/18134 正确**

## Performance

- **Duration:** 3 min
- **Started:** 2026-07-04T14:56:39Z
- **Completed:** 2026-07-04T14:59:30Z
- **Tasks:** 2/2
- **Files modified:** 0（源码零改动）

## Accomplishments

- `mvn -pl common -am -DskipTests install` 成功
- 16 个独立工程 `mvn -f ... compile` 全部退出码 0
- 约定扫描：无硬编码密钥、无废弃 API、无 2.0 MCP 包、无 TODO/FIXME
- 端口确认：20~33 → 180NN；34 server 18034、client 18134
- 未触碰 demos 01~19

## Compile Results

| Demo | Path | Result |
|------|------|--------|
| 20 | examples/20-structured-output-demo | PASS |
| 21 | examples/21-json-schema-demo | PASS |
| 22 | examples/22-embedding-demo | PASS |
| 23 | examples/23-pgvector-demo | PASS |
| 24 | examples/24-milvus-demo | PASS |
| 25 | examples/25-redis-vector-demo | PASS |
| 26 | examples/26-es-hybrid-demo | PASS |
| 27 | examples/27-rag-demo | PASS |
| 28 | examples/28-advanced-rag-demo | PASS |
| 29 | examples/29-hybrid-rag-demo | PASS |
| 30 | examples/30-rag-eval-demo | PASS |
| 31 | examples/31-mcp-server-demo | PASS |
| 32 | examples/32-mcp-client-demo | PASS |
| 33 | examples/33-mcp-auth-demo | PASS |
| 34-server | examples/34-mcp-nacos-demo/order-mcp-server | PASS |
| 34-client | examples/34-mcp-nacos-demo/office-assistant-client | PASS |

**FAIL_COUNT=0**

## Convention Scan Results

| Check | Result |
|-------|--------|
| 硬编码密钥（`sk-…` / 字面量 api-key） | PASS（无命中；api-key 均为 `${AI_DASHSCOPE_API_KEY}`） |
| `CallAroundAdvisor` / `PromptChatMemoryAdvisor` / `FunctionCallback` / `AdvisedRequest` / `AdvisedResponse` | PASS（无命中） |
| `org.springframework.ai.mcp.annotation`（2.0 包） | PASS（无命中） |
| `TODO` / `FIXME` / `请自行补充` | PASS（无命中） |
| server.port 18020~18034 | PASS |
| 34 client port 18134 | PASS |
| D-10 未强制 starter | PASS（仅 `saa-learning-common`） |
| D-19 examples 不挂父 POM | PASS |

### Ports

| Demo | Port |
|------|------|
| 20 | 18020 |
| 21 | 18021 |
| 22 | 18022 |
| 23 | 18023 |
| 24 | 18024 |
| 25 | 18025 |
| 26 | 18026 |
| 27 | 18027 |
| 28 | 18028 |
| 29 | 18029 |
| 30 | 18030 |
| 31 | 18031 |
| 32 | 18032 |
| 33 | 18033 |
| 34 server | 18034 |
| 34 client | 18134 |

## Task Commits

Each task was committed atomically:

1. **Task 1: common install + 20~34 全量 compile** — _(no commit; gate pass, 零修复)_
2. **Task 2: 约定扫描（密钥/废弃 API/TODO）** — _(no commit; scan pass, 零修复)_

**Plan metadata:** _(见下方 docs commit)_

## Files Created/Modified

- `.planning/phases/03-48-demo/03-08-SUMMARY.md` — 门禁结果与扫描报告

## Decisions Made

- 门禁全绿无需代码修复，Task 1/2 无源码 commit
- D-10：20~34 仅依赖 `saa-learning-common`，未强制引入 `saa-learning-starter`
- D-19：examples 保持独立应用，未挂父 POM modules

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required for the compile gate.
（真机 UAT 仍需 `AI_DASHSCOPE_API_KEY` 与对应中间件 profile。）

## Next Phase Readiness

- Batch 2（20~34）达到与 Batch 1 同等的 compile 验收水位（D-17）
- 可进入 `/gsd-verify-work` 或真机 curl UAT
- 无阻塞项

## Self-Check: PASSED

- [x] `03-08-SUMMARY.md` 存在
- [x] 16/16 compile PASS（本会话实测）
- [x] 约定扫描全 PASS（本会话实测）
- [x] 未修改 demos 01~19
- [x] 未修改 STATE.md / ROADMAP.md（由 orchestrator 更新）

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
