---
phase: 3
slug: 48-demo
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-04
batch: 2
scope: demos 20-34
---

# Phase 3 Batch 2 — Validation Strategy

> Per-batch validation contract for demos 20~34 (RAG / Embedding / VectorStore / MCP).

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test |
| **Config file** | 各 Demo `src/test/java`（按需） |
| **Quick run command** | `mvn -f examples/NN-xxx/pom.xml -q compile` |
| **Full suite command** | `for d in examples/{20..34}-*/; do mvn -f "$d/pom.xml" -q compile || exit 1; done` |
| **Estimated runtime** | compile ~2–4 min；冒烟 IT（有 Key）~1–3 min/个 |

---

## Sampling Rate

- **After every demo task:** `mvn -f examples/NN-xxx/pom.xml -q compile`
- **After every plan wave:** 该 plan 覆盖的全部 Demo compile
- **Before batch UAT:** 03-08 全量 20~34 compile 绿
- **Max feedback latency:** 60s per demo compile

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 03-04-01 | 04 | 1 | REQ-phase-3-demos | T-20-01 | validateSchema + 强类型 | compile + IT | `mvn -f examples/20-structured-output-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-04-02 | 04 | 1 | REQ-phase-3-demos | T-20-01 | ParameterizedTypeReference | compile | `mvn -f examples/21-json-schema-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-05-01 | 05 | 1 | REQ-phase-3-demos | — | Embedding 无硬编码密钥 | compile + IT | `mvn -f examples/22-embedding-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-05-02 | 05 | 1 | REQ-phase-3-demos | T-23-01 | FilterExpressionBuilder only | compile | `mvn -f examples/23-pgvector-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-05-03 | 05 | 1 | REQ-phase-3-demos | — | dimensions=1024 | compile | `mvn -f examples/24-milvus-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-05-04 | 05 | 1 | REQ-phase-3-demos | — | Redis Stack 声明 | compile | `mvn -f examples/25-redis-vector-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-05-05 | 05 | 1 | REQ-phase-3-demos | — | ES hybrid search API | compile | `mvn -f examples/26-es-hybrid-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-06-01 | 06 | 1 | REQ-phase-3-demos | T-27-01 | QuestionAnswerAdvisor | compile + IT | `mvn -f examples/27-rag-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-06-02 | 06 | 1 | REQ-phase-3-demos | T-27-01 | RetrievalAugmentationAdvisor | compile | `mvn -f examples/28-advanced-rag-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-06-03 | 06 | 1 | REQ-phase-3-demos | T-27-01 | citations from metadata | compile | `mvn -f examples/29-hybrid-rag-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-06-04 | 06 | 1 | REQ-phase-3-demos | — | eval endpoint | compile | `mvn -f examples/30-rag-eval-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-07-01 | 07 | 1 | REQ-phase-3-demos | T-31-01 | @McpTool beans | compile + IT | `mvn -f examples/31-mcp-server-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-07-02 | 07 | 1 | REQ-phase-3-demos | T-32-01 | 仅连本机 31 | compile | `mvn -f examples/32-mcp-client-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-07-03 | 07 | 1 | REQ-phase-3-demos | T-31-01 | Bearer 校验 | compile | `mvn -f examples/33-mcp-auth-demo/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-07-04 | 07 | 1 | REQ-phase-3-demos | T-34-01 | Server 18034 / Client 18134 | compile | `mvn -f examples/34-mcp-nacos-demo/order-mcp-server/pom.xml -q compile` | ❌ W0 | ⬜ pending |
| 03-08-01 | 08 | 2 | REQ-phase-3-demos | — | 20~34 全绿 | compile gate | batch compile loop | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers batch requirements:

- [x] Parent POM BOM（spring-ai / saa / extensions）
- [x] `saa-learning-common`（Result / GlobalExceptionHandler）
- [x] `docker/docker-compose.yml` profiles
- [x] Demo pattern from examples/04, 11, 16

No new shared test harness required for batch 2.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| curl 与章节预期输出一致 | REQ-phase-3-demos | 需真实模型 Key + 中间件 | 各 README「快速验证」 |
| 25 Redis Stack 连通 | REQ-phase-3-demos | 非 core profile 默认镜像 | 按 25 README 启 Stack 后 curl |
| 34 Nacos 注册可见 | REQ-phase-3-demos | 需 cloud profile + UI | 控制台 AI → MCP 服务列表 |
| 32 消费 31 工具 | REQ-phase-3-demos | 双进程 | 先启 31 再启 32 |

---

## Validation Sign-Off

- [x] All tasks have automated compile verify
- [x] Sampling continuity: every demo task has compile
- [x] Wave 0 covered by existing common/parent
- [x] No watch-mode flags
- [x] Feedback latency < 60s per compile
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-07-04 (batch 2 plan-phase)
