---
phase: 06-smart-cs-platform
plan: 08
subsystem: infra
tags: [docker-compose, volume-mount, db-init, gap-closure]

requires:
  - phase: 06-smart-cs-platform
    provides: 企业项目 override 与 scs-db-init 骨架
provides:
  - 三处企业项目 db volume 相对 docker/ 的正确路径
  - smartcs prometheus volume 正确路径
  - scs-db-init 运行时挂载验证（Mounts.Source → projects/.../db）
affects: [06-09-query-rewrite, uat-smart-cs]

tech-stack:
  added: []
  patterns: [compose-volume-relative-to-first-file]

key-files:
  created: []
  modified:
    - projects/smart-cs-platform/docker-compose.override.yml
    - projects/knowledge-qa-platform/docker-compose.override.yml
    - projects/office-agent-assistant/docker-compose.override.yml

key-decisions:
  - "改 volume 为 ../projects/<proj>/...，不引入 --project-directory，保持根目录 compose 调用兼容"

patterns-established:
  - "多文件 compose：override 相对路径相对首文件父目录 docker/，勿用 ./db"

requirements-completed:
  - REQ-phase-6-smart-cs

duration: 5min
completed: 2026-07-17
---

# Phase 06: Plan 08 Summary

**关闭 UAT Gap：三处企业项目 compose override 的 db/prometheus volume 改为相对 `docker/` 的 `../projects/...`，scs-db-init 挂载与 init 均验证通过。**

## Performance

- **Duration:** 5 min
- **Started:** 2026-07-17T16:06:00Z
- **Completed:** 2026-07-17T16:11:00Z
- **Tasks:** 2/2
- **Files modified:** 3

## Accomplishments
- smartcs / kqa / office 的 `*-db-init` volume 从 `./db` 改为 `../projects/<proj>/db`
- smartcs `scs-prometheus` volume 改为 `../projects/smart-cs-platform/monitor/prometheus.yml`
- 文件头注释标明：相对路径相对首文件 `docker/`，勿改回 `./db`
- Runtime：`docker inspect` Mounts.Source = `.../projects/smart-cs-platform/db`；exit 0；日志含初始化完成

## Task Commits

1. **Task 1: 修正三处企业项目 override 的 db volume 路径** - `ee5c100` (fix)
2. **Task 2: 同步修正 smartcs monitor prometheus volume 并验证 init 挂载** - `98fad2a` (fix)

## Files Created/Modified
- `projects/smart-cs-platform/docker-compose.override.yml` - scs-db-init + prometheus 路径 + 注释
- `projects/knowledge-qa-platform/docker-compose.override.yml` - kqa-db-init 路径 + 注释
- `projects/office-agent-assistant/docker-compose.override.yml` - office-db-init 路径 + 注释

## Decisions Made
- 不引入 `--project-directory`；与现有根目录 `docker compose -f docker/... -f projects/.../override.yml` 及 deploy-smoke 保持一致

## Deviations from Plan
None

## Verification
- G-06-08-db / G-06-08-no-rel：三处 `../projects/.../db:` 且无 `./db:` — PASS
- G-06-08-prom：prometheus 路径已改且无 `./monitor/` — PASS
- Runtime inspect Mounts.Source 含 `smart-cs-platform/db`；exit 0 — PASS

## Self-Check: PASSED

- [x] key-files.modified 三文件存在且含正确路径
- [x] `git log --grep=06-08` 有提交
- [x] 各 task acceptance 已满足
