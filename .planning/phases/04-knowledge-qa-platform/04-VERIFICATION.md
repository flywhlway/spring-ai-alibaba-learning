---
phase: 04-knowledge-qa-platform
verified: 2026-07-05T15:42:00Z
status: human_needed
score: 28/29 must-haves verified
overrides_applied: 0
human_verification:
  - test: "启动 infra + 应用后执行 04-UAT.md §2.1 同步问答"
    expected: "code=0，data.answer 含差旅/住宿费相关内容，data.citations 非空"
    why_human: "需 Milvus 向量索引 + DashScope API Key，RAG 召回与 citation 组装无法在静态分析中证明"
  - test: "执行 04-UAT.md §2.2 SSE 流式问答"
    expected: "事件顺序 message（增量文本）→ meta（含 citations）→ done"
    why_human: "SSE 事件序列与增量文本需真实 HTTP 流式响应验证"
  - test: "执行 04-UAT.md §3 管理员上传文档并 reindex"
    expected: "文档状态 UPLOADED→PARSING→INDEXED，问答可召回新文档"
    why_human: "MinIO + 异步 ETL + Milvus 写入需运行中间件"
  - test: "设置 AI_DASHSCOPE_API_KEY 后运行 QaAskIT、QaStreamIT"
    expected: "mvn -f projects/knowledge-qa-platform/pom.xml test 中两 IT 各 PASS"
    why_human: "验证环境无 API Key，IT 被 @EnabledIfEnvironmentVariable 跳过，未在本轮执行"
---

# Phase 4: 知识库问答平台 Verification Report

**Phase Goal:** 企业用户可通过 knowledge-qa-platform 上传知识并获得带引用溯源的流式问答
**Verified:** 2026-07-05T15:42:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| **ROADMAP** | | | |
| 1 | 管理员可上传/解析知识文档（MinIO + Spring AI ETL） | ✓ VERIFIED | `DocumentEtlPipeline`（Tika→Splitter→`vectorStore.add`）、`DocumentAdminService.upload`→`ingestAsync` |
| 2 | 员工提问获带 Citation 的答案 | ✓ VERIFIED（代码） | `QaService.ask` + `CitationPostProcessor.retrieve`；E2E 需人工 |
| 3 | 问答支持 SSE 流式输出 | ✓ VERIFIED | `QaController` `text/event-stream`；`QaService.stream` 发出 message/meta/done |
| 4 | 多模型切换（DashScope 主 / DeepSeek 备） | ✓ VERIFIED | `AiClientConfig` 经 `ModelRouter.route()` 装配 `ChatClient` |
| 5 | Redis ChatMemory（max-messages=20） | ✓ VERIFIED | `ChatMemoryConfig` `MessageWindowChatMemory` + `properties.memory().maxMessages()`；`application.yml` `max-messages: 20` |
| 6 | 统一交付标准（源码/DB/compose/OpenAPI/单测/19100） | ✓ VERIFIED | 75 个 Java 源文件、`db/schema.sql`+`data.sql`、`docker-compose.override.yml`、`OpenApiConfig`、`pom.xml` testcontainers |
| 7 | 运行栈 PG+Milvus+Redis+Security+审计+Micrometer | ✓ VERIFIED | `application.yml` 数据源/Redis/Milvus；`SecurityConfig` JWT；`DbAuditLoggingAdvisor`；`actuator/prometheus` |
| **PLAN 04-01** | | | |
| 8 | KqaProperties 绑定 kqa.minio/rag/memory/security | ✓ VERIFIED | `@ConfigurationProperties(prefix="kqa")` record 含四分组 |
| 9 | 8 表 @Entity + JpaRepository 与 schema 对齐 | ✓ VERIFIED | 8×`@Entity`；`schema.sql` 8×`CREATE TABLE` |
| **PLAN 04-02** | | | |
| 10 | JWT Resource Server 保护 /api/**，login/health 匿名 | ✓ VERIFIED | `SecurityConfig.oauth2ResourceServer` + `permitAll` login/health |
| 11 | MinIO Client 连 kqa-documents | ✓ VERIFIED | `MinioConfig.minioClient` |
| 12 | PromptTemplateProvider 三级回退可读 qa-system | ✓ VERIFIED | Nacos→DB PUBLISHED→classpath `prompts/*.st` |
| **PLAN 04-03** | | | |
| 13 | RagPipelineFactory top-k=5、threshold=0.35、allowEmptyContext=false | ✓ VERIFIED | `RagPipelineFactory` 绑定 `KqaProperties.Rag` + `ContextualQueryAugmenter` |
| 14 | ETL 异步状态机 UPLOADED→PARSING→INDEXED\|FAILED | ✓ VERIFIED | `IngestStatusTracker` + `DocumentEtlPipeline.@Async` |
| 15 | CitationPostProcessor 从 Document metadata 组装 CitationVO | ✓ VERIFIED | `toCitation` 读取 documentId/title/chunkId/score |
| 16 | Advisor 链：Memory→RAG→Audit，ChatModel 经 ModelRouter | ✓ VERIFIED | `AiClientConfig.defaultAdvisors` |
| 17 | 演示文档 INDEXED 无 chunk 时 Seeder 触发 reindex | ✓ VERIFIED | `DemoKnowledgeSeeder` ApplicationRunner |
| **PLAN 04-04** | | | |
| 18 | POST /api/auth/login 签发 JWT，GET /api/auth/me | ✓ VERIFIED | `AuthController` + `AuthService` |
| 19 | POST /api/qa/ask 返回 Result\<QaAnswerVO\> 含 citations | ✓ VERIFIED | `QaController.ask` → `QaService.ask` |
| 20 | GET /api/qa/stream SSE message→meta→done，error 复用 Result | ✓ VERIFIED | `QaService.stream` concatWith meta + done；`buildErrorEvent` |
| 21 | 会话 DELETE 清 Redis ChatMemory | ✓ VERIFIED | `ConversationService.delete` → `chatMemoryRepository.deleteByConversationId` |
| 22 | POST /api/qa/feedback 持久化 qa_feedback | ✓ VERIFIED | `FeedbackService.save` → `QaFeedbackRepository` |
| **PLAN 04-05** | | | |
| 23 | ADMIN 知识/Prompt/用户/审计/看板五组 API | ✓ VERIFIED | `admin/controller/*` 五控制器 + 对应 Service |
| 24 | Prompt publish 推 Nacos Data ID | ✓ VERIFIED | `PromptPublishService.publishConfigToNacos` `spring.ai.alibaba.configurable.prompt` |
| 25 | KnowledgeOpsTools 仅 ADMIN 经 ToolContext | ✓ VERIFIED | `isAdmin(toolContext)` 校验 role |
| **PLAN 04-06** | | | |
| 26 | 无 API Key 时 mvn clean install 全绿 | ✓ VERIFIED | `mvn -f projects/knowledge-qa-platform/pom.xml clean install -DskipTests` exit 0；`mvn test` 12 tests 0 failures |
| 27 | version-audit 与 spring-ai-2-readiness 全绿 | ✓ VERIFIED | 两脚本 exit 0；无废弃 API grep |
| 28 | 04-UAT.md 覆盖 api.http 全接口 | ✓ VERIFIED | health/login/me/qa/conversations/admin/prometheus/doc.html 均有 curl |
| 29 | 有 API Key 时 login/ask/stream 各 ≥1 IT 通过 | ? UNCERTAIN | `QaAskIT`、`QaStreamIT` 存在且 `@EnabledIfEnvironmentVariable`；本轮无 Key 被跳过 |

**Score:** 28/29 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `config/SecurityConfig.java` | JWT 鉴权链 | ✓ VERIFIED | oauth2ResourceServer + UserDetailsService |
| `config/ChatMemoryConfig.java` | Redis 会话记忆 | ✓ VERIFIED | MessageWindowChatMemory + MessageChatMemoryAdvisor |
| `config/AiClientConfig.java` | ChatClient Bean | ✓ VERIFIED | defaultAdvisors 三 Advisor |
| `rag/RagPipelineFactory.java` | Modular RAG | ✓ VERIFIED | RetrievalAugmentationAdvisor |
| `rag/DocumentEtlPipeline.java` | 异步 ETL | ✓ VERIFIED | @Async ingestAsync + vectorStore.add |
| `rag/CitationPostProcessor.java` | 引用溯源 | ✓ VERIFIED | metadata→CitationVO |
| `controller/QaController.java` | 同步+流式问答 | ✓ VERIFIED | /ask + /stream |
| `service/QaService.java` | 问答核心 | ✓ VERIFIED | ChatClient + CitationPostProcessor |
| `admin/controller/DocumentAdminController.java` | 知识管理 API | ✓ VERIFIED | /api/admin/documents |
| `prompt/PromptPublishService.java` | Nacos 推送 | ✓ VERIFIED | publishConfig |
| `admin/service/DashboardStatsService.java` | 运营看板 | ✓ VERIFIED | DashboardStatsVO 聚合 |
| `db/schema.sql` + `data.sql` | DDL + 演示数据 | ✓ VERIFIED | 8 表 + 演示账号/文档 |
| `docker-compose.override.yml` | kqa profile | ✓ VERIFIED | 存在 |
| `04-UAT.md` | curl 验收清单 | ✓ VERIFIED | 全接口覆盖 |
| `src/test/**` | 单测 + IT | ✓ VERIFIED | 12 单测/冒烟通过；3 IT 条件门控 |

### Key Link Verification

| From | To | Via | Status | Details |
|------|-----|-----|--------|---------|
| SecurityConfig | SysUserRepository | UserDetailsService | ✓ WIRED | `userDetailsService` lambda |
| ChatMemoryConfig | kqa.memory.* | KqaProperties | ✓ WIRED | `maxMessages(properties.memory().maxMessages())` |
| DocumentEtlPipeline | VectorStore | vectorStore.add | ✓ WIRED | metadata documentId/title/seqNo |
| AiClientConfig | ModelRouter | route() | ✓ WIRED | `fallbackModelRouter.route()` |
| DocumentAdminService | DocumentEtlPipeline | ingestAsync/reindex | ✓ WIRED | upload 后异步索引 |
| PromptPublishService | Nacos | publishConfig | ✓ WIRED | DATA_ID `spring.ai.alibaba.configurable.prompt` |
| QaService | ChatClient | CONVERSATION_ID | ✓ WIRED | `ChatMemory.CONVERSATION_ID` param |
| QaController stream | QaService | Flux SSE | ✓ WIRED | `event("meta")` in QaService |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|-------------------|--------|
| QaService.ask | citations | CitationPostProcessor.retrieve → DocumentRetriever | Milvus 向量检索（运行时） | ⚠️ 需 infra |
| DocumentEtlPipeline | chunks | MinIO/Tika 或内联样本 | 真实文本分块写入 PG+Milvus | ✓ FLOWING（代码路径） |
| DashboardStatsService | stats | QaMessageRepository + MeterRegistry | DB 聚合 + gen_ai 指标 | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Maven 编译 | `mvn -f projects/knowledge-qa-platform/pom.xml compile` | exit 0 | ✓ PASS |
| Maven 测试（无 Key） | `mvn -f projects/knowledge-qa-platform/pom.xml test` | 12 tests, 0 failures | ✓ PASS |
| Maven 打包 | `mvn -f projects/knowledge-qa-platform/pom.xml clean install -DskipTests` | exit 0 | ✓ PASS |
| 废弃 API 扫描 | `grep PromptChatMemoryAdvisor\|CallAroundAdvisor\|FunctionCallback` | 0 matches | ✓ PASS |
| TODO/债务标记 | `grep TODO\|FIXME\|TBD\|XXX` in src | 0 matches | ✓ PASS |
| 硬编码云 API Key | `grep sk-…\|AI_DASHSCOPE_API_KEY=` | 0 matches；`application.yml` 使用 `${AI_DASHSCOPE_API_KEY}` | ✓ PASS |
| version-audit | `bash scripts/version-audit.sh` | exit 0 | ✓ PASS |
| spring-ai-2-readiness | `bash scripts/spring-ai-2-readiness.sh projects/knowledge-qa-platform` | exit 0 | ✓ PASS |

### Probe Execution

Step 7c: SKIPPED — 本阶段无 `scripts/*/tests/probe-*.sh` 声明；验收脚本为 `scripts/uat-knowledge-qa.sh`（需运行中应用，未在本轮执行）。

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| REQ-phase-4-knowledge-qa | 04-01~06 | knowledge-qa-platform 19100：MinIO+ETL、RAG+Citation、多模型、Redis Memory、Prompt、Security+审计+Micrometer、SSE；PG+Milvus+Redis | ✓ SATISFIED（代码） | 全栈实现 + 测试/UAT 文档；E2E 待人工 |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `projects/knowledge-qa-platform/README.md` | 5-6 | 「Java 占位」「占位类不参与 Bean 装配」 | ⚠️ Warning | 文档与实现漂移；代码已全量实现且 Bean 已装配 |
| `KqaProperties.java` / `application.yml` | 51/120 | dev JWT secret 默认值 | ℹ️ Info | 本地开发回退；生产应设 `KQA_JWT_SECRET` 环境变量 |

### Human Verification Required

### 1. 同步问答 Citation 闭环

**Test:** 按 `04-UAT.md` §2.1，infra 就绪后以 zhangsan 提问差旅住宿费标准
**Expected:** `data.citations` 非空，答案引用演示制度文档
**Why human:** RAG 召回依赖 Milvus 向量与 DashScope Embedding，静态分析无法证明运行时召回质量

### 2. SSE 流式事件序列

**Test:** 按 `04-UAT.md` §2.2 调用 `/api/qa/stream`
**Expected:** message 增量 → meta（含 citations + usage）→ done
**Why human:** 需观察真实 `text/event-stream` 响应顺序与 payload

### 3. 管理员文档上传与索引

**Test:** 按 `04-UAT.md` §3 上传文档并 reindex
**Expected:** 状态机 INDEXED，后续问答可召回
**Why human:** MinIO + 异步 ETL 需完整中间件栈

### 4. API Key 门控集成测试

**Test:** `source scripts/setup-env.sh && mvn -f projects/knowledge-qa-platform/pom.xml test -Dtest=QaAskIT,QaStreamIT`
**Expected:** 两 IT 各 PASS
**Why human:** 验证环境未配置 `AI_DASHSCOPE_API_KEY`，IT 被 JUnit 条件跳过

### Gaps Summary

自动化验证未发现实现缺失或 stub 阻断项：核心问答/RAG/ETL/后台/安全链路均已落地且 `mvn compile`/`mvn test`/`clean install` 通过，无废弃 API、无 TODO、无硬编码云密钥。

**未决项均为运行时验证：** 带 Citation 的 RAG 问答、SSE 协议、MinIO 上传索引、以及 API Key 门控 IT 需人工在完整 infra 环境下执行 `04-UAT.md` 或 `scripts/uat-knowledge-qa.sh` 收口。

---

_Verified: 2026-07-05T15:42:00Z_
_Verifier: Claude (gsd-verifier)_
