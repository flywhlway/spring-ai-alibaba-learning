# spring-ai-alibaba-learning

## What This Is

Spring AI Alibaba 企业级教学仓库：教程正文 + 可运行源码 + 三个企业项目，按 7 阶段交付。面向有 Spring Boot/Cloud、Docker、RAG、MCP、LLM 经验的高级开发者，用锁定版本栈（Java 21 / Boot 3.5.16 / SAA 1.1.2.2 / Spring AI 1.1.2）把 SAA 能力从脚手架落到可运行 Demo 与真实业务项目。

## Core Value

学习者能用 `mvn spring-boot:run` 跑通全部 48 个 Demo 与 3 个企业项目，并通过 HANDOFF §7 质量门禁（编译、curl、version-audit、spring-ai-2-readiness、无废弃 API/硬编码密钥/TODO）。

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

- ✓ **REQ-phase-1-scaffold** — Phase 1：父 POM、common、docker profiles、scripts、docs/00-overview（含 ADR-001~006）、examples/projects README 清单与蓝图
- ✓ **REQ-phase-2-tutorials-starter** — Phase 2：22 章教程、saa-learning-starter（审计/路由/成本）、QA 脚本（version-audit、spring-ai-2-readiness）
- ✓ **REQ-phase-3-demos** — Phase 3：48 个独立 Demo（examples/01~48），UAT 48/48 通过，编号/端口以 examples/README.md 为 SSOT
- ✓ **REQ-phase-4-knowledge-qa** — Phase 4：knowledge-qa-platform（端口 19100）企业知识库问答 — Validated in Phase 4
- ✓ **REQ-phase-5-office-agent** — Phase 5：office-agent-assistant（端口 19200）办公 Agent 助手 — Validated in Phase 5
- ✓ **REQ-phase-6-smart-cs** — Phase 6：smart-cs-platform（端口 19300）智能客服 Agent 平台（FAQ/多智能体/工单 HITL/运营看板）— Validated in Phase 6（代码侧 27/28；真机 UAT 见 06-HUMAN-UAT.md）
- ✓ **REQ-phase-7-production** — Phase 7：CI/CD（GitHub Actions）、`quality-gate.sh`、Compose 部署路径与 UAT 债务索引 — Validated in Phase 7（自动化 12/12；真机项见 07-HUMAN-UAT.md）

### Active

<!-- Current scope. Building toward these. -->

（无 Active v1 需求 — Phase 7 已收口；后续见里程碑完成 / v2）

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Ollama / 本地模型 / GPU — ADR-003 锁定全云端 DashScope + DeepSeek
- Spring Boot 4 / Spring AI 2.0 — SAA 无对应兼容版，锁死 Boot 3.5.x（ADR-002）
- Gradle — ADR-005 锁定 Maven 多模块
- 重做 Phase 1–2 — 已交付并验证，仅登记不重规划
- 偏离 examples/README.md 的 Demo 编号/命名/端口 — 清单为 SSOT

## Context

- **当前阶段：** v1.0 Full Delivery 全部 7 阶段已标记 Complete。待人工 UAT 债务与可选远程 CI 确认后可 `/gsd-complete-milestone`。
- **已交付：** common/starter、48 Demo、三企业项目、`scripts/quality-gate.sh`、`.github/workflows/ci.yml` + `model-it.yml`、生产化文档与 UAT 索引。
- **已知欠账：** Phase 4/5/6/7 人工 UAT；Phase 6 `06-REVIEW.md` Critical（会话→HITL/工单隔离）建议 `/gsd-code-review 6 --fix`；远程 Actions 首次实跑可选。
- **工程约定：** 包根 `com.flywhl.saa`、端口 `180NN`、密钥仅环境变量、复用 common/starter、禁用废弃 API；项目 skill `.claude/skills/saa-conventions/SKILL.md`。
- **质量门禁：** `bash scripts/quality-gate.sh`（本地与 CI 共用入口）。

## Constraints

- **Tech stack**: Java 21 · Spring Boot 3.5.16 · SAA 1.1.2.2 · SAA Extensions 1.1.2.2 · Spring AI 1.1.2 — 父 POM 唯一真源，勿回退
- **BOM**: 必须同时导入 `spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom` — 主 BOM 单独不能管理 starter-dashscope
- **Models**: DashScope 主通道 + DeepSeek 副通道，禁止本地模型 — ADR-003
- **Vector stores**: 教学覆盖 Milvus/pgvector/Redis/ES；项目一 Milvus、项目二 pgvector、项目三 Milvus+Redis — ADR-004
- **Build**: Maven 多模块，子模块零版本号，依赖方向 projects/examples → starter → common — ADR-005
- **API docs**: SpringDoc + Knife4j，统一 `/doc.html` — ADR-006
- **Secrets**: 仅经环境变量（`AI_DASHSCOPE_API_KEY` / `DEEPSEEK_API_KEY`），严禁提交
- **Demo protocol**: 48 个 Demo，端口 `180NN`，README/api.http/curl 齐全 — examples/README.md SSOT

## Key Decisions

<!-- ADR-001..006 locked. Do not reopen without explicit discussion. -->

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| ADR-001: SAA 1.1.2.2 | 官方最新稳定版；Spring AI 超集，前半标准教学、后半 Agent/Graph 增值 | ✓ Locked |
| ADR-002: Boot 3.5.16（非 4.x） | 官方兼容矩阵内全 GA；SAA 无 Boot 4 / Spring AI 2.0 对应版 | ✓ Locked |
| ADR-003: DashScope 主 + DeepSeek 副 | 全云端、无 GPU；Embedding 统一 DashScope 保证向量空间一致 | ✓ Locked |
| ADR-004: Milvus / pgvector / Redis（按项目） | 教学全覆盖 VectorStore 抽象；三项目各有主库选型 | ✓ Locked |
| ADR-005: Maven 多模块 + 四 BOM 叠加 | 父 POM 唯一版本入口；子模块禁止版本号 | ✓ Locked |
| ADR-006: SpringDoc + Knife4j `/doc.html` | 调试体验统一；Knife4j 不兼容时可退化为 swagger-ui | ✓ Locked |

---
*Last updated: 2026-07-17 after Phase 7 (生产化) — v1.0 phases complete*
