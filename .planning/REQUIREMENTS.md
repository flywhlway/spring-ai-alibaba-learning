# Requirements: spring-ai-alibaba-learning

**Defined:** 2026-07-04
**Core Value:** 学习者能用 `mvn spring-boot:run` 跑通全部 48 个 Demo 与 3 个企业项目，并通过 HANDOFF §7 质量门禁

## v1 Requirements

Brownfield registration: Phase 1–2 VALIDATED（已交付）。Active 工作从 Phase 3 起。需求来自既有交付物与验收标准，不发明新需求。

### Phase 1 — 基座（Validated）

- [x] **REQ-phase-1-scaffold**: 脚手架与调研落地——父 pom.xml、common 模块（含单测）、docker/docker-compose.yml（profiles）、scripts/、docs/00-overview/（含 ADR-001~006）、examples/README.md 清单、projects/README.md 蓝图、根 README.md

### Phase 2 — 教程与 starter（Validated）

- [x] **REQ-phase-2-tutorials-starter**: 22 章教材级教程（docs/tutorial/01~22）、saa-learning-starter（审计 Advisor、模型路由降级、成本采集）、QA 脚本（version-audit.sh、spring-ai-2-readiness.sh）

### Phase 3 — 48 个独立 Demo（Validated）

- [x] **REQ-phase-3-demos**: 将教程各章核心 API 落成 48 个可独立 `mvn spring-boot:run` 的最小 Demo；编号/命名/端口以 examples/README.md 为 SSOT，满足 HANDOFF §3 验收（独立 pom、端口 180NN、infra profiles、环境变量密钥、REST+curl+README+api.http、Testcontainers/EnabledIfEnvironmentVariable、复用 common/starter、零 TODO/零废弃 API）— **UAT 48/48 通过 2026-07-05**

### Phase 4 — 知识库问答（Active）

- [x] **REQ-phase-4-knowledge-qa**: knowledge-qa-platform（端口 19100）——制度/手册/技术文档统一问答；MinIO+ETL、DashScope Embedding、RAG+Citation、多模型、Redis ChatMemory、Prompt 管理、Security+审计+Micrometer、SSE；DB：PostgreSQL + Milvus + Redis

### Phase 5 — 办公 Agent（Active）

- [ ] **REQ-phase-5-office-agent**: office-agent-assistant（端口 19200）——会议纪要/日报/邮件/数据查询/日程与审批；Prompt 模板+结构化输出、SQL/HTTP/Excel/Calendar Tools、MCP Client、Agent 编排、Redis+JDBC 记忆；DB：MySQL + pgvector + Redis

### Phase 6 — 智能客服（Active）

- [x] **REQ-phase-6-smart-cs**: smart-cs-platform（端口 19300）——FAQ 秒答、多智能体协作、工单、人工接管、运营看板；RoutingAgent+Supervisor+Handoffs、Milvus+Redis 语义缓存+ES、HITL、Micrometer/Prometheus/Grafana、Nacos；DB：PostgreSQL + Milvus + Redis + ES

### Phase 7 — 生产化（Validated 2026-07-17）

- [x] **REQ-phase-7-production**: 统一测试、CI/CD、部署、调优与排障；质量门禁可在每阶段收口执行（编译、curl、version-audit、spring-ai-2-readiness、无废弃 API/硬编码密钥/TODO）— **本地 quality-gate + 五大交付物核对通过 2026-07-17**

## v2 Requirements

Deferred. None registered at ingest.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Ollama / 本地模型 / GPU | ADR-003 锁定全云端 DashScope + DeepSeek |
| Spring Boot 4 / Spring AI 2.0 | SAA 无对应兼容版（ADR-002） |
| Gradle | ADR-005 锁定 Maven |
| 重做 Phase 1–2 | 已交付并验证 |
| 偏离 examples/README.md 的 Demo 编号/命名/端口 | 清单为 SSOT |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| REQ-phase-1-scaffold | Phase 1 | Complete |
| REQ-phase-2-tutorials-starter | Phase 2 | Complete |
| REQ-phase-3-demos | Phase 3 | Complete |
| REQ-phase-4-knowledge-qa | Phase 4 | Complete |
| REQ-phase-5-office-agent | Phase 5 | Pending |
| REQ-phase-6-smart-cs | Phase 6 | Complete |
| REQ-phase-7-production | Phase 7 | Complete |

**Coverage:**
- v1 requirements: 7 total
- Mapped to phases: 7
- Unmapped: 0 ✓

---
*Requirements defined: 2026-07-04*
*Last updated: 2026-07-17 after Phase 7 local quality-gate closeout*
