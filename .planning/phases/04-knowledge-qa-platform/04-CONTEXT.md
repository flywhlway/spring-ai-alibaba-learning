# Phase 4: 知识库问答平台 - Context

**Gathered:** 2026-07-05
**Status:** Ready for planning
**Mode:** `--auto`（骨架已实现，本阶段交付全部占位代码 + 严格质量/功能验收）

<domain>
## Phase Boundary

在 **已完成工程骨架**（`projects/knowledge-qa-platform`：pom / application.yml / DDL+演示数据 / docker-compose.override / http 契约 / Java 占位类）基础上，交付可 `mvn spring-boot:run` 的企业知识库问答平台（端口 **19100**）。

**本阶段必须 TRUE：**
1. 管理员上传/解析知识文档（MinIO + Spring AI ETL），员工对制度/手册/技术文档提问并获得带 **Citation** 的答案；
2. 问答支持 **SSE 流式**、多模型切换（DashScope 主 / DeepSeek 备）、**Redis ChatMemory**；
3. 满足统一交付标准：完整源码、DB 脚本（含演示数据）、docker-compose.override、OpenAPI/Knife4j、单测 + Testcontainers、部署说明；
4. 运行栈 PostgreSQL（业务）+ Milvus（向量）+ Redis（记忆/缓存），含 Security、审计日志与 Micrometer；
5. 通过 **HANDOFF §7** 质量门禁 + 项目 README 接口契约 curl 验收。

**不在本阶段：** 独立前端 SPA、多租户 SaaS、细粒度文档 ACL、Rerank 专用模型服务、办公 Agent / 智能客服能力（Phase 5/6）。

</domain>

<decisions>
## Implementation Decisions

### 交付策略（棕地续作）
- **D-01:** 以 `projects/knowledge-qa-platform/README.md` 为 **接口与架构 SSOT**；占位类全部替换为可运行实现，删除「骨架占位」注释，零 TODO/伪代码。
- **D-02:** 不改动已锁定的骨架资产：`db/schema.sql` + `data.sql`、`application.yml` 键名、`docker-compose.override.yml`、`http/api.http` 路径与权限矩阵——实现必须对齐而非重设计契约。
- **D-03:** 父 POM `<modules>` 仍不挂载本项目；独立 `mvn -f projects/knowledge-qa-platform/pom.xml` 构建运行（与 examples 一致）。
- **D-04:** 实现波次按 README §9 迭代清单拆 plan（5 波），每波结束 `mvn compile` 门禁，末波全量验收：
  1. `config/*`（Security JWT / AI / VectorStore / Memory / MinIO / OpenAPI）
  2. `rag/*`（ETL 异步流水线 + RAG 管线 + Citation）
  3. `controller` + `service`（认证 / 问答 SSE / 会话 / 反馈）
  4. `admin/*`（知识 / Prompt / 用户 / 审计 / 看板）
  5. 测试 + HANDOFF §7 门禁 + curl UAT

### 工程约定（继承 Phase 1–3，不可偏离）
- **D-05:** 包根 `com.flywhl.saa.knowledgeqa`，`@author flywhl`；复用 `saa-learning-common`（`Result`/`PageResult`/`BizException`/`GlobalExceptionHandler`）+ `saa-learning-starter`（`AuditLoggingAdvisor`/`FallbackModelRouter`/`CostRecorder`）。
- **D-06:** 禁用废弃 API：`PromptChatMemoryAdvisor`、`CallAroundAdvisor`/`AdvisedRequest`/`AdvisedResponse`、`FunctionCallback`、可变 Options setter。
- **D-07:** 密钥仅环境变量；`MessageChatMemoryAdvisor` + 显式 `conversationId`；Options 一律 Builder。
- **D-08:** Entity/DTO/VO 字段与 `db/schema.sql` 列名对齐；JPA `ddl-auto=none`。

### RAG 管线
- **D-09:** 采用 **Modular RAG**（对齐 `examples/28-advanced-rag-demo`）：`RetrievalAugmentationAdvisor` + `RewriteQueryTransformer` + `VectorStoreDocumentRetriever`。
- **D-10:** 检索参数绑定 `kqa.rag.*`：`top-k=5`、`similarity-threshold=0.35`；`DocumentPostProcessor` 按 score 降序截断 Top-K；`ContextualQueryAugmenter.allowEmptyContext(false)`——无召回时明确拒答，不编造。
- **D-11:** Embedding 保持骨架配置 **`text-embedding-v3`（1024 维）**，与 Milvus `embedding-dimension: 1024` 一致；Chunk 用 `TokenTextSplitter`（`chunk-size=512`、`chunk-overlap=64`）。
- **D-12:** Milvus collection `kqa_knowledge`；Chunk 向量 ID 与 PostgreSQL `kb_chunk.milvus_pk` 一一对应，供溯源回查。

### Citation 与 SSE 协议
- **D-13:** 同步 `/api/qa/ask` 返回 `QaAnswerVO`：`answer` + `citations[]`（`documentId`/`documentTitle`/`chunkId`/`snippet`/`score`）。
- **D-14:** 流式 `/api/qa/stream` 严格按 README §6：`message`（增量文本）→ `meta`（citations + token usage）→ `done`；错误走 `error` 事件（payload 复用 `Result`）。
- **D-15:** `CitationPostProcessor` 从 RAG 检索 `Document` metadata（`documentId`/`chunkId`/`title`）组装 `CitationVO`；答案正文不内联 `[1]` 脚注，引用独立在 citations 字段/事件中。

### 文档入库（ETL）
- **D-16:** 上传接口同步写 MinIO + `kb_document`（`UPLOADED`），解析索引 **异步**（`@Async` + `AsyncConfig`），状态机：`UPLOADED → PARSING → INDEXED | FAILED`；`IngestStatusTracker` 供管理端轮询/列表展示。
- **D-17:** 解析链：MinIO 下载 → Tika `DocumentReader` → `TokenTextSplitter` → Embedding → Milvus add + `kb_chunk` 批量落库；失败写 `fail_reason`。
- **D-18:** 删除/重建索引：级联删 Milvus 向量 + `kb_chunk`；`reindex` 重跑 ETL。

### 多模型 / 记忆 / Prompt
- **D-19:** `AiClientConfig` 装配 `ChatClient`：`defaultAdvisors` = `MessageChatMemoryAdvisor` + `RetrievalAugmentationAdvisor` + starter `AuditLoggingAdvisor`；底层 `ChatModel` 经 `FallbackModelRouter`（`saa.learning.primary-model`/`fallback-model`）。
- **D-20:** Redis ChatMemory：`max-messages=20`、`ttl=7d`；删除会话时清 Redis key + 软删/硬删 PG 归档（按 README 会话 DELETE 语义）。
- **D-21:** `PromptTemplateProvider` 三级回退：**Nacos 热更新 → DB `PUBLISHED` → classpath `prompts/*.st`**；`PromptPublishService` 发布时推 Nacos Data ID `spring.ai.alibaba.configurable.prompt`。
- **D-22:** 查询改写 prompt 使用 `prompts/query-rewrite.st`；系统 prompt 使用 `qa-system.st`，均可通过 Prompt 管理后台版本化。

### 安全与权限
- **D-23:** Spring Security **OAuth2 Resource Server**（Nimbus JWT）；`/api/auth/login` 匿名签发 JWT；`/actuator/health` 匿名；其余按 README 权限矩阵（`ADMIN` / `EMPLOYEE`）。
- **D-24:** 演示账号 `{noop}` 口令仅 `data.sql`；新建用户 **BCrypt**；`DelegatingPasswordEncoder` 兼容两者。
- **D-25:** `KnowledgeOpsTools`（`@Tool`）仅 ADMIN 角色经 `ToolContext` 校验，暴露文档状态查询/触发重建等管理操作（可选挂载到问答 Agent，非必须独立 REST）。

### 后台域
- **D-26:** 五组 admin API 全量实现（文档/Prompt/用户/审计/看板），分页统一 `Result<PageResult<T>>`；审计双轨：DB `audit_log` + starter 模型调用审计。
- **D-27:** 看板 `DashboardStatsVO`：问答量、Token 成本（starter `CostRecorder`/`gen_ai.usage.*`）、反馈满意度、知识规模（文档数/chunk 数）。

### 测试与验收
- **D-28:** 单测 + 集成测：PostgreSQL/Redis **Testcontainers**；Milvus IT 可选（文档声明手动 infra，不阻塞无 Docker 全绿）。
- **D-29:** 真实模型 IT：`@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")` 覆盖 login、ask、stream 至少各 1 例。
- **D-30:** 阶段收口必过：**HANDOFF §7**（`mvn -f projects/knowledge-qa-platform/pom.xml clean install`、`spring-boot:run` + README curl、`version-audit.sh`、`spring-ai-2-readiness.sh`、无废弃 API/密钥/TODO）。
- **D-31:** 产出 `04-UAT.md`（或等价验收清单），按 `http/api.http` 全接口 curl + 预期输出；演示数据（`data.sql` 预置文档）可完成端到端问答。

### Claude's Discretion
- MapStruct Converter 与 Repository 查询方法的具体命名/派生查询。
- SSE 实现选用 `SseEmitter` 或 `Flux` 包装，只要事件类型契约不变。
- 单元测试覆盖率的细粒度分配（核心路径优先：Auth、ETL、RAG、QaService）。
- Knife4j 分组标签命名。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 项目蓝图与契约
- `projects/README.md` — 三项目 SSOT 蓝图（项目一业务边界与技术映射）
- `projects/knowledge-qa-platform/README.md` — 接口总览、SSE 协议、演示账号、迭代清单、快速开始
- `projects/knowledge-qa-platform/http/api.http` — 全接口 REST Client 契约
- `projects/knowledge-qa-platform/db/schema.sql` — PostgreSQL DDL SSOT（8 表）
- `projects/knowledge-qa-platform/db/data.sql` — 演示数据（用户/Prompt/文档元数据）
- `projects/knowledge-qa-platform/src/main/resources/application.yml` — 中间件与 `kqa.*`/`saa.learning.*` 配置 SSOT
- `projects/knowledge-qa-platform/docker-compose.override.yml` — kqa profile 建库/MinIO bucket 初始化

### 质量与约定
- `HANDOFF-TO-CLAUDE-CODE.md` §7 — 质量门禁清单
- `.claude/skills/saa-conventions/SKILL.md` — 包根/端口/禁用 API/测试约定
- `CLAUDE.md` — 版本锁定、硬约定、禁用 API 速查

### 需求与路线图
- `.planning/ROADMAP.md` — Phase 4 Goal 与 Success Criteria
- `.planning/REQUIREMENTS.md` — REQ-phase-4-knowledge-qa
- `.planning/PROJECT.md` — 全局约束与已验证决策

### 可复用 Demo 参考（只读，不修改）
- `examples/28-advanced-rag-demo/` — Modular RAG（RetrievalAugmentationAdvisor + QueryTransformer）
- `examples/27-rag-demo/` — 基础 RAG + VectorStore
- `examples/24-milvus-demo/` — Milvus VectorStore 配置
- `examples/17-redis-memory-demo/` — Redis ChatMemory 模式
- `examples/44-stream-demo/` — SSE 流式输出
- `examples/47-routing-demo/` / `examples/48-fallback-demo/` — starter 路由降级
- `examples/15-tool-security-demo/` — @Tool + ToolContext 权限
- `starter/src/main/java/com/flywhl/saa/starter/` — AuditLoggingAdvisor / FallbackModelRouter / CostRecorder

### ADR
- `docs/00-overview/ADR-003-multi-model-strategy.md` — DashScope 主 / DeepSeek 备
- `docs/00-overview/ADR-004-vector-store-selection.md` — Milvus 选型
- `docs/00-overview/ADR-006-api-documentation.md` — Knife4j /doc.html

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- **工程骨架**：`projects/knowledge-qa-platform/` 已含 70+ Java 占位类、pom 依赖全集、prompts/*.st、Postman collection。
- **saa-learning-starter**：`FallbackModelRouter`、`AuditLoggingAdvisor`、`CostRecorder`、`SaaLearningAutoConfiguration`（application.yml 已配置 `saa.learning.*`）。
- **saa-learning-common**：`Result`、`PageResult`、`BizException`、`GlobalExceptionHandler`。
- **docker 编排**：`docker/docker-compose.yml` profiles `core`+`vector`+`cloud`+`kqa`；Milvus 冷启动 30~60s。
- **Phase 3 Demos**：28-advanced-rag（RAG 管线）、24-milvus、17-redis-memory、44-stream、47/48 routing/fallback 可直接抄装配模式。

### Established Patterns
- ChatClient + Advisor 链式装配（非废弃 PromptChatMemoryAdvisor）。
- Embedding `text-embedding-v3` 1024 维 + Milvus COSINE（application.yml 已锁定）。
- 企业项目目录：`controller/service/config/rag/admin/model/mapper/repository`（projects/README 共用骨架）。
- 占位类当前 **无 @Component/@Service**——实现时按层添加注解并保证 Bean 图可启动。

### Integration Points
- `KnowledgeQaApplication` 入口；`KqaProperties` 绑定 `kqa.*`。
- MinIO bucket `kqa-documents`；PostgreSQL `kqa_platform`；Milvus `kqa_knowledge`；Nacos prompt 热更新。
- JWT 过滤器链接 SecurityConfig → 各 Controller 方法级 `@PreAuthorize`。

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求：**基于已完成骨架和配置脚本**，继续实现**所有占位代码**，并完成**严格质量和功能验收**（对齐 HANDOFF §7 + README curl）。
- 接口路径、SSE 事件类型、演示账号以项目 README §5–§6 为准，实现阶段不得漂移。
- 预置 `data.sql` 演示文档应能支撑「员工提问 → 带 citation 答案」的 UAT 闭环（无需上传即可验问答）。

</specifics>

<deferred>
## Deferred Ideas

- 独立 Web 管理前端（本阶段仅 REST + Knife4j）
- 文档级/部门级细粒度 ACL（现 RBAC 仅 ADMIN/EMPLOYEE）
- 专用 Rerank 模型服务（现用 score 排序 DocumentPostProcessor）
- 语义答案缓存（Redis 仅会话记忆，不做 FAQ 缓存——留给 Phase 6）
- ES 混合检索（本项目 Milvus 单向量检索即可）

</deferred>

---

*Phase: 4-知识库问答平台*
*Context gathered: 2026-07-05*
