# Requirements

Brownfield registration: Phase 1–2 are VALIDATED (delivered). Active v1 work starts at Phase 3.
No PRD-typed sources in ingest set; requirements derived from phase deliverables and acceptance criteria stated in source docs. Do not invent beyond sources.

---

## REQ-phase-1-scaffold

- **status:** VALIDATED (done)
- **source:** HANDOFF-TO-CLAUDE-CODE.md (§0, §2 Phase 1); CLAUDE.md; docs/00-overview/01-学习路线.md (§1 Phase 1)
- **scope:** Phase 1 · 基座
- **description:** 脚手架与调研：父 pom.xml、common 模块（含单测）、docker/docker-compose.yml（profiles）、scripts/、docs/00-overview/（含 ADR-001~006）、examples/README.md 清单、projects/README.md 蓝图、根 README.md。
- **acceptance criteria:**
  - 父工程与 common 可构建；
  - docker profiles（core/vector/mq/search/cloud）可用；
  - 四份 overview 文档与 ADR-001~006 已落地。

---

## REQ-phase-2-tutorials-starter

- **status:** VALIDATED (done)
- **source:** HANDOFF-TO-CLAUDE-CODE.md (§0, §2 Phase 2); CLAUDE.md; docs/00-overview/01-学习路线.md (§1 Phase 2, §3)
- **scope:** Phase 2 · 教程 docs 01~22 + starter + QA
- **description:** 22 章教材级教程（docs/tutorial/01~22）、saa-learning-starter（审计 Advisor、模型路由降级、成本采集）、QA 脚本（version-audit.sh、spring-ai-2-readiness.sh）。
- **acceptance criteria:**
  - 22 章通过骨架/代码围栏/版本一致性/端口无冲突/下一章预告链条检查；
  - starter 模块完整实现（autoconfigure、AuditLoggingAdvisor、ModelRouter/FallbackModelRouter、CostRecorder 等）；
  - 已知欠账：starter 尚未真机 `mvn compile`（沙箱限制），落地首检须补编译与单测。

---

## REQ-phase-3-demos

- **status:** ACTIVE (v1)
- **source:** examples/README.md; HANDOFF-TO-CLAUDE-CODE.md (§3); docs/00-overview/01-学习路线.md (§1 Phase 3)
- **scope:** Phase 3 · 48 个独立 Demo（examples/）
- **description:** 将教程各章核心 API 落成 48 个可独立 `mvn spring-boot:run` 的最小 Demo，编号/命名/端口以 examples/README.md 为 SSOT，不得偏离。
- **acceptance criteria:** (source: HANDOFF-TO-CLAUDE-CODE.md §3; examples/README.md §1)
  1. 独立 `pom.xml`（parent 指向仓库父 POM，零版本号）；
  2. 端口 `18000 + Demo 编号`（即 `180NN`），与既有 Demo 不冲突；Server/Client 配对时 Client 用 `+100` 偏移；
  3. 依赖中间件用 `bash scripts/infra.sh up <profiles>`；
  4. `application.yml` 用 `${AI_DASHSCOPE_API_KEY}` / `DEEPSEEK_API_KEY` 注入，绝不硬编码；
  5. 至少一个 REST 入口 + curl 验证命令 + 预期输出；附独立 README、`api.http`、运行结果说明；
  6. 模型调用集成测试用 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")`；中间件用 Testcontainers；
  7. 复用 `saa-learning-common` 与 `saa-learning-starter`，不重复造轮子；
  8. 代码零 TODO/零伪代码；禁用已废弃 API（见 CLAUDE.md / HANDOFF §1.4）。

---

## REQ-phase-4-knowledge-qa

- **status:** ACTIVE (v1)
- **source:** projects/README.md（项目一）; docs/00-overview/01-学习路线.md (§4)
- **scope:** Phase 4 · knowledge-qa-platform（端口 19100）
- **description:** AI 企业知识库问答平台：制度/手册/技术文档统一问答入口；管理员维护知识，员工获得带引用溯源的答案。
- **acceptance criteria:** (source: projects/README.md 统一交付标准 + 项目一技术落点)
  - 完整源码 + 数据库脚本（含演示数据）+ docker-compose.override.yml + 完整文档/README/部署说明；
  - OpenAPI/Knife4j 完整接口；单测 + Testcontainers 集成测试；
  - 知识上传/解析（MinIO + Spring AI ETL）；Embedding/Chunk（DashScope + TokenTextSplitter）；
  - RAG + Citation（RetrievalAugmentationAdvisor + 自定义引用溯源）；
  - 多模型切换（DashScope 主 / DeepSeek 备）；Redis ChatMemory；Prompt 管理（DB 版本化 + Nacos 热更新）；
  - Spring Security + 审计日志 + Micrometer；SSE 流式问答；
  - 数据库：PostgreSQL（业务）+ Milvus（向量）+ Redis（记忆/缓存）。

---

## REQ-phase-5-office-agent

- **status:** ACTIVE (v1)
- **source:** projects/README.md（项目二）; docs/00-overview/01-学习路线.md (§4)
- **scope:** Phase 5 · office-agent-assistant（端口 19200）
- **description:** 企业 AI Agent 办公助手：会议纪要、日报、邮件起草、数据查询、日程与审批协助。
- **acceptance criteria:** (source: projects/README.md 统一交付标准 + 项目二技术落点)
  - 完整源码 + 数据库脚本 + compose 叠加 + 文档/OpenAPI/测试（同统一交付标准）；
  - Prompt 模板族 + 结构化输出（Record/JSON Schema）；
  - SQL/HTTP/Excel/Calendar Tool（@Tool + 权限校验 + SQL 防注入）；
  - MCP：企业工具以 MCP Server 暴露，助手为 MCP Client；
  - 审批助手：Agent Framework SequentialAgent/RoutingAgent；
  - 记忆：Redis（会话）+ JDBC（用户长期偏好）；用户/Prompt 管理 CRUD；
  - 数据库：MySQL（业务）+ pgvector + Redis。

---

## REQ-phase-6-smart-cs

- **status:** ACTIVE (v1)
- **source:** projects/README.md（项目三）; docs/00-overview/01-学习路线.md (§4)
- **scope:** Phase 6 · smart-cs-platform（端口 19300）
- **description:** 智能客服 Agent 平台：FAQ 秒答、多智能体协作、工单流转、人工接管、运营看板。
- **acceptance criteria:** (source: projects/README.md 统一交付标准 + 项目三技术落点)
  - 完整源码 + 数据库脚本 + compose 叠加 + 文档/OpenAPI/测试（同统一交付标准）；
  - RoutingAgent + Supervisor + Handoffs（并行子智能体）；
  - FAQ/知识库/RAG：Milvus + Redis 语义缓存 + ES 全文混合检索；
  - 工单/人工接管：工单域模型 + Graph interrupt（HITL）+ 接管状态机；
  - 监控/统计/成本：Micrometer + Prometheus + Grafana + Token 成本统计；
  - 模型/Prompt 管理后台 CRUD + Nacos 热更新 + 多模型配置化路由；
  - 数据库：PostgreSQL + Milvus + Redis + ES；配置与注册：Nacos。

---

## REQ-phase-7-production

- **status:** ACTIVE (v1)
- **source:** HANDOFF-TO-CLAUDE-CODE.md (§3 后续阶段); docs/00-overview/01-学习路线.md (§1 Phase 7)
- **scope:** Phase 7 · 生产化
- **description:** 统一测试、CI/CD、部署、调优与排障。
- **acceptance criteria:** (source: docs/00-overview/01-学习路线.md Phase 7; HANDOFF-TO-CLAUDE-CODE.md §5.2, §7)
  - CI/CD 流水线与部署脚本落地；
  - 质量门禁可在每阶段收口执行（编译、curl 验证、version-audit、spring-ai-2-readiness、无废弃 API/硬编码密钥/TODO）。
