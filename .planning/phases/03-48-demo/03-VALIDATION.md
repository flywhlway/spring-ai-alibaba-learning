---
phase: 3
slug: 48-demo
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-04
batch: 3
scope: demos 35-48
---

# Phase 3 Batch 3 — Validation Strategy

> Per-batch validation contract for demos 35~48 (Agent / Graph / Multi-Agent / Stream / Observability / Routing / Fallback).

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test |
| **Config file** | 各 Demo `src/test/java`（按需） |
| **Quick run command** | `mvn -f examples/NN-xxx/pom.xml -q compile` |
| **Full suite command** | `mvn -pl common,starter -am -q -DskipTests install && for d in examples/{35..48}-*/; do mvn -f "$d/pom.xml" -q compile || exit 1; done` |
| **Estimated runtime** | compile ~3–5 min；冒烟 IT（有 Key）~1–3 min/个 |

---

## Sampling Rate

- **After every demo task:** `mvn -f examples/NN-xxx/pom.xml -q compile`
- **After every plan wave:** 该 plan 覆盖的全部 Demo compile
- **Before batch UAT:** 03-14 全量 35~48 compile 绿
- **Max feedback latency:** 60s per demo compile

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 03-09-01 | 09 | 1 | REQ-phase-3-demos | T-35-01 | ReactAgent + tools | compile + IT | `mvn -f examples/35-agent-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-09-02 | 09 | 1 | REQ-phase-3-demos | — | Agent Skills | compile | `mvn -f examples/36-agent-skills-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-09-03 | 09 | 1 | REQ-phase-3-demos | T-37-01 | HITL pause/resume | compile | `mvn -f examples/37-agent-hitl-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-10-01 | 10 | 1 | REQ-phase-3-demos | — | StateGraph | compile + IT | `mvn -f examples/38-workflow-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-10-02 | 10 | 1 | REQ-phase-3-demos | — | parallel edges | compile | `mvn -f examples/39-graph-parallel-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-10-03 | 10 | 1 | REQ-phase-3-demos | — | saga compensate | compile | `mvn -f examples/40-graph-saga-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-11-01 | 11 | 1 | REQ-phase-3-demos | — | multi-agent modes | compile + IT | `mvn -f examples/41-multi-agent-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-11-02 | 11 | 1 | REQ-phase-3-demos | — | supervisor handoff | compile | `mvn -f examples/42-supervisor-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-11-03 | 11 | 1 | REQ-phase-3-demos | T-43-01 | A2A Nacos 18043/18143 | compile | `mvn -f examples/43-a2a-nacos-demo/` | ❌ W0 | ⬜ pending |
| 03-12-01 | 12 | 1 | REQ-phase-3-demos | — | SSE stream | compile + IT | `mvn -f examples/44-stream-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-12-02 | 12 | 1 | REQ-phase-3-demos | — | /actuator/prometheus | compile | `mvn -f examples/45-observability-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-12-03 | 12 | 1 | REQ-phase-3-demos | — | structured logs + TraceId | compile | `mvn -f examples/46-logging-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-13-01 | 13 | 1 | REQ-phase-3-demos | — | ModelRouter starter | compile + IT | `mvn -f examples/47-routing-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-13-02 | 13 | 1 | REQ-phase-3-demos | — | fallback policy | compile | `mvn -f examples/48-fallback-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-14-01 | 14 | 2 | REQ-phase-3-demos | — | 35~48 全绿 | compile gate | batch compile loop | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers batch requirements:

- [x] Parent POM BOM（spring-ai / saa / extensions）
- [x] `saa-learning-common`（Result / GlobalExceptionHandler）
- [x] `saa-learning-starter`（ModelRouter / CostRecorder / AuditLoggingAdvisor — 44~48）
- [x] `docker/docker-compose.yml` profiles（43 → cloud）
- [x] Demo pattern from examples/04, 11, 16, 34

No new shared test harness required for batch 3. Smoke IT classes created per-demo in plans 09/10/11/12/13.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| curl 与章节预期输出一致 | REQ-phase-3-demos | 需真实模型 Key | 各 README「快速验证」 |
| 43 Nacos A2A 互通 | REQ-phase-3-demos | 需 cloud profile + 双进程 | `bash scripts/infra.sh up cloud`；先启 Server 再启 Client |
| 47 多模型路由 | REQ-phase-3-demos | 需 DEEPSEEK_API_KEY（可选） | 有 Key 时验证路由到 DeepSeek；无 Key 时仅 DashScope 路径 |

---

## Validation Sign-Off

- [x] All tasks have automated compile verify
- [x] Sampling continuity: every demo task has compile
- [x] Wave 0 covered by existing common/starter/parent
- [x] No watch-mode flags
- [x] Feedback latency < 60s per compile
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-07-04 (batch 3 plan-phase)
