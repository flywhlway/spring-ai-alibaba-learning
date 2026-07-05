---
phase: 4
slug: knowledge-qa-platform
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-07-05
scope: knowledge-qa-platform
---

# Phase 4 — Validation Strategy

> Per-phase validation contract for `projects/knowledge-qa-platform`（端口 19100）。

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + AssertJ + Spring Boot Test 3.5.16 |
| **Config file** | `src/test/java/com/flywhl/saa/knowledgeqa/support/`（Wave 6 建立） |
| **Quick run command** | `mvn -f projects/knowledge-qa-platform/pom.xml -q test` |
| **Full suite command** | `mvn -f projects/knowledge-qa-platform/pom.xml clean install` |
| **Smoke command** | `bash scripts/uat-knowledge-qa.sh`（infra + spring-boot:run + curl） |
| **Estimated runtime** | compile ~30s；无 Key test ~1min；有 Key IT ~2–5min；smoke ~3min |

---

## Sampling Rate

- **After every plan task:** `mvn -f projects/knowledge-qa-platform/pom.xml -q compile`
- **After every wave:** 该 wave 覆盖模块 compile 绿
- **Before phase close:** `clean install` + `version-audit.sh` + `spring-ai-2-readiness.sh` + `uat-knowledge-qa.sh`
- **Max feedback latency:** 90s per compile

---

## Per-Plan Verification Map

| Task | Plan | Wave | Requirement | Test Type | Automated Command | Status |
|------|------|------|-------------|-----------|-------------------|--------|
| 04-01-01 | 01 | 1 | 包路径迁移 | compile | `mvn -f projects/knowledge-qa-platform/pom.xml -q compile` | ⬜ |
| 04-01-02 | 01 | 1 | Entity/Repository | unit | compile + Repository IT stub | ⬜ |
| 04-01-03 | 01 | 1 | DTO/VO/Converter | compile | compile | ⬜ |
| 04-02-01 | 02 | 2 | JWT Security | compile | compile | ⬜ |
| 04-02-02 | 02 | 2 | MinIO/VectorStore/Memory | compile | compile + MessageChatMemoryAdvisor @Bean | ⬜ |
| 04-02-03 | 02 | 2 | PromptTemplateProvider | compile | compile | ⬜ |
| 04-03-01 | 03 | 3 | RagPipelineFactory | unit | `RagPipelineFactoryTest` | ⬜ |
| 04-03-02 | 03 | 3 | DocumentEtlPipeline | unit | `DocumentEtlPipelineTest` | ⬜ |
| 04-03-03 | 03 | 3 | AiClientConfig + Seeder | compile | compile | ⬜ |
| 04-04-01 | 04 | 4 | Auth login | integration | `AuthIntegrationTest` | ⬜ |
| 04-04-02 | 04 | 4 | Qa ask/stream | integration | `QaAskIT`/`QaStreamIT` @EnabledIf | ⬜ |
| 04-04-03 | 04 | 4 | Conversation/Feedback | compile | compile | ⬜ |
| 04-05-01 | 05 | 5 | admin documents/prompts | compile | compile | ⬜ |
| 04-05-02 | 05 | 5 | audit_log 双轨落库 | integration | `AuditLogServiceTest` + admin 操作后 audits 可查 | ⬜ |
| 04-05-03 | 05 | 5 | KnowledgeOpsTools | compile | compile | ⬜ |
| 04-06-01 | 06 | 6 | 单测 | unit | `mvn test` 无 Key 全绿 | ⬜ |
| 04-06-02 | 06 | 6 | Testcontainers | integration | PG+Redis IT | ⬜ |
| 04-06-03 | 06 | 6 | HANDOFF §7 + smoke | e2e | `clean install && version-audit && spring-ai-2-readiness && uat-knowledge-qa.sh` | ⬜ |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command |
|--------|----------|-----------|-------------------|
| REQ-phase-4-knowledge-qa | JWT login | integration | `AuthIntegrationTest` |
| REQ-phase-4-knowledge-qa | 带 Citation 问答 | integration + smoke | `QaAskIT` + `uat-knowledge-qa.sh` ask |
| REQ-phase-4-knowledge-qa | SSE 流式 | integration + smoke | `QaStreamIT` + smoke stream |
| REQ-phase-4-knowledge-qa | 文档上传/索引 | smoke | `uat-knowledge-qa.sh` admin upload |
| REQ-phase-4-knowledge-qa | audit_log 双轨 | integration | `AuditLogServiceTest` |
| REQ-phase-4-knowledge-qa | HANDOFF §7 | gate | `clean install` + audit scripts |

---

## Manual-Only Verifications

| Behavior | Why Manual | Instructions |
|----------|------------|--------------|
| Milvus 冷启动 30~60s | compose health 时序 | `docker compose ps` 全 healthy 后再 smoke |
| 全量 api.http 人工走查 | 04-UAT.md 清单 | 按 04-UAT.md 逐条 curl |

---

## Wave 0 Gaps

- [ ] 迁移 `com\/flywhl\/` → `com/flywhl/saa/knowledgeqa/`
- [ ] `testcontainers-redis` pom 依赖
- [ ] `scripts/uat-knowledge-qa.sh` smoke 脚本
- [ ] `DemoKnowledgeSeeder` 解决 data.sql INDEXED 无 Milvus 向量
