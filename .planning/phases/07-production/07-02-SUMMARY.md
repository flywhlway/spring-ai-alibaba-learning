---
phase: 07-production
plan: 02
subsystem: ci
tags: [github-actions, ci, maven, quality-gate, secrets, model-it]

requires:
  - phase: 07-production
    provides: scripts/quality-gate.sh blocking 入口（07-01）
provides:
  - .github/workflows/ci.yml blocking 流水线（common/starter、examples compile、projects test、quality-gate）
  - .github/workflows/model-it.yml secret-gated optional 模型 IT（check-dashscope-secret）
affects: [07-03 docs, Wave 2 CI 真机绿]

tech-stack:
  added: [actions/checkout@v4, actions/setup-java@v4]
  patterns: [动态 examples matrix via jq+ls, check-secret job outputs 门控, mvn -f 非 reactor 模块]

key-files:
  created:
    - .github/workflows/ci.yml
    - .github/workflows/model-it.yml
  modified: []

key-decisions:
  - "examples 编译矩阵用 list-examples job 动态 ls，避免 48 项静态漂移"
  - "model-it 独立 workflow + check-dashscope-secret outputs，禁止 job-level if: secrets.*"
  - "optional 模型 IT 仅跑 projects/smart-cs-platform test（step env 注入 Key）"

patterns-established:
  - "Pattern: examples/projects 一律 mvn -f path/pom.xml，禁止 -pl 模块名"
  - "Pattern: secrets 仅 step 内判断或 env 注入；job if 只读 needs.*.outputs"
  - "Pattern: blocking CI 永不引用 AI_DASHSCOPE_API_KEY"

requirements-completed: []  # REQ-phase-7-production 待 Phase 07 全部 plans 完成后再勾选（与 07-01 一致）

duration: 2min
completed: 2026-07-17
---

# Phase 07 Plan 02: GitHub Actions CI Summary

**Blocking `ci.yml`（无 Key）+ optional `model-it.yml`（check-secret 门控），覆盖 monorepo 构建矩阵与 quality-gate**

## Performance

- **Duration:** 2 min
- **Started:** 2026-07-17T15:02:01Z
- **Completed:** 2026-07-17T15:03:30Z
- **Tasks:** 2/2
- **Files modified:** 2

## Accomplishments

- 新建 blocking CI：`push`/`pull_request`→`main` + `workflow_dispatch`；JDK 21 Temurin + Maven cache
- examples 经 `list-examples` 动态 matrix（`fail-fast: false`，`max-parallel: 6`），一律 `mvn -f examples/.../pom.xml compile`
- 三企业项目 `mvn -f projects/<name>/pom.xml test`（不设 DashScope Key）+ `bash scripts/quality-gate.sh` blocking
- optional `model-it.yml`：`check-dashscope-secret` → outputs → `model-it`；无 Key 跳过且 workflow 成功

## Task Commits

Each task was committed atomically:

1. **Task 1: 创建 blocking ci.yml** - `3142ca6` (feat)
2. **Task 2: 创建 optional model-it.yml（check-secret 模式）** - `ebd2962` (feat)

**Plan metadata:** （本 SUMMARY 提交后回填）

## Files Created/Modified

- `.github/workflows/ci.yml` - blocking：common-starter / list-examples / examples-compile / projects-test / quality-gate
- `.github/workflows/model-it.yml` - optional：check-dashscope-secret + model-it（smart-cs-platform）

## Maintainer Note (model-it)

维护者需在 GitHub → Settings → Secrets and variables → Actions 配置 Repository secret `AI_DASHSCOPE_API_KEY` 后，手动 **Run workflow**（`Model IT (optional)`）才会执行模型真机 IT；未配置时 check job 输出 `defined=false`，`model-it` 跳过，整体仍绿。

## Decisions Made

- examples 用动态 `ls`+`jq` 生成 matrix，少漂移（相对静态 48 项）
- projects matrix 使用 `path: projects/<name>` 字面量，便于审查与 verify grep
- model-it 仅覆盖 `smart-cs-platform`（可按需再加 kqa/office）
- Pin 仅官方 `actions/checkout@v4` 与 `actions/setup-java@v4`（T-07-06 / T-07-SC）

## Deviations from Plan

### Auto-fixed Issues

None - plan executed exactly as written.

（实现细节：projects-test 用 `matrix.include.path` 写出完整 `projects/<name>`，满足 PLAN verify grep；语义仍为三项目 `mvn -f` test。）

## Threat Flags

无新增未登记威胁面。secrets 仅出现在 model-it 的 step 判断/`env`；ci.yml 零 secrets 引用。

## Known Stubs

None.

## Self-Check: PASSED

- FOUND: `.github/workflows/ci.yml`
- FOUND: `.github/workflows/model-it.yml`
- FOUND: commit `3142ca6`
- FOUND: commit `ebd2962`
- VERIFY: ci.yml / model-it.yml PLAN `<automated>` grep 门禁均通过
- actionlint: 未安装，按计划不阻塞
