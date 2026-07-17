---
phase: 7
slug: production
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-07-17
scope: ci-cd-quality-gates-deploy
---

# Phase 7 — Validation Strategy

> Per-phase validation contract for 生产化（CI/CD、质量门禁、Compose 部署路径）。

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5（Spring Boot Test）+ Testcontainers + AssertJ；脚本门禁 bash |
| **Config file** | 各模块 pom；根无聚合 surefire |
| **Quick run command** | `mvn -pl common,starter -am test` |
| **Full suite command** | `mvn -pl common,starter -am clean install` + `for p in projects/*/pom.xml; do mvn -f "$p" test; done` |
| **Gate command** | `bash scripts/quality-gate.sh`（07-01 交付后） |
| **Estimated runtime** | common+starter ~2–5min；examples 矩阵 compile ~10–20min；gate ~1–3min |

---

## Sampling Rate

- **After every plan task:** 相关脚本 `bash -n` / 对应模块 compile
- **After every wave:** Wave 1 → `quality-gate.sh`；Wave 2 → GHA 等价本地命令；Wave 3+ → gate + 文档存在性
- **Before phase close:** CI 绿等价 + quality-gate 绿 + 生产化文档可读 + UAT 索引存在
- **Max feedback latency:** 门禁脚本 <3min（不含全量 examples 矩阵）

---

## Per-Plan Verification Map

| Task | Plan | Wave | Requirement | Test Type | Automated Command | Status |
|------|------|------|-------------|-----------|-------------------|--------|
| 07-01-* | 01 | 1 | 门禁硬化 | script | `bash scripts/quality-gate.sh` | ⬜ |
| 07-02-* | 02 | 2 | GHA CI | workflow yaml + local parity | `actionlint` 或 yaml 校验 + 本地等价 mvn | ⬜ |
| 07-03-* | 03 | 3 | Compose 部署 | smoke/docs | `bash -n scripts/deploy-smoke.sh` + 文档含 quality-gate | ⬜ |
| 07-04-* | 04 | 4 | UAT 索引 | docs | 索引文件存在且链到 uat-*.sh | ⬜ |
| 07-05-* | 05 | 5 | 收口 | gate + human | quality-gate + ROADMAP 勾选 | ⬜ |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REQ-phase-7-production | common+starter 可构建且单测绿 | unit/integration | `mvn -B -pl common,starter -am clean install` | ✅ |
| REQ-phase-7-production | examples 可编译 | compile smoke | `mvn -B -f examples/*/pom.xml -DskipTests compile`（矩阵） | ✅ pom |
| REQ-phase-7-production | projects test（无 Key 绿） | unit + conditional IT | `mvn -B -f projects/*/pom.xml test` | ✅ |
| REQ-phase-7-production | version-audit 全绿 | script gate | `bash scripts/version-audit.sh` via quality-gate | ⬜ harden |
| REQ-phase-7-production | spring-ai-2-readiness 低位 | script gate | readiness + `--fail-above` | ⬜ threshold |
| REQ-phase-7-production | 无废弃 API / 无 sk- / 无 TODO | script scan | `quality-gate.sh` | ⬜ |
| REQ-phase-7-production | CI 可重复执行 | GHA | `.github/workflows/ci.yml` | ⬜ |
| REQ-phase-7-production | Compose 部署路径文档化 | docs/smoke | `05-生产化与运维.md` + deploy-smoke | ⬜ |
| REQ-phase-7-production | curl 验证 | manual UAT | `uat-*.sh`（不进默认 CI） | ✅ scripts |

---

## Manual-Only Verifications

- 真机 UAT（Phase 4/5/6 HUMAN-UAT）— 索引可见即可，不强制 CI
- GitHub Actions 首次 push 实跑（需远程仓库）
- Optional `model-it.yml` 在配置 `AI_DASHSCOPE_API_KEY` secret 后

---

## Wave 0 Gaps

- [ ] `scripts/quality-gate.sh`
- [ ] `.github/workflows/ci.yml` + `model-it.yml`
- [ ] readiness 基线常量
- [ ] `docs/00-overview/05-生产化与运维.md` + UAT 债务索引

---
*Generated 2026-07-17 from 07-RESEARCH Validation Architecture*
