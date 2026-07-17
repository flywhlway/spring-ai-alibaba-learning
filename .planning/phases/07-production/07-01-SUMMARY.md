---
phase: 07-production
plan: 01
subsystem: infra
tags: [quality-gate, version-audit, spring-ai-2-readiness, bash, maven]

requires:
  - phase: 06-smart-cs-platform
    provides: 可扫描的 examples/projects Java 源码树与既有审计脚本
provides:
  - scripts/quality-gate.sh 统一 blocking 质量门禁入口
  - version-audit BOM 缺失 exit 1
  - spring-ai-2-readiness --fail-above 阈值模式
  - readiness 基线常量 43/10/29
affects: [07-02 CI workflows, 07-03 docs, Wave 2 CI]

tech-stack:
  added: [scripts/quality-gate.sh]
  patterns: [统一质量门禁入口, 基线阈值 fail-on-exceed, §7 fail-on-match 扫描]

key-files:
  created:
    - scripts/quality-gate.sh
  modified:
    - scripts/version-audit.sh
    - scripts/spring-ai-2-readiness.sh

key-decisions:
  - "readiness 采用 Option A：可选 --fail-above，默认无参仍只报告（保留第22章教学路径）"
  - "readiness 基线锁定为 Jackson=43 / MCP=10 / withXxx=29（超过才失败）"
  - "examples 编译抽样固定 01-quickstart-demo 与 35-agent-demo，用 mvn -f 而非 -pl"

patterns-established:
  - "Pattern: quality-gate.sh 为本地与 CI 唯一 blocking 入口（D-07/D-09）"
  - "Pattern: 复用 version-audit / readiness，仅硬化 exit 语义（D-08）"
  - "Pattern: §7 扫描 exclude setup-env.example.sh / target / .git / node_modules"

requirements-completed: []  # 阶段级 REQ-phase-7-production 待 07 全部 plans 完成后再勾选

duration: 3min
completed: 2026-07-17
---

# Phase 07 Plan 01: 质量门禁硬化 Summary

**统一 `quality-gate.sh` 入口：BOM 缺失与 readiness 超基线必失败，§7 废弃 API/TODO/sk- 扫描 fail-on-match**

## Performance

- **Duration:** 3 min
- **Started:** 2026-07-17T14:56:42Z
- **Completed:** 2026-07-17T14:59:31Z
- **Tasks:** 3/3
- **Files modified:** 3

## Accomplishments

- 硬化 `version-audit.sh`：任一 SAA BOM 缺失 → `exit 1`（消除假绿）
- 扩展 `spring-ai-2-readiness.sh`：可选 `--fail-above JACKSON,MCP,WITH`，默认无参仍只报告
- 新建 `scripts/quality-gate.sh`：common/starter install → examples 编译抽样 → audit → readiness 基线 → §7 扫描；本机全绿

## Task Commits

Each task was committed atomically:

1. **Task 1: 硬化 version-audit 与 readiness 退出语义** - `43f622c` (fix)
2. **Task 2: 新建 quality-gate.sh 并锁定 readiness 基线** - `b699cca` (feat)
3. **Task 3: 本地跑通 quality-gate 并修扫描误报** - _(无代码变更；`bash scripts/quality-gate.sh` exit 0，首次即绿)_

**Plan metadata:** `3b2aa64` (docs: complete plan); `c48b175` (docs: STATE/ROADMAP)

## Files Created/Modified

- `scripts/quality-gate.sh` - 统一质量门禁入口（blocking）
- `scripts/version-audit.sh` - BOM 缺失 exit 1
- `scripts/spring-ai-2-readiness.sh` - `--fail-above` 阈值失败模式

## Readiness Baseline (for CI / regression)

| Metric | Baseline |
|--------|----------|
| Jackson (`com.fasterxml.jackson`) files | **43** |
| MCP community (`org.springaicommunity.mcp`) files | **10** |
| `.withXxx()` files | **29** |

写入 `scripts/quality-gate.sh` 常量：`BASELINE_JACKSON_FILES` / `BASELINE_MCP_FILES` / `BASELINE_WITH_XXX_FILES`。

## Decisions Made

- readiness 选 Option A（`--fail-above`），改动小且可被 quality-gate 直接调用
- 基线取本机首次跑通实测值，仅**超过**时失败（Open Question 1 / A3）
- 密钥扫描排除 `scripts/setup-env.example.sh`，避免占位模板假红

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] 撤销阶段级需求过早勾选**
- **Found during:** 状态收尾
- **Issue:** `requirements.mark-complete REQ-phase-7-production` 会在仅完成 07-01 时把整阶段需求标 Complete
- **Fix:** 将 REQUIREMENTS.md 中该条目改回 Pending；SUMMARY `requirements-completed` 置空
- **Files modified:** `.planning/REQUIREMENTS.md`, `07-01-SUMMARY.md`
- **Verification:** 勾选框为 `[ ]`，traceability 为 Pending

---

**Total deviations:** 1 auto-fixed (Rule 2)
**Impact on plan:** 仅修正规划状态准确性，不影响门禁实现。

Task 3 无需改扫描范围或 exclude：首次全绿，无误报。

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Wave 2 CI（07-02）可直接调用 `bash scripts/quality-gate.sh`
- 基线三元组已锁定，后续回归对照以此为准

## Self-Check: PASSED

- FOUND: `scripts/quality-gate.sh`
- FOUND: `scripts/version-audit.sh`（含 BOM 缺失 `exit 1`）
- FOUND: `scripts/spring-ai-2-readiness.sh`（含 `--fail-above`）
- FOUND: commit `43f622c`
- FOUND: commit `b699cca`
- VERIFIED: `bash scripts/quality-gate.sh` → exit 0，输出含 `quality-gate OK`

---
*Phase: 07-production*
*Completed: 2026-07-17*
