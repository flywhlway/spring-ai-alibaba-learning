# Roadmap: spring-ai-alibaba-learning

## Overview

从脚手架与教程基座出发，交付 48 个可独立运行的 SAA Demo，再落地三个企业项目（知识库问答 → 办公 Agent → 智能客服），最后以 CI/CD、部署与质量门禁完成生产化收口。Phase 1–2 已交付；当前从 Phase 3（48 demos）推进。

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

- [x] **Phase 1: 基座脚手架** - 父 POM、common、docker profiles、scripts、overview 文档与 ADR
- [x] **Phase 2: 教程与 starter** - 22 章教程、saa-learning-starter、QA 脚本
- [ ] **Phase 3: 48 个独立 Demo** - examples/ 全量可 `mvn spring-boot:run` 的最小 Demo
- [ ] **Phase 4: 知识库问答平台** - knowledge-qa-platform（端口 19100）
- [ ] **Phase 5: 办公 Agent 助手** - office-agent-assistant（端口 19200）
- [ ] **Phase 6: 智能客服平台** - smart-cs-platform（端口 19300）
- [ ] **Phase 7: 生产化** - CI/CD、部署、调优、排障与质量门禁收口

## Phase Details

### Phase 1: 基座脚手架
**Goal**: 仓库具备可构建的父工程、公共模块、中间件编排与选型文档，后续阶段可直接挂载模块
**Depends on**: Nothing (first phase)
**Requirements**: REQ-phase-1-scaffold
**Success Criteria** (what must be TRUE):
  1. 学习者可构建父工程与 `saa-learning-common`（含单测）
  2. 学习者可用 `bash scripts/infra.sh up` 按 profile（core/vector/mq/search/cloud）启动中间件
  3. docs/00-overview/ 四份文档与 ADR-001~006 已落地，examples/README.md 与 projects/README.md 清单/蓝图可读
**Plans**: Delivered (brownfield)
**Status**: Complete

### Phase 2: 教程与 starter
**Goal**: 学习者有完整 22 章教程与可复用的 starter（审计/路由/成本），以及版本与 2.0 就绪自检脚本
**Depends on**: Phase 1
**Requirements**: REQ-phase-2-tutorials-starter
**Success Criteria** (what must be TRUE):
  1. docs/tutorial/01~22 通过骨架/代码围栏/版本一致性/端口无冲突/下一章预告链条检查
  2. `saa-learning-starter` 提供 AuditLoggingAdvisor、ModelRouter/FallbackModelRouter、CostRecorder 等装配能力
  3. `bash scripts/version-audit.sh` 与 `bash scripts/spring-ai-2-readiness.sh` 可执行
**Plans**: Delivered (brownfield)
**Status**: Complete
**Notes**: starter 真机编译与单测为落地首检欠账（沙箱限制），在 Phase 3 启动前补齐

### Phase 3: 48 个独立 Demo
**Goal**: 学习者可对 examples/ 中全部 48 个 Demo 独立 `mvn spring-boot:run`，并用 curl 得到与章节一致的预期输出，且通过 HANDOFF §7 质量门禁
**Depends on**: Phase 2
**Requirements**: REQ-phase-3-demos
**Success Criteria** (what must be TRUE):
  1. examples/README.md 清单中的 48 个 Demo（01-quickstart-demo … 48-fallback-demo）均存在独立工程，parent 指向仓库父 POM、子模块零版本号
  2. 每个 Demo 端口为 `180NN`（Server/Client 配对时 Client = Server+100），彼此无冲突；密钥仅经 `${AI_DASHSCOPE_API_KEY}` / `DEEPSEEK_API_KEY` 注入
  3. 每个 Demo 具备独立 README、`api.http`、至少一个 REST 入口与 curl 验证命令及预期输出；`mvn spring-boot:run` 可跑，中间件依赖通过 `bash scripts/infra.sh up <profiles>` 声明
  4. 模型调用集成测试使用 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")`，中间件用 Testcontainers；复用 `saa-learning-common` 与 `saa-learning-starter`
  5. 全量 Demo 通过 HANDOFF §7 门禁：真实编译、version-audit 全绿、spring-ai-2-readiness 低位、无废弃 API/硬编码密钥/TODO/伪代码
**Plans**: 3/TBD（首批扩展 01~19 已交付：03-01/02/03）
**Plan priority note** (for `/gsd-plan-phase 3`):
  1. ~~首批：章节 01~08 基础 + advisor/tool/memory（09~19）~~ ✅ 2026-07-04（含限额中断 UAT 闭环）
  2. 次批：RAG / Embedding / VectorStore / MCP（20~34）
  3. 再次：Agent / Graph / Multi-Agent（35~43）
  4. 末批：best-practice / migration 相关（44~48 stream/observability/logging/routing/fallback）

**Demo inventory (SSOT: examples/README.md):**
01 quickstart · 02 autoconfig · 03 multi-model · 04 chat · 05 retry · 06 prompt · 07 prompt-builder · 08 prompt-nacos · 09 advisor · 10 custom-advisor · 11 tool · 12 dynamic-tool · 13 http-tool · 14 db-tool · 15 tool-security · 16 memory · 17 redis-memory · 18 jdbc-memory · 19 summary-memory · 20 structured-output · 21 json-schema · 22 embedding · 23 pgvector · 24 milvus · 25 redis-vector · 26 es-hybrid · 27 rag · 28 advanced-rag · 29 hybrid-rag · 30 rag-eval · 31 mcp-server · 32 mcp-client · 33 mcp-auth · 34 mcp-nacos · 35 agent · 36 agent-skills · 37 agent-hitl · 38 workflow · 39 graph-parallel · 40 graph-saga · 41 multi-agent · 42 supervisor · 43 a2a-nacos · 44 stream · 45 observability · 46 logging · 47 routing · 48 fallback

### Phase 4: 知识库问答平台
**Goal**: 企业用户可通过 knowledge-qa-platform 上传知识并获得带引用溯源的流式问答
**Depends on**: Phase 3
**Requirements**: REQ-phase-4-knowledge-qa
**Success Criteria** (what must be TRUE):
  1. 管理员可上传/解析知识文档（MinIO + Spring AI ETL），员工可对制度/手册/技术文档提问并获得带 Citation 的答案
  2. 问答支持 SSE 流式输出、多模型切换（DashScope 主 / DeepSeek 备）、Redis ChatMemory
  3. 工程满足统一交付标准：完整源码、DB 脚本（含演示数据）、docker-compose.override.yml、OpenAPI/Knife4j、单测 + Testcontainers、部署说明；端口 19100
  4. 运行栈为 PostgreSQL（业务）+ Milvus（向量）+ Redis（记忆/缓存），含 Security、审计日志与 Micrometer
**Plans**: TBD

### Phase 5: 办公 Agent 助手
**Goal**: 企业用户可通过 office-agent-assistant 完成会议纪要、日报、邮件起草、数据查询与审批协助
**Depends on**: Phase 4
**Requirements**: REQ-phase-5-office-agent
**Success Criteria** (what must be TRUE):
  1. 用户可调用 Prompt 模板族获得结构化输出（Record/JSON Schema）完成纪要/日报/邮件等办公任务
  2. Agent 可安全使用 SQL/HTTP/Excel/Calendar Tools（权限校验 + SQL 防注入），并通过 MCP Client 调用企业 MCP Server 工具
  3. 审批助手走 SequentialAgent/RoutingAgent；记忆为 Redis（会话）+ JDBC（长期偏好）；用户/Prompt 管理 CRUD 可用
  4. 工程满足统一交付标准；端口 19200；栈为 MySQL + pgvector + Redis
**Plans**: TBD

### Phase 6: 智能客服平台
**Goal**: 客服场景可通过 smart-cs-platform 完成 FAQ 秒答、多智能体协作、工单流转与人工接管，并具备运营看板
**Depends on**: Phase 5
**Requirements**: REQ-phase-6-smart-cs
**Success Criteria** (what must be TRUE):
  1. FAQ/知识库问答可用（Milvus + Redis 语义缓存 + ES 全文混合检索）
  2. RoutingAgent + Supervisor + Handoffs 驱动多智能体协作；工单域模型与 Graph interrupt（HITL）支持人工接管状态机
  3. 运营可查看监控/统计/成本（Micrometer + Prometheus + Grafana + Token 成本）；模型/Prompt 后台 CRUD + Nacos 热更新
  4. 工程满足统一交付标准；端口 19300；栈为 PostgreSQL + Milvus + Redis + ES + Nacos
**Plans**: TBD

### Phase 7: 生产化
**Goal**: 全仓具备可重复执行的 CI/CD、部署与质量门禁，任何阶段收口均可验证交付物达标
**Depends on**: Phase 6
**Requirements**: REQ-phase-7-production
**Success Criteria** (what must be TRUE):
  1. CI/CD 流水线与部署脚本可对 common/starter/examples/projects 执行构建与发布路径
  2. 质量门禁可一键执行：真实编译、curl 验证、version-audit 全绿、spring-ai-2-readiness 低位
  3. 门禁扫描确认无废弃 API、无硬编码密钥、无 TODO/伪代码
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. 基座脚手架 | delivered | Complete | 2026-07-03 |
| 2. 教程与 starter | delivered | Complete | 2026-07-03 |
| 3. 48 个独立 Demo | 3/TBD（batch1 01~19） | In progress | - |
| 4. 知识库问答平台 | 0/TBD | Not started | - |
| 5. 办公 Agent 助手 | 0/TBD | Not started | - |
| 6. 智能客服平台 | 0/TBD | Not started | - |
| 7. 生产化 | 0/TBD | Not started | - |
