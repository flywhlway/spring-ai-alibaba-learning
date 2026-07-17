---
phase: 6
slug: smart-cs-platform
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-17
updated: 2026-07-17
scope: gap-closure-cold-start
---

# Phase 6 — Validation Strategy（Gap Closure）

> Per-phase validation contract for `projects/smart-cs-platform` 冷启动 UAT gap（06-08 / 06-09）。
> 本文件登记 gap 修复的 automated 命令；HITL REVIEW Critical 不在本 VALIDATION 范围。

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | bash grep 门禁 + Maven compile；可选 Docker Compose + curl health |
| **Config file** | `projects/smart-cs-platform/pom.xml`；compose override 见三企业项目 |
| **Quick run command** | 见下方 Gap Automated Gates |
| **Full suite command** | `mvn -f projects/smart-cs-platform/pom.xml clean install` |
| **Smoke command** | `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`（需 Key + infra） |
| **Estimated runtime** | grep <5s；compile ~30–60s；runtime health ~2–5min |

---

## Sampling Rate

- **After every gap plan task:** 对应 grep / compile 命令
- **After 06-08:** volume + prometheus grep 全绿
- **After 06-09:** `{target}`/`{query}` grep + compile；Docker 可用时 health 硬门禁
- **Before gap close:** 下方 Gap Automated Gates 全部 ✅（runtime 行按 Docker 可用性）
- **Max feedback latency:** compile <90s；health <5min

---

## Gap Automated Gates（本轮必须登记）

| Gate ID | Plan | Behavior | Automated Command | Status |
|---------|------|----------|-------------------|--------|
| G-06-08-db | 08 | 三处 db volume 相对 `docker/` | `grep -E '\.\./projects/(smart-cs-platform\|knowledge-qa-platform\|office-agent-assistant)/db:' projects/smart-cs-platform/docker-compose.override.yml projects/knowledge-qa-platform/docker-compose.override.yml projects/office-agent-assistant/docker-compose.override.yml \| wc -l \| grep -qx 3` | ⬜ |
| G-06-08-no-rel | 08 | 无残留 `./db:` | `! grep -E '[[:space:]]- \./db:' projects/*/docker-compose.override.yml` | ⬜ |
| G-06-08-prom | 08 | prometheus volume 路径 | `grep -q '\.\./projects/smart-cs-platform/monitor/prometheus.yml' projects/smart-cs-platform/docker-compose.override.yml && ! grep -E '[[:space:]]- \./monitor/' projects/smart-cs-platform/docker-compose.override.yml` | ⬜ |
| G-06-09-ph | 09 | `.st` + 双 data.sql 含 `{target}`+`{query}` | `for f in projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st projects/smart-cs-platform/db/data.sql projects/smart-cs-platform/src/test/resources/db/data.sql; do grep -q '{target}' "$f" && grep -q '{query}' "$f" \|\| exit 1; done` | ⬜ |
| G-06-09-upd | 09 | 幂等 UPDATE 存在 | `grep -q "content NOT LIKE '%{target}%'" projects/smart-cs-platform/db/data.sql && grep -q "content NOT LIKE '%{target}%'" projects/smart-cs-platform/src/test/resources/db/data.sql` | ⬜ |
| G-06-09-compile | 09 | 模块可编译 | `mvn -f projects/smart-cs-platform/pom.xml -DskipTests compile -q` | ⬜ |
| G-06-09-health | 09 | 冷启动 health UP（Docker 可用时硬门禁） | 见 Runtime Health Gate | ⬜ / ⚠️ residual |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ residual（无 Docker 时）*

---

## Runtime Health Gate（可选环境 → 有 Docker 则硬验收）

**前置：** Docker 可用；仓库根执行；已完成 06-08 / 06-09 代码改动。

```bash
# 1) 重建 init（验证挂载 + 幂等 UPDATE）
docker compose -f docker/docker-compose.yml \
  -f projects/smart-cs-platform/docker-compose.override.yml \
  --profile core --profile vector --profile search --profile cloud --profile smartcs \
  up -d --force-recreate scs-db-init

docker inspect saa-scs-db-init --format '{{range .Mounts}}{{.Source}} {{end}}'
# 期望含 projects/smart-cs-platform/db，不含 .../docker/db 作为唯一源

# 2) 等中间件 healthy 后启动应用
mvn -f projects/smart-cs-platform/pom.xml spring-boot:run
# 另终端：
curl -sf http://localhost:19300/actuator/health | grep -qi UP
# 应用日志不得出现：placeholders must be present in the prompt template: target
```

**Residual risk（无 Docker / 中间件不全时）：** 仅 grep + compile 绿不能证明 Bean 冷启动；SUMMARY 必须写明 `G-06-09-health=residual`，并由后续有 Docker 的会话或 `uat-smart-cs.sh` 补跑。

---

## Per-Plan Verification Map（Gap）

| Task | Plan | Wave | Requirement | Test Type | Automated Command | Status |
|------|------|------|-------------|-----------|-------------------|--------|
| 06-08-01 | 08 | 1 | volume 三处 db | grep | G-06-08-db + G-06-08-no-rel | ⬜ |
| 06-08-02 | 08 | 1 | prometheus volume | grep (+ optional inspect) | G-06-08-prom | ⬜ |
| 06-09-01 | 09 | 2 | query-rewrite 占位符 | grep | G-06-09-ph + G-06-09-upd | ⬜ |
| 06-09-02 | 09 | 2 | README + 冷启动 | compile + health-or-residual | G-06-09-compile + G-06-09-health | ⬜ |

---

## Phase Requirements → Test Map（本 gap 切片）

| Req ID | Behavior | Test Type | Automated Command |
|--------|----------|-----------|-------------------|
| REQ-phase-6-smart-cs | compose 挂载正确 → schema 可导入 | grep + optional docker inspect | G-06-08-* |
| REQ-phase-6-smart-cs | RewriteQueryTransformer 模板合规 | grep + compile | G-06-09-ph/upd/compile |
| REQ-phase-6-smart-cs | 冷启动 health UP | runtime curl | G-06-09-health（Docker 硬门禁） |

---

## Manual-Only Verifications

| Behavior | Why Manual | Instructions |
|----------|------------|--------------|
| 全量 uat-smart-cs.sh（ask/stream/HITL） | 需 Key；HITL Critical 另案 | `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh` |
| 06-REVIEW HITL pending | STATE Pending / D-14 | 不纳入本 gap VALIDATION |

---

## Wave 0 Gaps

- [x] 本 VALIDATION 已登记 gap automated 命令（本文件）
- [ ] 执行 06-08 / 06-09 后将上表 Status 标 ✅
- [ ] Docker 环境下将 G-06-09-health 标 ✅（否则 ⚠️ residual）

---
*Generated 2026-07-17 for Phase 06 UAT gap closure (Nyquist)*
