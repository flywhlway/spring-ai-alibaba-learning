# Constraints (SPEC)

Extracted from SPEC-typed sources and structural constraints stated in architecture docs.

---

## Phase 3 Demo inventory and conventions

- **type:** protocol
- **source:** examples/README.md
- **scope:** examples/, Phase 3, 48 demos, ports 180NN

**content:**
- Phase 3 交付 **48** 个完全独立的 Demo 工程；本 README 是 Demo 清单与规范的 SSOT，实现时不得偏离编号与命名。
- 每个 Demo = 独立 Maven 工程（`<parent>` 指向仓库父 POM，零版本号）+ 独立 README + `api.http` + curl 示例 + Postman Collection + 运行结果说明与截图。
- 端口：`18000 + Demo 编号`（如 04 号 chat-demo → 18004）。
- 依赖中间件在各自 README 顶部声明所需 profile（`bash scripts/infra.sh up <profiles>`）。
- 模型 Key 统一环境变量：`AI_DASHSCOPE_API_KEY` / `DEEPSEEK_API_KEY`。
- Demo README 统一模板（前置条件 / 运行 / 接口 / 快速验证 / 源码导读 / 运行结果）。

**Demo 清单（48，编号固定）：**
01 quickstart-demo · 02 autoconfig-demo · 03 multi-model-demo · 04 chat-demo · 05 retry-demo · 06 prompt-demo · 07 prompt-builder-demo · 08 prompt-nacos-demo · 09 advisor-demo · 10 custom-advisor-demo · 11 tool-demo · 12 dynamic-tool-demo · 13 http-tool-demo · 14 db-tool-demo · 15 tool-security-demo · 16 memory-demo · 17 redis-memory-demo · 18 jdbc-memory-demo · 19 summary-memory-demo · 20 structured-output-demo · 21 json-schema-demo · 22 embedding-demo · 23 pgvector-demo · 24 milvus-demo · 25 redis-vector-demo · 26 es-hybrid-demo · 27 rag-demo · 28 advanced-rag-demo · 29 hybrid-rag-demo · 30 rag-eval-demo · 31 mcp-server-demo · 32 mcp-client-demo · 33 mcp-auth-demo · 34 mcp-nacos-demo · 35 agent-demo · 36 agent-skills-demo · 37 agent-hitl-demo · 38 workflow-demo · 39 graph-parallel-demo · 40 graph-saga-demo · 41 multi-agent-demo · 42 supervisor-demo · 43 a2a-nacos-demo · 44 stream-demo · 45 observability-demo · 46 logging-demo · 47 routing-demo · 48 fallback-demo

---

## Phase 4–6 enterprise project blueprints

- **type:** nfr
- **source:** projects/README.md
- **scope:** projects/, Phase 4–6, three enterprise projects

**content:**
- Phase 4~6 交付三个真实企业业务项目（非 Todo Demo）；本 README 是蓝图 SSOT：业务边界、技术映射、目录骨架锁定，实现阶段不得偏离。
- **统一交付标准：** 完整源码 + 完整数据库脚本（含演示数据）+ 独立 docker-compose 叠加文件 + 完整文档 + 完整接口（OpenAPI/Knife4j）+ 完整 README + 完整部署说明 + 完整测试（单测 + Testcontainers 集成测试）。

**项目一 knowledge-qa-platform（Phase 4，端口 19100）：**
- 业务：企业内部制度/手册/技术文档统一问答；管理员维护知识，员工获带引用溯源答案。
- 技术：MinIO + ETL；DashScope embedding；RAG+Citation；多模型路由；Redis ChatMemory；Prompt DB+Nacos；Security+审计+Micrometer；SSE。
- DB：PostgreSQL + Milvus + Redis。

**项目二 office-agent-assistant（Phase 5，端口 19200）：**
- 业务：会议纪要/日报/邮件/数据查询/日程与审批协助。
- 技术：Prompt 模板+结构化输出；SQL/HTTP/Excel/Calendar Tools；MCP Client；Agent 编排；Redis+JDBC 记忆；用户/Prompt CRUD。
- DB：MySQL + pgvector + Redis。

**项目三 smart-cs-platform（Phase 6，端口 19300）：**
- 业务：FAQ 秒答、多智能体协作、工单、人工接管、运营看板。
- 技术：RoutingAgent+Supervisor+Handoffs；Milvus+Redis 语义缓存+ES；HITL；Micrometer/Prometheus/Grafana；Nacos。
- DB：PostgreSQL + Milvus + Redis + ES；配置/注册：Nacos。

**共用骨架：**
```
projects/<project-name>/
├── pom.xml
├── README.md
├── docker-compose.override.yml
├── db/
├── http/
└── src/main/java/com/flywhl/saa/<project>/
    ├── controller / service / config
    ├── model / mapper
    ├── agent / tool / rag / prompt
    └── admin
```

---

## Repository structure and module dependency rules

- **type:** protocol
- **source:** docs/00-overview/03-总体架构与目录规划.md
- **scope:** repository layout, module dependencies, SSOT

**content:**
- 目录：docs/、common/、starter/、examples/、projects/、scripts/、docker/、images/；父 pom.xml 为唯一版本管理入口。
- 模块挂载：common 常驻；starter 已挂载；examples/* 与 projects/* 为独立 Spring Boot 应用，以 parent 继承版本，按阶段逐个挂入 modules。
- 依赖方向只允许自上而下（projects/examples → starter → common），严禁反向与横向依赖。
- SSOT：版本→父 pom；选型依据→02-版本调研报告；Result/异常→common；AI 装配→starter；中间件→docker-compose profiles；API Key→环境变量；Demo README 模板→examples/README.md §3。

---

## Coding and API protocol conventions

- **type:** api-contract
- **source:** docs/00-overview/03-总体架构与目录规划.md (§5); HANDOFF-TO-CLAUDE-CODE.md (§1.4); CLAUDE.md
- **scope:** all modules

**content:**
- 包根 `com.flywhl.saa`；作者 `@author flywhl`。
- 同步接口返回 `Result<T>`（code=0 成功）；分页 `Result<PageResult<T>>`；流式 `text/event-stream`（message/meta/error/done）。
- OpenAPI 注解；Knife4j 路径 `/doc.html`。
- 配置统一 `application.yml`；敏感项 `${ENV_VAR}`。
- 端口：examples `180NN`；projects `19100/19200/19300`。
- 测试：JUnit 5 + AssertJ；中间件 Testcontainers；模型调用 `@EnabledIfEnvironmentVariable`。
- 禁用废弃 API：`PromptChatMemoryAdvisor`→`MessageChatMemoryAdvisor`；`CallAroundAdvisor`/`AdvisedRequest`/`AdvisedResponse`→`CallAdvisor`/`ChatClientRequest`/`ChatClientResponse`；`FunctionCallback`→`@Tool`/`ToolCallback`；可变 Options setter→Builder。
- 图示一律 Mermaid；代码零 TODO/零伪代码。

---

## Version lock (aligned with ADR, informational in SPEC/DOC)

- **type:** nfr
- **source:** docs/00-overview/02-版本调研报告.md; CLAUDE.md; HANDOFF-TO-CLAUDE-CODE.md (§1.1)
- **scope:** parent POM versions

**content:**
- Java 21 · Spring Boot 3.5.16 · SAA 1.1.2.2 · SAA Extensions 1.1.2.2 · Spring AI 1.1.2
- Lombok 1.18.36 · MapStruct 1.6.3 · springdoc 2.8.9 · knife4j 4.5.0
- 两个 BOM 必须同时导入：`spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom`
- Spring AI 2.0 已 GA 但 SAA 无对齐版 → 锁死 Boot 3.5.x，勿升 Boot 4 / Spring AI 2.0
- 模型：DashScope + DeepSeek（全云端）；向量库：Milvus 2.5.x / pgvector (PG16) / Redis；注册配置：Nacos 3.0.x
