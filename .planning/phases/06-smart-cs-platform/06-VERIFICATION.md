---
phase: 06-smart-cs-platform
verified: 2026-07-17T14:43:00Z
status: human_needed
score: 27/28 must-haves verified
overrides_applied: 0
human_verification:
  - test: "按 06-UAT.md 前置条件起 infra（core+vector+search+cloud+smartcs）并 spring-boot:run，执行 §0 health + §1 三角色 login"
    expected: "health UP；admin/agent1/customer1 均 code=0 且拿到 accessToken；角色 claim 正确"
    why_human: "需 Docker 中间件与进程内 JWT 真签发；本机 Docker 不可用，Auth IT 亦被 DockerAvailableCondition 跳过"
  - test: "设置 AI_DASHSCOPE_API_KEY 后执行 06-UAT.md §2 同步 ask + SSE stream（退货政策种子问）"
    expected: "ask 返回答案；stream 含 message/done（FAQ 路径可出现 cacheHit）；cs_message 持久化"
    why_human: "需 DashScope + Milvus/ES/Redis Stack 真链路，静态分析无法证明秒答与混合检索召回"
  - test: "执行 06-UAT.md 工单流转 + §handoff start/approve（坐席 JWT）"
    expected: "非法 transition 400；approve 后工单 HUMAN_HANDLING，HITL resume 成功"
    why_human: "Graph interrupt + HumanInTheLoopHook 依赖真模型工具调用与进程内 pending 表"
  - test: "ADMIN 调 GET /api/admin/dashboard/stats 与 model/prompt publish；可选起 monitor profile 看 Prometheus/Grafana"
    expected: "stats 含会话/工单/cacheHitRate/成本字段；Nacos 出现 scs.model.profiles 与 prompt Data ID；prometheus 可 scrape"
    why_human: "Nacos 热更新与 Grafana 面板需运行时观测"
  - test: "bash projects/smart-cs-platform/scripts/uat-smart-cs.sh（有 Key 全量 / 无 Key 仅 health+login+RBAC）"
    expected: "脚本 exit 0；与 06-UAT.md 预期一致"
    why_human: "端到端 smoke 需已启动的 19300 应用与中间件"
---

# Phase 6: 智能客服平台 Verification Report

**Phase Goal:** 客服场景可通过 smart-cs-platform 完成 FAQ 秒答、多智能体协作、工单流转与人工接管，并具备运营看板  
**Verified:** 2026-07-17T14:43:00Z  
**Status:** human_needed  
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| **ROADMAP** | | | |
| 1 | FAQ/知识库问答可用（Milvus + Redis 语义缓存 + ES 全文混合检索） | ✓ VERIFIED | `FaqAnswerService`：`lookup`→`hybridSearch`→ChatClient→`put`；`HybridSearchService` RRF_K=60 + `accumulateRrf`；`SemanticCacheService`→`redisStackVectorStore`；`FaqEtlPipeline` 双写 Milvus/ES |
| 2 | RoutingAgent + Supervisor + Handoffs；工单域 + Graph interrupt（HITL） | ✓ VERIFIED | `CsAgentConfig`：`LlmRoutingAgent` 四分支；`businessSupervisor`+`AgentTool.create`；`HumanInTheLoopHook.approvalOn`；`TicketService.ALLOWED_TRANSITIONS`；`HumanHandoffController` start/approve+`resume` |
| 3 | 运营监控/成本；模型/Prompt CRUD + Nacos 热更新 | ✓ VERIFIED | `DashboardStatsService`+`DashboardAdminController`；`ModelAdminController` CRUD+publish；`ModelProfileNacosPublisher` Data ID `scs.model.profiles`；`PromptPublishService` Data ID `spring.ai.alibaba.configurable.prompt`；actuator prometheus + compose monitor |
| 4 | 统一交付标准；端口 19300；栈 PG+Milvus+Redis+ES+Nacos | ✓ VERIFIED | `application.yml` port 19300、`scs_platform`、milvus/ES/nacos；`docker-compose.override.yml` smartcs+monitor；`mvn -f … compile` / `clean install -DskipTests` BUILD SUCCESS |
| **PLAN 06-01** | | | |
| 5 | 工程可独立 compile，包根 `com.flywhl.saa.smartcs`，不挂父 modules | ✓ VERIFIED | `pom.xml` parent 仓库父 POM；根 `pom.xml` modules 仅 common/starter；主类包路径正确 |
| 6 | `db/schema.sql` 11 业务表与 Entity 对齐 | ✓ VERIFIED | 11×`CREATE TABLE`；11 Entity；角色/工单状态枚举齐全；`data.sql` 4 用户 + 12 FAQ + 2 工单 |
| 7 | compose smartcs：`scs-db-init` + `scs-redis-stack:6380` | ✓ VERIFIED | override 含 `psql -f schema/data`、`6380:6379` |
| 8 | `ScsProperties` 绑定 `scs.rag/cache/memory/security/ticket` | ✓ VERIFIED | `@ConfigurationProperties(prefix="scs")`；yml `scs.*` 键完整 |
| **PLAN 06-02** | | | |
| 9 | JWT 登录链就绪（Security + Auth） | ✓ VERIFIED | `SecurityConfig.oauth2ResourceServer`；`permitAll` `/api/auth/login`；`AuthService`/`AuthController` |
| 10 | 双 Redis 隔离：6379 ChatMemory vs 6380 语义缓存 | ✓ VERIFIED | `spring.data.redis` 6379 + `RedisChatMemoryRepository`；`RedisStackCacheConfig`/`scs.cache.redis-uri` 6380 |
| 11 | Milvus `scs_faq` + ES `scs-faq` VectorStore Bean | ✓ VERIFIED | `MilvusVectorStoreConfig` / `ElasticsearchVectorStoreConfig` `@Bean` + `@Qualifier` |
| 12 | Actuator 暴露 health + prometheus | ✓ VERIFIED | `management.endpoints…include=health,info,prometheus,metrics` |
| **PLAN 06-03** | | | |
| 13 | 语义缓存命中返回 `cacheHit=true` | ✓ VERIFIED | `FaqAnswerService` 命中分支构造 `ChatAnswerVO(..., true, …)`；单测 `SemanticCacheServiceTest` 3/3 |
| 14 | 未命中走 RRF 混合检索 + 生成 + 回写缓存 | ✓ VERIFIED | `hybridSearch`→Prompt→`chatClient`→`semanticCacheService.put`；空上下文短路不调模型 |
| 15 | FAQ ETL 双写并记 `faq_chunk` 溯源 | ✓ VERIFIED | `FaqEtlPipeline.indexAllSeedFaqs` ApplicationRunner；milvus/es add + chunk 元数据 |
| **PLAN 06-04** | | | |
| 16 | LlmRoutingAgent 四分支 FAQ/business/ticket/human | ✓ VERIFIED | `csIntentRouter.subAgents(List.of(faqAgent, businessSupervisor, ticketAgent, humanEscalationAgent))` |
| 17 | business-supervisor 用 `AgentTool.create` 调度子 Agent | ✓ VERIFIED | `orderAgent`/`afterSalesAgent`/`techSupportAgent` |
| 18 | HITL：`approvalOn("requestHumanHandoff")`，无 `interruptBefore` | ✓ VERIFIED | `HumanInTheLoopHook`；全仓无禁用 API 命中 |
| 19 | `CsOrchestratorService` 以 conversationId 作 threadId | ✓ VERIFIED | `RunnableConfig.builder().threadId(threadId)` |
| **PLAN 06-05** | | | |
| 20 | GET `/api/chat/stream` SSE + 消息持久化 | ✓ VERIFIED | `ChatController.stream`→`ChatService.stream`；`saveUserMessage`/`saveAssistantMessage` |
| 21 | 工单状态机非法转移返回 400 | ✓ VERIFIED | `TicketService.transition`→`BizException(BAD_REQUEST)`；`TicketServiceTest` 4/4 |
| 22 | 坐席 approve 恢复 HITL 且工单 `HUMAN_HANDLING` | ✓ VERIFIED | `HumanHandoffController.approve`→`addHumanFeedback`+`resume`→`transitionToHumanHandling` |
| 23 | conversationId 全链路 UUID 与 threadId 一致 | ✓ VERIFIED | Chat/Handoff/Orchestrator 均 UUID/`conversationId` 绑定 |
| **PLAN 06-06** | | | |
| 24 | ADMIN CRUD `model_profile` 并按 scene 路由 | ✓ VERIFIED | `ModelAdminController`；`ConfigurableModelRouter.routeForScene` |
| 25 | Prompt / model_profile 发布推 Nacos | ✓ VERIFIED | `PromptPublishService` + `ModelProfileNacosPublisher.publishConfig` |
| 26 | Dashboard stats + Prometheus/Grafana 文档 | ✓ VERIFIED | `DashboardStatsVO` 聚合；`monitor/prometheus.yml`；README §7 Grafana |
| **PLAN 06-07** | | | |
| 27 | 无 Key 编译/单测绿；UAT 清单与 smoke 脚本存在 | ✓ VERIFIED | unit 15 pass / 4 skip；`06-UAT.md`；`uat-smart-cs.sh` syntax OK；无 TODO/FIXME/硬编码密钥 |
| 28 | 有 API Key 时 login/chat/ticket/handoff 各 ≥1 IT 可运行 | ? UNCERTAIN | IT 类存在（`Auth`/`Chat`/`Ticket`/`ModelIntegrationTest`）；本机 Docker 不可用致 Testcontainers 跳过；未跑真模型 IT |

**Score:** 27/28 truths verified（1 UNCERTAIN → 人工）

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `projects/smart-cs-platform/pom.xml` | 独立工程 | ✓ VERIFIED | 含 common/starter/agent-framework/milvus/ES/nacos/actuator |
| `db/schema.sql` | DDL SSOT | ✓ VERIFIED | 11 表，`scs_platform` |
| `config/ScsProperties.java` | scs.* 绑定 | ✓ VERIFIED | `@ConfigurationProperties(prefix="scs")` |
| `config/SecurityConfig.java` | JWT RS | ✓ VERIFIED | `oauth2ResourceServer` |
| `config/RedisStackCacheConfig.java` | 6380 VS | ✓ VERIFIED | `redisStackVectorStore` |
| `config/AiClientConfig.java` | ChatClient.Builder | ✓ VERIFIED | AuditLoggingAdvisor + scene 路由 |
| `rag/HybridSearchService.java` | RRF | ✓ VERIFIED | `RRF_K` + `accumulateRrf` |
| `service/SemanticCacheService.java` | 语义缓存 | ✓ VERIFIED | threshold + expiresAt |
| `service/FaqAnswerService.java` | 缓存→混合→RAG | ✓ VERIFIED | lookup 优先 |
| `agent/CsAgentConfig.java` | Agent 图 | ✓ VERIFIED | LlmRoutingAgent + Supervisor + HITL |
| `agent/FlowStateExtractor.java` | 文本提取 | ✓ VERIFIED | `extractText` |
| `tool/HandoffTools.java` | `@Tool` handoff | ✓ VERIFIED | `requestHumanHandoff` |
| `controller/ChatController.java` | SSE 网关 | ✓ VERIFIED | ask + stream |
| `service/TicketService.java` | 状态机 | ✓ VERIFIED | `ALLOWED_TRANSITIONS` |
| `controller/HumanHandoffController.java` | HITL API | ✓ VERIFIED | start/approve |
| `admin/service/DashboardStatsService.java` | 看板 | ✓ VERIFIED | DB + MeterRegistry |
| `admin/controller/ModelAdminController.java` | model CRUD | ✓ VERIFIED | gsd `contains: model_profile` 字面未命中属误报；实体/API 齐全 |
| `prompt/PromptPublishService.java` | Nacos Prompt | ✓ VERIFIED | publishConfig |
| `support/ScsPostgresRedisITBase.java` | IT 基座 | ✓ VERIFIED | PG+Redis Testcontainers + Docker 门控 |
| `scripts/uat-smart-cs.sh` | smoke | ✓ VERIFIED | health/login/RBAC/可选模型路径 |
| `06-UAT.md` | 验收清单 | ✓ VERIFIED | 254 行，覆盖 api.http |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `docker-compose.override.yml` | `db/schema.sql` | `scs-db-init` psql -f | ✓ WIRED | entrypoint 执行 schema+data |
| `ScsProperties` | `application.yml` | `prefix=scs` | ✓ WIRED | yml `scs.rag/cache/memory/security/ticket` |
| `SecurityConfig` | `/api/auth/login` | permitAll | ✓ WIRED | 路径放行 |
| `RedisStackCacheConfig` | scs.cache 6380 | 独立连接 | ✓ WIRED | JedisPooled + uri |
| `FaqAnswerService` | `SemanticCacheService` | lookup 优先 | ✓ WIRED | L60-64 |
| `HybridSearchService` | milvus+ES | `accumulateRrf` | ✓ WIRED | Qualifier 注入 |
| `CsOrchestratorService` | `csIntentRouter` | threadId | ✓ WIRED | invoke→LlmRoutingAgent |
| `businessSupervisor` | 子 Agent | `AgentTool.create` | ✓ WIRED | 三工具 |
| `ChatService` | `CsOrchestratorService` | ask/stream | ✓ WIRED | FAQ 空答回退 `FaqAnswerService` |
| `HumanHandoffController` | `humanEscalationAgent` | invoke+resume | ✓ WIRED | Qualifier Bean |
| `ModelProfileNacosPublisher` | Nacos ConfigService | `scs.model.profiles` | ✓ WIRED | publishConfig |
| `DashboardStatsService` | cs_message + MeterRegistry | cache_hit/token | ✓ WIRED | 仓储计数 + gen_ai meter |
| `AuthIntegrationTest` | `/api/auth/login` | MockMvc | ✓ WIRED | 文件存在；运行需 Docker |
| `uat-smart-cs.sh` | `:19300` | curl | ✓ WIRED | 脚本含 health/login/dashboard |

> 注：`gsd-sdk query verify.key-links` 对短名 from 报 “Source file not found”，以上为人工 grep 接线结论。

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|--------------------|--------|
| `FaqAnswerService` | answer / cacheHit | SemanticCache → HybridSearch → ChatClient | 是（运行时依赖向量库/模型） | ✓ FLOWING |
| `ChatService` | OrchestratorResult / ChatAnswerVO | `CsOrchestratorService` + 可选 FAQ 回退 | 是；消息写入 `cs_message` | ✓ FLOWING |
| `DashboardStatsService` | DashboardStatsVO | JPA count + MeterRegistry | 是（库空时为零值，非硬编码空壳） | ✓ FLOWING |
| `HumanHandoffController` | pendingByThread | InterruptionMetadata | 是（进程内 Map；文档已注明生产需持久化） | ✓ FLOWING |
| `RagPipelineFactory` | `RetrievalAugmentationAdvisor` | Bean 已注册 | 无消费者注入（FAQ 走 Hybrid 链路） | ⚠️ ORPHANED |
| `ChatMemoryConfig` | `MessageChatMemoryAdvisor` | Bean 已注册 | 未挂入 `ChatClient.Builder`；会话靠 JPA | ⚠️ ORPHANED |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| 独立编译 | `mvn -f projects/smart-cs-platform/pom.xml -DskipTests compile` | BUILD SUCCESS | ✓ PASS |
| clean install | `mvn -f … clean install -DskipTests` | BUILD SUCCESS | ✓ PASS |
| 单测（无 Docker） | `mvn -f … -Dtest='*Test,*Tests' test` | 15 run, 0 fail, 4 skip | ✓ PASS |
| 禁用 API 门禁 | rg `interruptBefore\|SupervisorAgent\|PromptChatMemoryAdvisor\|FunctionCallback` | 0 命中 | ✓ PASS |
| UAT 脚本语法 | `bash -n scripts/uat-smart-cs.sh` | OK | ✓ PASS |
| version-audit（仓库） | `bash scripts/version-audit.sh` | BOM OK；子工程不在 reactor 故未解析 SAA 坐标（预期） | ✓ PASS* |
| spring-ai-2-readiness | `bash scripts/spring-ai-2-readiness.sh projects/smart-cs-platform` | Jackson2 引用 8（1.x 锁定期预期）；MCP 0 | ✓ PASS* |
| 真机 UAT / Testcontainers IT | — | Docker 不可用 | ? SKIP |

### Probe Execution

| Probe | Command | Result | Status |
|-------|---------|--------|--------|
| （无 phase 声明 probe-*.sh） | — | N/A | SKIP |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| **REQ-phase-6-smart-cs** | 06-01…06-07 全部 | smart-cs-platform：FAQ 秒答、多智能体、工单、人工接管、运营看板；RoutingAgent+Supervisor+Handoffs、Milvus+Redis 缓存+ES、HITL、Micrometer/Prometheus/Grafana、Nacos；DB PG+Milvus+Redis+ES | ✓ SATISFIED（代码） | 四项 ROADMAP SC 均有实现证据；E2E 待人工 |
| （孤儿检查） | REQUIREMENTS.md Phase 6 | 仅此一条映射到 Phase 6 | ✓ 无 ORPHANED | Traceability 表仅 `REQ-phase-6-smart-cs` |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `rag/RagPipelineFactory.java` | Bean | `RetrievalAugmentationAdvisor` 无注入点 | ⚠️ Warning | FAQ 主路径用 Hybrid+ChatClient，目标仍达成；Advisor 成死 Bean |
| `config/ChatMemoryConfig.java` | Bean | `MessageChatMemoryAdvisor` 未挂 ChatClient | ⚠️ Warning | 会话靠 `cs_message` JPA；双 Redis 配置仍在 |
| `agent/CsAgentConfig.java` | faqParallelContext | ParallelAgent 未接入顶层路由 | ℹ️ Info | 注释写明供后续消费；非 must-have |
| `HumanHandoffController.java` | pending Map | 进程内 ConcurrentHashMap | ℹ️ Info | 计划允许演示用内存；文档已声明生产应持久化 |
| — | — | TBD/FIXME/XXX / 硬编码 sk- | （无） | — |

### Human Verification Required

#### 1. 健康检查与三角色登录

**Test:** 起 compose profiles + 应用后，执行 06-UAT.md §0–§1  
**Expected:** health UP；admin/agent1/customer1 登录拿 token  
**Why human:** Docker 本轮不可用；需真 JWT 签发

#### 2. FAQ ask + SSE（需 API Key）

**Test:** 06-UAT.md §2，问「收到商品后多久可以申请退货？」  
**Expected:** 同步/流式均有答案；可选二次命中 cacheHit  
**Why human:** 依赖 Embedding/Milvus/ES/Redis Stack/DashScope

#### 3. 工单 + HITL handoff

**Test:** 创建/流转工单；handoff start→坐席 approve  
**Expected:** 非法转移 400；approve 后 HUMAN_HANDLING  
**Why human:** Graph interrupt 与真工具调用

#### 4. 运营看板 + Nacos + 监控

**Test:** dashboard/stats、model/prompt publish；可选 Grafana  
**Expected:** 统计字段非空结构正确；Nacos Data ID 更新；prometheus scrape  
**Why human:** 运行时配置中心与监控栈

#### 5. uat-smart-cs.sh smoke

**Test:** `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`  
**Expected:** exit 0  
**Why human:** 需已运行的应用与中间件

### Gaps Summary

无代码级 BLOCKER。相位目标在代码库中可达成：FAQ 混合检索+语义缓存、多智能体路由/Supervisor/HITL、工单状态机、运营看板与 Nacos 发布、19300 交付栈均已落地；单测与编译通过；Testcontainers IT 在 Docker 不可用时跳过（符合约定）。

剩余为 **human_needed**：真机 UAT / 有 Key IT / Nacos·Grafana 观测。另有两处非阻断 WARNING（`RetrievalAugmentationAdvisor` 与 `MessageChatMemoryAdvisor` Bean 未接入主问答链）。

---

_Verified: 2026-07-17T14:43:00Z_  
_Verifier: Claude (gsd-verifier)_
