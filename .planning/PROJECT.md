# spring-ai-alibaba-learning

## What This Is

Spring AI Alibaba 企业级教学仓库：教程正文 + 可运行源码 + 三个企业项目。面向有 Spring Boot/Cloud、Docker、RAG、MCP、LLM 经验的高级开发者，用锁定版本栈（Java 21 / Boot 3.5.16 / SAA 1.1.2.2 / Spring AI 1.1.2）把 SAA 能力从脚手架落到可运行 Demo 与真实业务项目。

**Current State:** ✅ **v1.0 Full Delivery shipped 2026-07-18**

## Core Value

学习者能用 `mvn spring-boot:run` 跑通全部 48 个 Demo 与 3 个企业项目，并通过 HANDOFF §7 质量门禁（编译、curl、version-audit、spring-ai-2-readiness、无废弃 API/硬编码密钥/TODO）。

## Requirements

### Validated

- ✓ **REQ-phase-1-scaffold** — v1.0：父 POM、common、docker profiles、scripts、docs/00-overview（ADR-001~006）
- ✓ **REQ-phase-2-tutorials-starter** — v1.0：22 章教程、saa-learning-starter、QA 脚本
- ✓ **REQ-phase-3-demos** — v1.0：48 个独立 Demo，UAT 48/48
- ✓ **REQ-phase-4-knowledge-qa** — v1.0：knowledge-qa-platform（19100），脚本 UAT 8/0
- ✓ **REQ-phase-5-office-agent** — v1.0：office-agent-assistant（19200），脚本 UAT 12/0
- ✓ **REQ-phase-6-smart-cs** — v1.0：smart-cs-platform（19300），HUMAN-UAT 5/5 + REVIEW-FIX 8/8
- ✓ **REQ-phase-7-production** — v1.0：CI/CD、quality-gate、Compose 部署路径、UAT 债务索引

### Active

（待 `/gsd-new-milestone` 定义下一里程碑需求）

### Out of Scope

- Ollama / 本地模型 / GPU — ADR-003 锁定全云端 DashScope + DeepSeek
- Spring Boot 4 / Spring AI 2.0 — SAA 无对应兼容版，锁死 Boot 3.5.x（ADR-002）
- Gradle — ADR-005 锁定 Maven 多模块
- 重做 Phase 1–2 — 已交付并验证
- 偏离 examples/README.md 的 Demo 编号/命名/端口 — 清单为 SSOT
- K8s/Helm — Phase 7 D-10/D-11 明确非目标

## Next Milestone Goals

候选方向（未锁定，由 `/gsd-new-milestone` 确认）：

- 远程 CI Actions 首次绿与可选 model-it 真跑
- deploy-smoke 三项目 runtime 闭环
- HITL pending 持久化（Redis/DB）与多实例 approve
- Spring AI 2.0 / Boot 4 就绪跟踪（待 SAA 兼容版）

## Context

- **已交付：** common/starter、48 Demo、三企业项目、quality-gate、ci.yml/model-it.yml、生产化文档、UAT 索引
- **归档：** `.planning/milestones/v1.0-{ROADMAP,REQUIREMENTS,MILESTONE-AUDIT}.md`
- **Deferred：** 见 STATE.md Deferred Items（远程 Actions、deploy-smoke、HITL 持久化等）
- **工程约定：** 包根 `com.flywhl.saa`、端口 `180NN`、密钥仅环境变量、复用 common/starter、禁用废弃 API
- **质量门禁：** `bash scripts/quality-gate.sh`

## Constraints

- **Tech stack**: Java 21 · Spring Boot 3.5.16 · SAA 1.1.2.2 · SAA Extensions 1.1.2.2 · Spring AI 1.1.2 — 父 POM 唯一真源
- **BOM**: 必须同时导入 `spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom`
- **Models**: DashScope 主 + DeepSeek 副 — ADR-003
- **Vector stores**: 教学全覆盖；项目一 Milvus、项目二 pgvector、项目三 Milvus+Redis/ES — ADR-004
- **Build**: Maven 多模块，子模块零版本号 — ADR-005
- **API docs**: SpringDoc + Knife4j `/doc.html` — ADR-006
- **Secrets**: 仅环境变量，严禁提交

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| ADR-001: SAA 1.1.2.2 | 官方稳定版；Spring AI 超集 | ✓ Good |
| ADR-002: Boot 3.5.16 | SAA 无 Boot 4 / Spring AI 2.0 对应版 | ✓ Good |
| ADR-003: DashScope 主 + DeepSeek 副 | 全云端；Embedding 统一保证向量空间一致 | ✓ Good |
| ADR-004: 按项目选型 VectorStore | 教学全覆盖 + 企业场景差异 | ✓ Good |
| ADR-005: Maven 多模块 + 四 BOM | 父 POM 唯一版本入口 | ✓ Good |
| ADR-006: SpringDoc + Knife4j | 调试体验统一 | ✓ Good |
| JWT 显式 HS256 | Security 6.5 默认 RS256 与 HMAC 不兼容 | ✓ Good |
| office MySQL @Primary | 防 pgvector DataSource 抢占 JPA | ✓ Good |
| MCP Client 默认关（office） | 自连 :19200 冷启动鸡生蛋 | ✓ Good |
| D-14 HITL → REVIEW-FIX | Chat 路径注册 InterruptionMetadata | ✓ Good |

---
*Last updated: 2026-07-18 after v1.0 milestone archive*
