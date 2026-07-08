# Phase 6: 智能客服平台 - Pattern Map

**Mapped:** 2026-07-06
**Files analyzed:** 52
**Analogs found:** 48 / 52

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `projects/smart-cs-platform/pom.xml` | config | — | `projects/knowledge-qa-platform/pom.xml` | exact |
| `projects/smart-cs-platform/README.md` | docs | — | `projects/knowledge-qa-platform/README.md` | exact |
| `projects/smart-cs-platform/docker-compose.override.yml` | middleware | batch | `projects/knowledge-qa-platform/docker-compose.override.yml` + `examples/25-redis-vector-demo/docker-compose.override.yml` | exact |
| `projects/smart-cs-platform/db/schema.sql` | migration | CRUD | `projects/knowledge-qa-platform/db/schema.sql` | role-match |
| `projects/smart-cs-platform/db/data.sql` | migration | batch | `projects/knowledge-qa-platform/db/data.sql` | exact |
| `projects/smart-cs-platform/http/api.http` | route | request-response | `projects/knowledge-qa-platform/http/api.http` | exact |
| `projects/smart-cs-platform/scripts/uat-smart-cs.sh` | utility | batch | `projects/knowledge-qa-platform` UAT 脚本模式 | role-match |
| `SmartCsApplication.java` | config | — | `projects/knowledge-qa-platform/.../KnowledgeQaApplication.java` | exact |
| `config/ScsProperties.java` | config | — | `projects/knowledge-qa-platform/.../config/KqaProperties.java` | exact |
| `config/SecurityConfig.java` | middleware | request-response | `projects/knowledge-qa-platform/.../config/SecurityConfig.java` | exact |
| `config/OpenApiConfig.java` | config | — | `projects/office-agent-assistant/.../config/OpenApiConfig.java` | exact |
| `config/AiClientConfig.java` | config | — | `projects/knowledge-qa-platform/.../config/AiClientConfig.java` | exact |
| `config/ChatMemoryConfig.java` | config | — | `projects/knowledge-qa-platform/.../config/ChatMemoryConfig.java` | exact |
| `config/RedisChatMemoryRepository.java` | middleware | CRUD | `projects/office-agent-assistant/.../config/RedisChatMemoryRepository.java` | exact |
| `config/MilvusVectorStoreConfig.java` | config | — | `projects/knowledge-qa-platform/.../config/VectorStoreConfig.java` | role-match |
| `config/ElasticsearchVectorStoreConfig.java` | config | — | `examples/26-es-hybrid-demo/.../application.yml` + kqa VectorStoreConfig | role-match |
| `config/RedisStackCacheConfig.java` | config | — | `examples/25-redis-vector-demo/docker-compose.override.yml` + `application.yml` | role-match |
| `config/ObservabilityConfig.java` | config | — | `examples/45-observability-demo/src/main/resources/application.yml` | exact |
| `controller/AuthController.java` | controller | request-response | `projects/knowledge-qa-platform/.../controller/AuthController.java` | exact |
| `controller/ChatController.java` | controller | streaming | `projects/knowledge-qa-platform/.../controller/QaController.java` | exact |
| `controller/TicketController.java` | controller | CRUD | `projects/office-agent-assistant/.../controller/ApprovalController.java` | role-match |
| `controller/HumanHandoffController.java` | controller | request-response | `examples/37-agent-hitl-demo/.../HitlController.java` | exact |
| `service/AuthService.java` | service | request-response | `projects/knowledge-qa-platform/.../service/AuthService.java` | exact |
| `service/ChatService.java` | service | streaming | `projects/knowledge-qa-platform/.../service/QaService.java` | role-match |
| `service/CsOrchestratorService.java` | service | request-response | `projects/office-agent-assistant/.../service/ApprovalService.java` | role-match |
| `service/FaqAnswerService.java` | service | transform | `projects/knowledge-qa-platform/.../service/QaService.java` | role-match |
| `service/SemanticCacheService.java` | service | transform | `examples/25-redis-vector-demo/.../RedisVectorController.java` | exact |
| `service/TicketService.java` | service | CRUD | `projects/office-agent-assistant/.../service/ApprovalService.java` | partial |
| `service/ModelAdminService.java` | service | CRUD | `projects/knowledge-qa-platform/.../admin/service/UserAdminService.java` | role-match |
| `agent/CsAgentConfig.java` | config | request-response | `examples/41-multi-agent-demo/.../MultiAgentConfig.java` + `examples/42-supervisor-demo/.../OfficeSupervisorConfig.java` | exact |
| `agent/FlowStateExtractor.java` | utility | transform | `examples/41-multi-agent-demo/.../FlowStateExtractor.java` | exact |
| `tool/OrderTool.java` | service | request-response | `projects/office-agent-assistant/.../tool/SqlQueryTool.java` | role-match |
| `tool/TicketTools.java` | service | CRUD | `projects/office-agent-assistant/.../tool/CalendarTool.java` | role-match |
| `tool/HandoffTools.java` | service | event-driven | `examples/37-agent-hitl-demo/.../HighRiskTools.java` | role-match |
| `tool/ToolSecuritySupport.java` | middleware | request-response | `projects/office-agent-assistant/.../tool/ToolSecuritySupport.java` | exact |
| `rag/FaqEtlPipeline.java` | service | file-I/O | `projects/knowledge-qa-platform/.../rag/DocumentEtlPipeline.java` | role-match |
| `rag/RagPipelineFactory.java` | config | transform | `projects/knowledge-qa-platform/.../rag/RagPipelineFactory.java` | exact |
| `rag/HybridSearchService.java` | service | transform | `examples/26-es-hybrid-demo/.../HybridSearchService.java` | exact |
| `rag/SemanticCacheAdvisor.java` | middleware | transform | `examples/25-redis-vector-demo` lookup 逻辑 | role-match |
| `prompt/PromptTemplateProvider.java` | service | — | `projects/knowledge-qa-platform/.../prompt/PromptTemplateProvider.java` | exact |
| `prompt/PromptPublishService.java` | service | pub-sub | `projects/knowledge-qa-platform/.../prompt/PromptPublishService.java` | exact |
| `admin/DashboardStatsService.java` | service | batch | `projects/knowledge-qa-platform/.../admin/service/DashboardStatsService.java` | role-match |
| `admin/controller/*AdminController.java` | controller | CRUD | `projects/knowledge-qa-platform/.../admin/controller/DashboardAdminController.java` | exact |
| `model/entity/*` | model | CRUD | `projects/knowledge-qa-platform/model/entity/*` | exact |
| `model/dto/*` / `model/vo/*` | model | request-response | `projects/knowledge-qa-platform/model/dto/*` | exact |
| `repository/*` | service | CRUD | `projects/knowledge-qa-platform/repository/*` | exact |
| `mapper/*` | utility | transform | `projects/knowledge-qa-platform/mapper/*` | exact |
| `support/ScsPostgresRedisITBase.java` | test | — | `projects/knowledge-qa-platform/.../support/KqaPostgresRedisITBase.java` | exact |
| `support/DockerAvailableCondition.java` | test | — | `projects/knowledge-qa-platform/.../support/DockerAvailableCondition.java` | exact |
| `test/AuthIntegrationTest.java` | test | request-response | `projects/office-agent-assistant/.../AuthIntegrationTest.java` | exact |
| `test/ChatIntegrationTest.java` | test | request-response | `projects/office-agent-assistant/.../AuthIntegrationTest.java`（@MockBean Agent） | role-match |
| `config/ConfigurableModelRouter.java` | middleware | request-response | `saa-learning-starter` FallbackModelRouter | partial |
| `config/ModelProfileNacosPublisher.java` | service | pub-sub | `prompt/PromptPublishService.java`（Nacos 推送模式） | role-match |

## Pattern Assignments

### `projects/smart-cs-platform/pom.xml` (config)

**Analog:** `projects/knowledge-qa-platform/pom.xml`

**Parent + 公共底座** (lines 7-33):
```xml
<parent>
    <groupId>com.flywhl.saa</groupId>
    <artifactId>spring-ai-alibaba-learning</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
<dependencies>
    <dependency>
        <groupId>com.flywhl.saa</groupId>
        <artifactId>saa-learning-common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.flywhl.saa</groupId>
        <artifactId>saa-learning-starter</artifactId>
    </dependency>
```

**Phase 6 增量依赖**（在 kqa 基础上追加，零版本号）:
- `spring-ai-alibaba-agent-framework` — Agent 编排
- `spring-ai-starter-vector-store-elasticsearch` — ES 混合检索
- `org.testcontainers:elasticsearch`（test scope，可选）

**禁止：** 子模块硬编码 SAA/Spring AI 版本；挂父 POM `<modules>`。

---

### `docker-compose.override.yml` (middleware, batch)

**Analog:** `projects/knowledge-qa-platform/docker-compose.override.yml` + `examples/25-redis-vector-demo/docker-compose.override.yml`

**DB init 模式** (kqa lines 18-43):
```yaml
scs-db-init:
  image: pgvector/pgvector:pg16
  profiles: [ "smartcs" ]
  environment:
    PGHOST: postgres
    PGUSER: saa
    PGPASSWORD: saa123456
  volumes:
    - ./db:/scs-db:ro
  entrypoint:
    - bash
    - -ceu
    - |
      until pg_isready -h postgres -U saa -d saa_learning; do sleep 2; done
      psql -d saa_learning -tAc "SELECT 1 FROM pg_database WHERE datname='scs_platform'" | grep -q 1 \
        || createdb scs_platform
      psql -d scs_platform -v ON_ERROR_STOP=1 -f /scs-db/schema.sql
      psql -d scs_platform -v ON_ERROR_STOP=1 -f /scs-db/data.sql
```

**Redis Stack 语义缓存** (25-demo lines 8-18):
```yaml
scs-redis-stack:
  image: redis/redis-stack-server:latest
  container_name: saa-scs-redis-stack
  profiles: [ "smartcs" ]
  ports:
    - "6380:6379"
  healthcheck:
    test: [ "CMD", "redis-cli", "ping" ]
```

**启动命令：** `docker compose -f docker/docker-compose.yml -f projects/smart-cs-platform/docker-compose.override.yml --profile core --profile vector --profile search --profile cloud --profile smartcs up -d`

---

### `db/schema.sql` (migration, CRUD)

**Analog:** `projects/knowledge-qa-platform/db/schema.sql`

**表头约定** (kqa lines 1-9):
```sql
-- =====================================================================
-- 项目三 · 智能客服平台 —— PostgreSQL 业务库 DDL（SSOT）
-- 数据库：scs_platform（由 docker-compose.override.yml 的 scs-db-init 自动创建并执行本文件）
-- 约定：JPA ddl-auto=none，本文件是表结构唯一真源
-- =====================================================================
```

**用户表 RBAC 模式** (kqa lines 14-24，角色改为 CUSTOMER/AGENT/ADMIN):
```sql
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    role          VARCHAR(32)  NOT NULL DEFAULT 'CUSTOMER',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

**Phase 6 新增表：** `faq_article`、`faq_chunk`、`cs_conversation`、`cs_message`、`cs_ticket`、`cs_ticket_event`、`model_profile`、`cs_feedback`；复用 `prompt_template`、`audit_log` 结构。

---

### `config/ScsProperties.java` (config)

**Analog:** `projects/knowledge-qa-platform/.../config/KqaProperties.java`

**Record + 默认值模式** (lines 13-33):
```java
@ConfigurationProperties(prefix = "scs")
public record ScsProperties(
        Rag rag,
        Cache cache,
        Memory memory,
        Security security,
        Ticket ticket) {

    public ScsProperties {
        if (rag == null) {
            rag = new Rag(5, 0.35, 512, 64);
        }
        if (cache == null) {
            cache = new Cache(0.95, 300, "redis://localhost:6380");
        }
        if (memory == null) {
            memory = new Memory(20, Duration.ofDays(7));
        }
        if (security == null) {
            security = new Security(new Jwt("smart-cs-platform", Duration.ofHours(2), null));
        }
    }
}
```

**新增分组：** `Cache`（similarityThreshold、ttlSeconds、redisHost/port）、`Ticket`（escalation 阈值等）。

---

### `config/SecurityConfig.java` (middleware, request-response)

**Analog:** `projects/knowledge-qa-platform/.../config/SecurityConfig.java`

**FilterChain + JWT** (lines 52-64):
```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                    .requestMatchers(DOC_PATHS.toArray(String[]::new)).permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
}
```

**角色映射：** `CUSTOMER` / `AGENT` / `ADMIN`；`@PreAuthorize("hasRole('AGENT')")` 用于工单/HITL 接口。

---

### `controller/ChatController.java` (controller, streaming)

**Analog:** `projects/knowledge-qa-platform/.../controller/QaController.java`

**SSE 端点** (lines 27-48):
```java
@RestController
@RequestMapping("/api/chat")
@PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
public class ChatController {

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam String conversationId,
            @RequestParam String question) {
        return chatService.stream(conversationId, question);
    }
}
```

**路径差异：** `/api/chat/stream`（非 kqa 的 `/api/qa/stream`）；同步入口可选 `POST /api/chat/ask`。

---

### `service/ChatService.java` + `service/CsOrchestratorService.java` (service, streaming / request-response)

**Analog:** `projects/knowledge-qa-platform/.../service/QaService.java` + `projects/office-agent-assistant/.../service/ApprovalService.java`

**SSE 流式骨架** (QaService lines 100+):
```java
public Flux<ServerSentEvent<String>> stream(String conversationId, String question) {
    SysUser user = authService.requireCurrentUser();
    ensureConversation(user, conversationId, question);
    saveUserMessage(conversationId, question);
    // 委托 CsOrchestratorService 路由 → FAQ / Supervisor / Ticket / HITL
    return orchestratorService.stream(conversationId, question)
            .doOnComplete(() -> touchConversation(...));
}
```

**Agent 调用** (ApprovalService lines 47-55):
```java
RunnableConfig config = RunnableConfig.builder()
        .threadId(conversationId)  // 全链路 UUID，禁止自增
        .build();
Optional<OverAllState> state = csIntentRouter.invoke(query, config);
String answer = FlowStateExtractor.extractText(state);
```

**文本提取** (41-demo FlowStateExtractor lines 20-36):
```java
static String extractText(Optional<OverAllState> stateOptional) {
    if (stateOptional.isEmpty()) return "";
    List<Message> messages = stateOptional.get().value("messages", List.of());
    for (int i = messages.size() - 1; i >= 0; i--) {
        if (messages.get(i) instanceof AssistantMessage assistant) {
            String text = assistant.getText();
            if (text != null && !text.isBlank()) return text;
        }
    }
    return stateOptional.get().value("output", "");
}
```

---

### `agent/CsAgentConfig.java` (config, request-response)

**Analog:** `examples/41-multi-agent-demo/.../MultiAgentConfig.java` + `examples/42-supervisor-demo/.../OfficeSupervisorConfig.java` + `examples/37-agent-hitl-demo/.../HitlAgentConfig.java`

**LlmRoutingAgent 顶层路由** (41-demo lines 123-141):
```java
@Bean
LlmRoutingAgent csIntentRouter(ChatModel dashScopeChatModel,
        ReactAgent faqAgent,
        ReactAgent businessSupervisor,
        ReactAgent ticketAgent,
        ReactAgent humanEscalationAgent) {
    return LlmRoutingAgent.builder()
            .name("cs-intent-router")
            .description("客服意图路由：FAQ / 业务 / 工单 / 人工")
            .model(dashScopeChatModel)
            .systemPrompt("""
                    根据用户问题选择唯一子智能体：
                    - faq-agent：标准 FAQ、政策、流程类可检索问题
                    - business-supervisor：订单/物流/售后/技术复杂问题
                    - ticket-agent：明确要求建单、查单、催单
                    - human-escalation-agent：用户要求人工、投诉升级
                    """)
            .subAgents(List.of(faqAgent, businessSupervisor, ticketAgent, humanEscalationAgent))
            .hooks(ModelCallLimitHook.builder().runLimit(6).build())
            .build();
}
```

**Supervisor 模式** (42-demo lines 42-58):
```java
@Bean
ReactAgent businessSupervisor(ChatModel dashScopeChatModel,
        ReactAgent orderAgent,
        ReactAgent afterSalesAgent,
        ReactAgent techSupportAgent) {
    return ReactAgent.builder()
            .name("business-supervisor")
            .model(dashScopeChatModel)
            .tools(
                AgentTool.create(orderAgent),
                AgentTool.create(afterSalesAgent),
                AgentTool.create(techSupportAgent))
            .systemPrompt("根据需求调用 order/aftersales/tech 助手，汇总回复。")
            .hooks(ModelCallLimitHook.builder().runLimit(6).build())
            .build();
}
```

**FAQ 内 ParallelAgent** (41-demo lines 78-87):
```java
@Bean
ParallelAgent faqParallelContext(ReactAgent knowledgeBaseAgent, ReactAgent ticketHistoryAgent) {
    return ParallelAgent.builder()
            .name("faq-parallel-context")
            .subAgents(List.of(knowledgeBaseAgent, ticketHistoryAgent))
            .maxConcurrency(2)
            .mergeOutputKey("mergedContext")
            .hooks(ModelCallLimitHook.builder().runLimit(4).build())
            .build();
}
```

**HITL Hook** (37-demo lines 19-35):
```java
HumanInTheLoopHook hitl = HumanInTheLoopHook.builder()
        .approvalOn("requestHumanHandoff", "人工接管需坐席确认")
        .build();
// ReactAgent.builder()...hooks(hitl, ModelCallLimitHook...).saver(new MemorySaver())
```

**禁止：** `SupervisorAgent`、`AgentTool.from`、`interruptBefore`、`LlmRoutingAgent.routes(Map)`。

---

### `controller/HumanHandoffController.java` (controller, request-response)

**Analog:** `examples/37-agent-hitl-demo/.../HitlController.java`

**start + approve 模式** (lines 45-95):
```java
@PostMapping("/api/handoff/start")
public Result<HitlSessionResponse> start(@RequestParam String query) throws GraphRunnerException {
    String threadId = UUID.randomUUID().toString();
    RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
    Optional<NodeOutput> output = humanEscalationAgent.invokeAndGetOutput(query, config);
    if (output.isPresent() && output.get() instanceof InterruptionMetadata interruption) {
        pendingByThread.put(threadId, interruption);
        ticketService.transitionToPendingHuman(threadId, ...);
        return Result.ok(new HitlSessionResponse(threadId, "PENDING_HUMAN", ...));
    }
    return Result.ok(...);
}

@PostMapping("/api/handoff/approve")
public Result<HitlSessionResponse> approve(@RequestParam String threadId) throws GraphRunnerException {
    InterruptionMetadata pending = pendingByThread.remove(threadId);
    if (pending == null) {
        throw new BizException(CommonResultCode.NOT_FOUND, "无待审批会话或 threadId 无效：" + threadId);
    }
    RunnableConfig resumeConfig = RunnableConfig.builder()
            .threadId(threadId)
            .addHumanFeedback(buildApprovedFeedback(pending))
            .resume()
            .build();
    // ticketService.transitionToHumanHandling(threadId)
    ...
}
```

**差异：** 结合 `TicketService` 状态机；`threadId` 与 `cs_conversation.conversation_id` 绑定。

---

### `service/SemanticCacheService.java` (service, transform)

**Analog:** `examples/25-redis-vector-demo/.../RedisVectorController.java`

**lookup** (lines 102-133，阈值改为 0.95):
```java
public Optional<CacheHit> lookup(String query) {
    FilterExpressionBuilder b = new FilterExpressionBuilder();
    List<Document> results = redisStackVectorStore.similaritySearch(
            SearchRequest.builder()
                    .query(query)
                    .topK(3)
                    .similarityThreshold(properties.cache().similarityThreshold()) // 默认 0.95
                    .filterExpression(b.eq("type", "semantic-cache").build())
                    .build());
    Instant now = Instant.now();
    for (Document doc : results) {
        Object expiresAt = doc.getMetadata().get("expiresAt");
        if (expiresAt != null && now.isBefore(Instant.parse(expiresAt.toString()))) {
            return Optional.of(new CacheHit(doc.getMetadata().get("answer").toString(), doc.getScore()));
        }
    }
    return Optional.empty();
}
```

**put** (lines 80-96):
```java
public void put(String query, String answer, int ttlSeconds) {
    Instant now = Instant.now();
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("type", "semantic-cache");
    metadata.put("answer", answer);
    metadata.put("expiresAt", now.plusSeconds(ttlSeconds).toString());
    redisStackVectorStore.add(List.of(new Document(query, metadata)));
}
```

---

### `rag/HybridSearchService.java` (service, transform)

**Analog:** `examples/26-es-hybrid-demo/.../HybridSearchService.java`

**直接复制 RRF 核心** (lines 31-121):
```java
private static final int RRF_K = 60;

public List<Map<String, Object>> hybridSearch(String query, int topK) throws IOException {
    int candidateK = Math.max(topK * 2, topK);
    List<Document> vectorHits = vectorSearch(query, candidateK);
    List<Document> textHits = fullTextSearch(query, candidateK);
    Map<String, Double> rrfScores = new HashMap<>();
    Map<String, Document> docsById = new HashMap<>();
    accumulateRrf(vectorHits, rrfScores, docsById);
    accumulateRrf(textHits, rrfScores, docsById);
    return rrfScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
            .limit(topK)
            .map(...)
            .toList();
}
```

**Bean 命名：** `milvusVectorStore`（collection `scs_faq`）+ `elasticsearchVectorStore`（index `scs-faq`）；与语义缓存 `redisStackVectorStore` 隔离。

---

### `rag/RagPipelineFactory.java` (config, transform)

**Analog:** `projects/knowledge-qa-platform/.../rag/RagPipelineFactory.java`

**RetrievalAugmentationAdvisor 组装** (lines 30-68):
```java
@Bean
RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
        ChatClient.Builder chatClientBuilder,
        VectorStoreDocumentRetriever documentRetriever,
        ScsProperties properties,
        PromptTemplateProvider promptTemplateProvider) {
    var rewriteTransformer = RewriteQueryTransformer.builder()
            .chatClientBuilder(chatClientBuilder.build().mutate())
            .promptTemplate(new PromptTemplate(promptTemplateProvider.getQueryRewriteTemplate()))
            .build();
    return RetrievalAugmentationAdvisor.builder()
            .queryTransformers(rewriteTransformer)
            .documentRetriever(documentRetriever)
            .documentPostProcessors(scoreReranker)
            .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(false).build())
            .build();
}
```

---

### `rag/FaqEtlPipeline.java` (service, file-I/O)

**Analog:** `projects/knowledge-qa-platform/.../rag/DocumentEtlPipeline.java`

**模式：** FAQ 种子数据 → TokenTextSplitter → Embedding → 双写 Milvus + ES；`faq_chunk` 记录 `milvus_pk` / `es_doc_id`；`faq_article.status` 流转 INDEXED/FAILED。

---

### `service/FaqAnswerService.java` (service, transform)

**Analog:** `projects/knowledge-qa-platform/.../service/QaService.java` + 25-demo 缓存

**链路顺序：**
1. `semanticCacheService.lookup(query)` → 命中则秒回，`cache_hit=true`
2. `hybridSearchService.hybridSearch(query, topK)` → RAG 生成
3. `semanticCacheService.put(query, answer, ttl)` 回写

---

### `service/TicketService.java` (service, CRUD)

**Analog:** `projects/office-agent-assistant/.../service/ApprovalService.java`（部分）

**状态机校验模式**（无完整先例，参考 ApprovalService 写库 + BizException）:
```java
@Transactional
public CsTicket transition(Long ticketId, TicketStatus to, String actor, String reason) {
    CsTicket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "工单不存在"));
    TicketStatus from = ticket.getStatus();
    if (!ALLOWED_TRANSITIONS.get(from).contains(to)) {
        throw new BizException(CommonResultCode.BAD_REQUEST,
                "非法状态转移: " + from + " → " + to);
    }
    ticket.setStatus(to);
    ticketEventRepository.save(new CsTicketEvent(ticketId, from, to, actor, reason));
    return ticketRepository.save(ticket);
}
```

**合法转移：** OPEN→AI_PROCESSING；AI_PROCESSING→RESOLVED|PENDING_HUMAN；PENDING_HUMAN→HUMAN_HANDLING|AI_PROCESSING；HUMAN_HANDLING→RESOLVED；RESOLVED→CLOSED。

---

### `tool/HandoffTools.java` + `tool/ToolSecuritySupport.java` (service / middleware)

**Analog:** `examples/37-agent-hitl-demo/.../HighRiskTools.java` + `projects/office-agent-assistant/.../tool/ToolSecuritySupport.java`

**@Tool 触发 HITL** (HighRiskTools 模式):
```java
@Tool(description = "请求人工接管，需提供升级原因")
public String requestHumanHandoff(String reason, ToolContext toolContext) {
    String role = ToolSecuritySupport.requireRole(toolContext, "CUSTOMER", "AGENT");
    if (role == null) throw new IllegalStateException("无权请求人工接管");
    return ticketService.createOrEscalate(toolContext, reason);
}
```

**权限校验** (ToolSecuritySupport lines 35-41):
```java
public static String requireRole(ToolContext ctx, String... allowed) {
    String role = roleOf(ctx);
    for (String a : allowed) {
        if (a.equals(role)) return role;
    }
    return null;
}
```

---

### `prompt/PromptPublishService.java` (service, pub-sub)

**Analog:** `projects/knowledge-qa-platform/.../prompt/PromptPublishService.java`

**Nacos 推送** (lines 104-120):
```java
void publishConfigToNacos() {
    String json = objectMapper.writeValueAsString(payload);
    ConfigService configService = createConfigService();
    boolean ok = configService.publishConfig(
            "spring.ai.alibaba.configurable.prompt", "DEFAULT_GROUP", json, "json");
    if (!ok) {
        throw new BizException(CommonResultCode.INTERNAL_ERROR, "Nacos Prompt 配置推送失败");
    }
}
```

**model_profile 扩展：** 发布时同步推 `scs.model.profiles` JSON（同 `createConfigService()` 模式，Data ID 不同）。

---

### `admin/DashboardStatsService.java` (service, batch)

**Analog:** `projects/knowledge-qa-platform/.../admin/service/DashboardStatsService.java`

**Micrometer 优先 + DB 回退** (lines 64-89):
```java
private double resolveTotalCost(OffsetDateTime since) {
    double fromMetrics = sumGenAiUsageCost();
    if (fromMetrics > 0) return fromMetrics;
    return messageRepository.findByRoleAndCreatedAtAfter(ASSISTANT_ROLE, since).stream()
            .mapToDouble(this::estimateMessageCost)
            .sum();
}

private double sumGenAiUsageCost() {
    return Search.in(meterRegistry)
            .name("gen_ai.client.token.usage")
            .counters()
            .stream()
            .mapToDouble(counter -> counter.count() * pricing.pricePer1kInputTokens() / 1000.0)
            .sum();
}
```

**Phase 6 增量指标：** 工单量、缓存命中率（`cs_message.cache_hit`）、路由 Agent 分布（`route_agent` 列）。

---

### `admin/controller/DashboardAdminController.java` (controller, CRUD)

**Analog:** `projects/knowledge-qa-platform/.../admin/controller/DashboardAdminController.java`

**标准 Admin 控制器** (lines 19-33):
```java
@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardAdminController {
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(dashboardStatsService.stats(days));
    }
}
```

---

### `config/ObservabilityConfig.java` + `application.yml` (config)

**Analog:** `examples/45-observability-demo/src/main/resources/application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
saa:
  learning:
    cost-tracking:
      enabled: true
      price-per-1k-input-tokens: 0.004
      price-per-1k-output-tokens: 0.012
```

---

### `support/ScsPostgresRedisITBase.java` (test)

**Analog:** `projects/knowledge-qa-platform/.../support/KqaPostgresRedisITBase.java`

**Testcontainers 基座** (lines 17-44):
```java
@Testcontainers
@ExtendWith(DockerAvailableCondition.class)
public abstract class ScsPostgresRedisITBase {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:16"))
      .withDatabaseName("scs_platform")
      .withUsername("saa")
      .withPassword("saa123456");

  @Container
  static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7"))
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.ai.nacos.prompt.template.enabled", () -> "false");
    registry.add("scs.security.jwt.secret", () -> "dev-only-scs-jwt-secret-key-32bytes!!");
  }
}
```

**Milvus/ES：** IT 默认 `@MockBean VectorStore`（office 模式）；真机 IT 加 `@EnabledIfEnvironmentVariable(AI_DASHSCOPE_API_KEY)`。

---

### `test/AuthIntegrationTest.java` + `test/ChatIntegrationTest.java` (test)

**Analog:** `projects/office-agent-assistant/.../AuthIntegrationTest.java`

**@MockBean Agent 模式** (lines 41-54):
```java
@MockBean(name = "csIntentRouter")
private LlmRoutingAgent csIntentRouter;

@MockBean(name = "businessSupervisor")
private ReactAgent businessSupervisor;

@MockBean
private VectorStore milvusVectorStore;

@MockBean
private VectorStore elasticsearchVectorStore;
```

---

## Shared Patterns

### Result 统一响应
**Source:** `saa-learning-common` `Result<T>`
**Apply to:** 所有 Controller（SSE 除外）
```java
return Result.ok(data);
throw new BizException(CommonResultCode.NOT_FOUND, "...");
```

### JWT 认证
**Source:** `projects/knowledge-qa-platform/.../config/SecurityConfig.java`
**Apply to:** 所有 `/api/**` 接口
- 登录 `POST /api/auth/login` permitAll
- Nimbus JwtEncoder/JwtDecoder + `role` claim → `ROLE_*`
- `@PreAuthorize` 方法级 RBAC

### MessageChatMemoryAdvisor
**Source:** `projects/knowledge-qa-platform/.../service/QaService.java`
**Apply to:** ChatService 非 Agent 直调路径
```java
chatClient.prompt()
    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
    .user(question)
```

### 审计日志
**Source:** `projects/knowledge-qa-platform/.../service/AuditLogService.java`
**Apply to:** Prompt 发布、工单状态变更、人工接管

### MapStruct 转换
**Source:** `projects/knowledge-qa-platform/mapper/*`
**Apply to:** entity ↔ dto ↔ vo；`@Mapper(componentModel = "spring")`

### 小 DTO 同包原则
**Source:** `CLAUDE.md` / `saa-conventions`
**Apply to:** 仅 1～2 处使用的 record 放主包 `com.flywhl.saa.smartcs`，勿盲目拆 `model` 子包

### Agent 调用统一入口
**Source:** `examples/41-multi-agent-demo/.../MultiAgentController.java`
**Apply to:** CsOrchestratorService
```java
RunnableConfig config = RunnableConfig.builder()
        .threadId(conversationId)  // UUID
        .build();
Optional<OverAllState> state = flowAgent.invoke(query, config);
return FlowStateExtractor.extractText(state);
```

### 禁用 API 清单
**Apply to:** 全项目 grep 门禁
- `PromptChatMemoryAdvisor` → `MessageChatMemoryAdvisor`
- `SupervisorAgent` → `ReactAgent` + `AgentTool.create`
- `AgentTool.from` → `AgentTool.create`
- `interruptBefore` → `HumanInTheLoopHook.approvalOn`
- `FunctionCallback` → `@Tool`

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|------|
| `service/ModelAdminService.java` | service | CRUD | Phase 4/5 无 `model_profile` 表先例；参考 UserAdminService + PromptAdminService 组合 |
| `config/ConfigurableModelRouter.java` | middleware | request-response | 无 DB 驱动 scene 路由先例；参考 starter `FallbackModelRouter` + 自定义 scene 选择 |
| `config/ModelProfileNacosPublisher.java` | service | pub-sub | 无 `scs.model.profiles` 先例；复制 PromptPublishService Nacos 推送骨架 |
| `service/TicketService.java`（完整状态机） | service | CRUD | ApprovalService 仅单状态写入；需新增 `ALLOWED_TRANSITIONS` 枚举图 |

## Metadata

**Analog search scope:** `projects/knowledge-qa-platform/`, `projects/office-agent-assistant/`, `examples/41-multi-agent-demo/`, `examples/42-supervisor-demo/`, `examples/37-agent-hitl-demo/`, `examples/26-es-hybrid-demo/`, `examples/25-redis-vector-demo/`, `examples/45-observability-demo/`
**Files scanned:** ~120
**Pattern extraction date:** 2026-07-06
