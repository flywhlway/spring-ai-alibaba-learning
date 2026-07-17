---
phase: 07-production
plan: 03
subsystem: deploy-docs
tags: [docker-compose, infra.sh, deploy-smoke, production-docs, quality-gate, ci]

requires:
  - phase: 07-production
    provides: scripts/quality-gate.sh（07-01）与 ci.yml/model-it.yml（07-02）
provides:
  - scripts/deploy-smoke.sh Compose 部署 smoke 骨架（wait/retry + actuator/health）
  - docs/00-overview/05-生产化与运维.md（CI/门禁/部署/排障）
  - docs/README.md 与根 README.md 索引链接
affects: [07-04 UAT 索引, 07-05 收口验证]

tech-stack:
  added: []
  patterns: [compose override 复用非手写编排, deploy-smoke 参数化 kqa|office|smartcs]

key-files:
  created:
    - scripts/deploy-smoke.sh
    - docs/00-overview/05-生产化与运维.md
  modified:
    - docs/README.md
    - README.md

key-decisions:
  - "企业项目中间件经 docker compose + override，非仅 infra.sh（override 含建库/redis-stack）"
  - "文档显式链到既有 ci.yml/model-it.yml，不复制 YAML"
  - "无 K8s/Helm；Dockerfile 仅可选 spring-boot:build-image 说明（D-10/D-11）"

patterns-established:
  - "Pattern: deploy-smoke 别名→目录/端口/profiles，与 projects/*/README 对齐"
  - "Pattern: 生产化文档含演示口令替换与密钥仅 env 警告（T-07-07/T-07-08）"

requirements-completed: []  # REQ-phase-7-production 待 Phase 07 全部 plans 完成后再勾选（与 07-01/07-02 一致）

duration: 2min
completed: 2026-07-17
---

# Phase 07 Plan 03: Compose 部署路径 + 生产化文档 Summary

**Compose 部署 smoke 骨架 + `05-生产化与运维.md`，从根 README 可跳到 CI/门禁/部署/排障（无 K8s）**

## Performance

- **Duration:** 2 min
- **Started:** 2026-07-17T15:05:11Z
- **Completed:** 2026-07-17T15:07:13Z
- **Tasks:** 2/2
- **Files modified:** 4

## Accomplishments

- 新建 `scripts/deploy-smoke.sh`：`kqa|office|smartcs`、`--skip-infra`/`--no-start`、compose override、Milvus wait/retry、`/actuator/health`（19100/19200/19300）
- 撰写 `docs/00-overview/05-生产化与运维.md`：引用既有 `ci.yml`/`model-it.yml`、`quality-gate.sh`、`infra.sh` profiles、三项目 override、排障与教学级调优
- 更新 `docs/README.md` 总览表第 05 行与根 `README.md` 快速开始/学习入口短链

## Task Commits

Each task was committed atomically:

1. **Task 1: 新建 deploy-smoke.sh 骨架** - `a4aa755` (feat)
2. **Task 2: 撰写 05-生产化与运维.md 并更新索引** - `8a588da` (docs)

**Plan metadata:** （见下方 final docs commit）

## Files Created/Modified

- `scripts/deploy-smoke.sh` - Compose 类生产部署 smoke（复用 override，非新编排）
- `docs/00-overview/05-生产化与运维.md` - Phase 7 生产化章节（D-12）
- `docs/README.md` - 总览表增加 05 行
- `README.md` - 快速开始链 quality-gate + 05；学习入口第 5 项

## Decisions Made

- 企业项目必须叠加 `docker-compose.override.yml`（建库/bucket/redis-stack）；`infra.sh` 仅作通用 profile 补充说明
- 文档指向 `.github/workflows/ci.yml` 与 `model-it.yml` 原文，避免 YAML 漂移
- 明确非目标：无 K8s/Helm、无 Boot4/Spring AI 2.0；镜像路径可选一句即可（A5）

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 避免 macOS Bash 3.2 的 mapfile**
- **Found during:** Task 1
- **Issue:** 初稿用 `mapfile` 组装 compose 参数，系统 Bash 3.2 不支持
- **Fix:** 改为函数内 `COMPOSE_ARGS` 数组拼接
- **Files modified:** `scripts/deploy-smoke.sh`
- **Commit:** `a4aa755`

**Total deviations:** 1 auto-fixed（Rule 3）
**Impact on plan:** 无范围蔓延；仅兼容性修复。

## Issues Encountered

None beyond the Bash 3.2 compatibility fix above.

## Threat Flags

无新增未登记威胁面。T-07-07（演示口令替换警告）与 T-07-08（密钥仅 env / setup-env.example）已写入文档与脚本头注释；T-07-09（Milvus wait/retry）已实现。

## Known Stubs

None — deploy-smoke 为可执行骨架（本计划不要求真实拉起全套中间件；Wave 5 门禁级验证）。

## Self-Check: PASSED

- FOUND: `scripts/deploy-smoke.sh`
- FOUND: `docs/00-overview/05-生产化与运维.md`
- FOUND: commit `a4aa755`
- FOUND: commit `8a588da`
- VERIFY: PLAN `<automated>` grep 门禁均通过（`bash -n` + actuator/ports + infra/compose；文档 quality-gate/infra.sh/索引）
