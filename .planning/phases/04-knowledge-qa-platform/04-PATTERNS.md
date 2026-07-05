# Phase 4: 知识库问答平台 - Pattern Map

**Mapped:** 2026-07-05
**Files analyzed:** 65（64 骨架 Java + 1 测试占位；`KnowledgeQaApplication` 已实现）
**Analogs found:** 52 / 65（13 项无仓库内近邻，见文末）

## 实施前置：骨架路径修复

**阻塞项：** 63 个占位类当前落在错误目录 `src/main/java/com\/flywhl\/saa\/knowledgeqa/`（字面反斜杠目录名），仅 `com/flywhl/saa/knowledgeqa/KnowledgeQaApplication.java` 在标准包路径。实现波次 0 须将全部 `.java` 迁移至 `com/flywhl/saa/knowledgeqa/**` 并删除畸形目录，否则 Maven 编译无法发现占位类。

---

## File Classification

### 入口与配置（波次 1）

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `KnowledgeQaApplication.java` | config | bootstrap | 各 Demo `*Application.java` | exact |
| `config/KqaProperties.java` | config | transform | `starter/.../SaaLearningProperties.java` | exact |
| `config/AiClientConfig.java` | config | request-response | `28-advanced-rag/AdvancedRagConfig` + `47-routing/RoutingController` + `44-stream/StreamConfig` | exact |
| `config/VectorStoreConfig.java` | config | CRUD | `24-milvus/MilvusController` + `application.yml` Milvus 段 | role-match |
| `config/ChatMemoryConfig.java` | config | request-response | `17-redis-memory/MemoryConfig` | exact |
| `config/MinioConfig.java` | config | file-I/O | — | no analog |
| `config/SecurityConfig.java` | middleware | request-response | — | no analog |
| `config/OpenApiConfig.java` | config | request-response | `docs/00-overview/ADR-006` | partial |
| `config/AsyncConfig.java` | config | event-driven | — | no analog |

### RAG 域（波次 2）

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `rag/RagPipelineFactory.java` | service | request-response | `28-advanced-rag/AdvancedRagConfig` | exact |
| `rag/CitationPostProcessor.java` | utility | transform | `24-milvus/MilvusController.toHit` + `28-advanced-rag` Document metadata | role-match |
| `rag/DocumentEtlPipeline.java` | service | batch | `27-rag/RagController.ingest` + `24-milvus/MilvusController.add` | role-match |
| `rag/IngestStatusTracker.java` | service | CRUD | — | no analog |

### 问答域 Controller / Service（波次 3）

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `controller/AuthController.java` | controller | request-response | `common/Result` + Demo Controller 构造注入 | role-match |
| `controller/QaController.java` | controller | streaming + request-response | `44-stream/StreamController` + `28-advanced-rag/AdvancedRagController` | exact |
| `controller/ConversationController.java` | controller | CRUD | `17-redis-memory/MemoryController` + `common/PageResult` | role-match |
| `controller/FeedbackController.java` | controller | CRUD | `24-milvus/MilvusController` POST 模式 | role-match |
| `service/AuthService.java` | service | request-response | — | no analog |
| `service/QaService.java` | service | streaming | `47-routing/RoutingController` + `17-redis-memory/MemoryController` + `28-advanced-rag` | exact |
| `service/ConversationService.java` | service | CRUD | `17-redis-memory/repository/RedisChatMemoryRepository` | role-match |
| `service/FeedbackService.java` | service | CRUD | — | no analog |

### Prompt / Tool

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `prompt/PromptTemplateProvider.java` | service | transform | `08-prompt-nacos/PromptNacosController` | exact |
| `prompt/PromptPublishService.java` | service | event-driven | `08-prompt-nacos` + Nacos Data ID 约定 | role-match |
| `tool/KnowledgeOpsTools.java` | component | request-response | `15-tool-security/KnowledgeAdminTools` | exact |

### 后台域 admin（波次 4）

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `admin/controller/DocumentAdminController.java` | controller | CRUD + file-I/O | `24-milvus/MilvusController` | role-match |
| `admin/controller/PromptAdminController.java` | controller | CRUD | `08-prompt-nacos/PromptNacosController` | role-match |
| `admin/controller/UserAdminController.java` | controller | CRUD | `24-milvus/MilvusController` | role-match |
| `admin/controller/AuditAdminController.java` | controller | CRUD | `common/PageResult` 分页模式 | role-match |
| `admin/controller/DashboardAdminController.java` | controller | request-response | `45-observability/ObservabilityController` + `starter/CostRecorder` | role-match |
| `admin/service/DocumentAdminService.java` | service | CRUD + batch | `rag/DocumentEtlPipeline`（编排） | role-match |
| `admin/service/PromptAdminService.java` | service | CRUD | `08-prompt-nacos` | role-match |
| `admin/service/UserAdminService.java` | service | CRUD | — | no analog |
| `admin/service/AuditQueryService.java` | service | CRUD | `starter/advisor/AuditLoggingAdvisor` | role-match |
| `admin/service/DashboardStatsService.java` | service | transform | `starter/metrics/CostRecorder` + `45-observability` | role-match |

### Model / Repository / Mapper

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `model/entity/*.java`（8 表） | model | CRUD | `db/schema.sql`（列名 SSOT） | partial |
| `model/dto/*.java`（5 个） | model | request-response | `24-milvus/DocumentRequest` record + `@NotBlank` | exact |
| `model/vo/*.java`（6 个） | model | transform | `common/Result` + `20-structured-output` record | role-match |
| `repository/*.java`（8 个） | middleware | CRUD | Spring Data JPA 惯例 + `schema.sql` | partial |
| `mapper/*.java`（4 个） | utility | transform | —（MapStruct，仓库无范例） | no analog |

### 测试（波次 5）

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `KnowledgeQaApplicationTests.java` | test | bootstrap | Phase 3 `*ApplicationIT` 系列 | role-match |
| （待建）`*IT.java` / `*Test.java` | test | 各域 | `44-stream/StreamDemoApplicationIT` 等 | exact |

---

## Pattern Assignments

### 波次 0：路径修复 + 全局约定

**Analog：** `KnowledgeQaApplication.java`（已实现）

```16:22:projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/KnowledgeQaApplication.java
@SpringBootApplication
@ConfigurationPropertiesScan
public class KnowledgeQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeQaApplication.class, args);
    }
}
```

- `@ConfigurationPropertiesScan` 扫描 `KqaProperties` 等 record 属性类。
- Starter 自动装配 `GlobalExceptionHandler`、`ModelRouter`、`AuditLoggingAdvisor`、`CostRecorder`（无需重复 `@Import`）。

---

### `config/KqaProperties.java`（config, transform）

**Analog：** `starter/src/main/java/com/flywhl/saa/starter/autoconfigure/SaaLearningProperties.java`

**record + 紧凑构造器默认值**（lines 19-36）：

```19:36:starter/src/main/java/com/flywhl/saa/starter/autoconfigure/SaaLearningProperties.java
@ConfigurationProperties(prefix = "saa.learning")
public record SaaLearningProperties(
        String primaryModel,
        String fallbackModel,
        boolean auditEnabled,
        CostTracking costTracking) {

    public SaaLearningProperties {
        if (primaryModel == null || primaryModel.isBlank()) {
            primaryModel = "dashScopeChatModel";
        }
        if (fallbackModel == null || fallbackModel.isBlank()) {
            fallbackModel = "deepSeekChatModel";
        }
        if (costTracking == null) {
            costTracking = new CostTracking(true, 0.0008, 0.002);
        }
    }
```

**绑定键 SSOT：** `application.yml` 的 `kqa.minio.*` / `kqa.rag.*` / `kqa.memory.*` / `kqa.security.jwt.*`（lines 101-119）。

---

### `config/AiClientConfig.java`（config, request-response）

**Analog：** `28-advanced-rag/AdvancedRagConfig` + `47-routing/RoutingController` + `44-stream/StreamConfig` + `17-redis-memory/MemoryConfig`

**Modular RAG Advisor 链**（`AdvancedRagConfig` lines 33-65）——参数改绑 `KqaProperties.rag()`：

```33:65:examples/28-advanced-rag-demo/src/main/java/com/flywhl/saa/advancedrag/AdvancedRagConfig.java
    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        var rewriteTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.build().mutate())
                .build();

        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(20)
                .build();

        DocumentPostProcessor scoreReranker = (query, documents) -> documents.stream()
                .sorted(Comparator.comparing(Document::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();

        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(rewriteTransformer)
                .documentRetriever(documentRetriever)
                .documentPostProcessors(scoreReranker)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(false)
                        .build())
                .build();

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(ragAdvisor)
                .build();
    }
```

**kqa 项目差异：**
- `similarityThreshold` → `0.35`，`topK` 检索 20 → postProcessor `limit(5)` 对齐 `kqa.rag.top-k`。
- `defaultSystem` 从 `PromptTemplateProvider.get("qa-system")` 读取，非硬编码。
- `defaultAdvisors` 顺序：`MessageChatMemoryAdvisor` → `RetrievalAugmentationAdvisor`（由 `RagPipelineFactory` 产出）→ `AuditLoggingAdvisor`。
- 底层 `ChatModel` 经 `ModelRouter.route()` 注入 `ChatClient.builder(model)`（非直接用 `chatClientBuilder` 默认模型）：

```28:38:examples/47-routing-demo/src/main/java/com/flywhl/saa/routing/RoutingController.java
    @GetMapping("/route/ask")
    public Result<String> ask(@RequestParam(defaultValue = "用一句话介绍多模型路由") String question) {
        ChatModel model = modelRouter.route();
        String content = ChatClient.builder(model)
                .defaultAdvisors(auditLoggingAdvisor)
                .build()
                .prompt()
                .user(question)
                .call()
                .content();
        return Result.ok(content);
    }
```

**显式挂载 AuditLoggingAdvisor**（Starter 不会自动挂到 ChatClient）：

```15:21:examples/44-stream-demo/src/main/java/com/flywhl/saa/stream/StreamConfig.java
@Configuration(proxyBeanMethods = false)
public class StreamConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AuditLoggingAdvisor auditLoggingAdvisor) {
        return builder.defaultAdvisors(auditLoggingAdvisor).build();
    }
}
```

**禁用 API：** 不用 `PromptChatMemoryAdvisor`、`QuestionAnswerAdvisor`（Naive RAG）、`CallAroundAdvisor`。

---

### `config/ChatMemoryConfig.java`（config, request-response）

**Analog：** `17-redis-memory/MemoryConfig` + `RedisChatMemoryRepository`

```17:32:examples/17-redis-memory-demo/src/main/java/com/flywhl/saa/redismemory/MemoryConfig.java
@Configuration(proxyBeanMethods = false)
public class MemoryConfig {

    @Bean
    public ChatMemory chatMemory(RedisChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(50)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
```

**kqa 差异：**
- `maxMessages` → `kqa.memory.max-messages`（20）。
- Redis key 前缀建议 `kqa:chat-memory:`（复制 `RedisChatMemoryRepository` 模式，加 TTL）。
- 会话调用时显式 `conversationId`：

```24:31:examples/17-redis-memory-demo/src/main/java/com/flywhl/saa/redismemory/MemoryController.java
    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String message, @RequestParam String userId) {
        String content = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .user(message)
                .call()
                .content();
        return Result.ok(content);
    }
```

---

### `config/VectorStoreConfig.java`（config, CRUD）

**Analog：** `24-milvus/MilvusController` + `application.yml` `spring.ai.vectorstore.milvus`

Milvus 由 Spring AI 自动配置（`collection-name: kqa_knowledge`、`embedding-dimension: 1024`）。本类仅补充：
- 启动时 collection 健康校验（可选 `@PostConstruct`）。
- 暴露 `SearchRequest` 默认参数 Bean（供 `RagPipelineFactory` 读取 `kqa.rag.*`）。

**Document metadata 入库模式**（ETL 须写入 citation 字段）：

```33:40:examples/24-milvus-demo/src/main/java/com/flywhl/saa/milvus/MilvusController.java
    @PostMapping("/documents")
    public Result<Map<String, Object>> add(@Valid @RequestBody DocumentRequest request) {
        Map<String, Object> metadata = request.metadata() != null ? request.metadata() : Map.of();
        Document document = new Document(request.content(), metadata);
        vectorStore.add(List.of(document));
        return Result.ok(Map.of(
                "id", document.getId(),
                "message", "已入库 1 条文档"));
```

**kqa ETL metadata 键：** `documentId`、`chunkId`、`title`（供 `CitationPostProcessor` 组装 `CitationVO`）。

---

### `rag/RagPipelineFactory.java`（service, request-response）

**Analog：** `28-advanced-rag/AdvancedRagConfig`（整段复制并参数化）

- 从 `KqaProperties` 读取 `top-k`、`similarity-threshold`。
- 查询改写 prompt 从 `PromptTemplateProvider.get("query-rewrite")` 注入 `RewriteQueryTransformer`（若 API 支持自定义 template；否则保持 demo 默认 builder + 外部 system prompt）。
- 产出 `RetrievalAugmentationAdvisor` Bean，供 `AiClientConfig` 注入。

---

### `rag/DocumentEtlPipeline.java`（service, batch）

**Analog：** `27-rag/RagController.ingest` + `24-milvus/MilvusController.add`

**入库样例**（lines 36-49）：

```36:49:examples/27-rag-demo/src/main/java/com/flywhl/saa/rag/RagController.java
    @PostMapping("/ingest")
    public Result<Map<String, Object>> ingest() {
        List<Document> documents = List.of(
                new Document(
                        "OTA 升级失败常见原因包括：网络中断导致包体传输不完整、签名校验失败、存储空间不足。",
                        Map.of("source", "OTA故障排查手册.pdf", "page", "12")),
                // ...
        vectorStore.add(documents);
        return Result.ok(Map.of("ingested", documents.size()));
    }
```

**kqa 流水线：**
1. `@Async("etlExecutor")` 由 `AsyncConfig` 提供线程池。
2. MinIO 下载 → Tika `DocumentReader` → `TokenTextSplitter`（`chunk-size=512`、`chunk-overlap=64`，Builder 写法）。
3. 每 chunk：`vectorStore.add` + `kb_chunk` 批量 `saveAll`（`milvus_pk` = `Document.getId()`）。
4. 状态机经 `IngestStatusTracker`：`UPLOADED → PARSING → INDEXED | FAILED`。

---

### `rag/CitationPostProcessor.java`（utility, transform）

**Analog：** `24-milvus/MilvusController.toHit`（score + metadata 提取）

```79:86:examples/24-milvus-demo/src/main/java/com/flywhl/saa/milvus/MilvusController.java
    private static Map<String, Object> toHit(Document doc) {
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("id", doc.getId());
        hit.put("content", doc.getText());
        hit.put("metadata", doc.getMetadata());
        hit.put("score", doc.getScore());
        return hit;
    }
```

映射到 `CitationVO`：`documentId`、`documentTitle`、`chunkId`、`snippet`、`score`。答案正文不内联 `[1]` 脚注。

---

### `controller/QaController.java`（controller, streaming + request-response）

**Analog：** `28-advanced-rag/AdvancedRagController`（同步）+ `44-stream/StreamController`（SSE）

**同步问答**（lines 47-51）：

```47:51:examples/28-advanced-rag-demo/src/main/java/com/flywhl/saa/advancedrag/AdvancedRagController.java
    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        String answer = chatClient.prompt().user(question).call().content();
        return Result.ok(answer);
    }
```

**kqa：** `POST /api/qa/ask` + `@Valid QaRequest` → `Result<QaAnswerVO>`（含 `citations[]`）。

**SSE 统一协议**（lines 31-58）——扩展 `meta` 事件：

```31:58:examples/44-stream-demo/src/main/java/com/flywhl/saa/stream/StreamController.java
    @GetMapping(value = "/chat/stream-unified", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamUnified(@RequestParam String message) {
        return chatClient.prompt().user(message).stream().chatResponse()
                .map(chatResponse -> {
                    String text = chatResponse.getResult().getOutput().getText();
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(text == null ? "" : text)
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("").build()))
                .onErrorResume(ex -> Flux.just(buildErrorEvent(ex)));
    }

    private ServerSentEvent<String> buildErrorEvent(Throwable ex) {
        Result<Void> errorPayload = Result.fail(CommonResultCode.INTERNAL_ERROR, safeMessage(ex));
        // ...
                    .event("error")
```

**kqa SSE 契约（README §6）：** `message` → `meta`（citations + token usage JSON）→ `done`；`error` payload 复用 `Result`。

---

### `controller/AuthController.java` + `service/AuthService.java`（controller/service, request-response）

**Analog：** `common/Result` 响应 + 各 Demo 构造器注入 Controller

**无 JWT 范例**——按 Spring Security OAuth2 Resource Server 标准实现：
- `POST /api/auth/login` 匿名，`UserDetailsService` 校验 `sys_user`（`DelegatingPasswordEncoder` 兼容 `{noop}` / BCrypt）。
- 签发 JWT（`kqa.security.jwt.*`）。
- `GET /api/auth/me` 返回当前用户 VO。

业务异常抛 `BizException`，由 `GlobalExceptionHandler` 转 `Result`。

---

### `controller/ConversationController.java` + `service/ConversationService.java`（CRUD）

**Analog：** `17-redis-memory` + `common/PageResult`

**分页响应：**

```35:37:common/src/main/java/com/flywhl/saa/common/result/PageResult.java
    public static <T> PageResult<T> of(long pageNo, long pageSize, long total, List<T> records) {
        return new PageResult<>(pageNo, pageSize, total, records);
    }
```

Controller 返回 `Result<PageResult<ConversationVO>>`。删除会话时：`RedisChatMemoryRepository.deleteByConversationId` + PG 归档更新。

---

### `admin/controller/*` + `admin/service/*`（controller/service, CRUD）

**Analog：** `24-milvus/MilvusController`（REST + `Result`）+ `08-prompt-nacos`（Prompt 域）

**标准 Controller 模式**（构造注入 + `Result.ok`）：

```24:40:examples/24-milvus-demo/src/main/java/com/flywhl/saa/milvus/MilvusController.java
@RestController
public class MilvusController {

    private final VectorStore vectorStore;

    public MilvusController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/documents")
    public Result<Map<String, Object>> add(@Valid @RequestBody DocumentRequest request) {
        // ...
        return Result.ok(Map.of(
                "id", document.getId(),
                "message", "已入库 1 条文档"));
    }
```

**权限：** 方法级 `@PreAuthorize("hasRole('ADMIN')")`（`SecurityConfig` 启用方法安全）。

**看板：** `DashboardStatsService` 聚合 `CostRecorder` 日志/`gen_ai.usage.*` 指标 + DB 计数（`kb_document`、`qa_message`、`qa_feedback`）。

---

### `prompt/PromptTemplateProvider.java` + `PromptPublishService.java`（service, transform）

**Analog：** `08-prompt-nacos/PromptNacosController`

**三级回退中的 Nacos 层**（lines 34-45）：

```34:45:examples/08-prompt-nacos-demo/src/main/java/com/flywhl/saa/promptnacos/PromptNacosController.java
    @GetMapping("/diagnosis")
    public Result<String> diagnose(@RequestParam String code) {
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate("dtc-diagnosis");
        if (template == null) {
            template = promptTemplateFactory.create(
                    "dtc-diagnosis",
                    "请分析故障码 {code} 的可能原因",
                    Map.of("code", "P0000"));
        }
        String prompt = template.render(Map.of("code", code));
        return Result.ok(chatClient.prompt().user(prompt).call().content());
    }
```

**kqa 回退链：** Nacos `getTemplate(key)` → DB `PUBLISHED` → classpath `prompts/*.st`。发布时 `PromptPublishService` 推 Nacos Data ID `spring.ai.alibaba.configurable.prompt`。

---

### `tool/KnowledgeOpsTools.java`（component, request-response）

**Analog：** `15-tool-security/KnowledgeAdminTools`（exact）

```19:50:examples/15-tool-security-demo/src/main/java/com/flywhl/saa/toolsecurity/KnowledgeAdminTools.java
@Component
public class KnowledgeAdminTools {

    @Tool(description = "查询知识库文档数量")
    public int countDocuments(ToolContext toolContext) {
        String role = roleOf(toolContext);
        // ...
    }

    @Tool(description = "删除指定 ID 的知识库文档，仅管理员可执行", returnDirect = true)
    public String deleteDocument(@ToolParam(description = "文档 ID") String docId, ToolContext toolContext) {
        if (!"ADMIN".equals(role)) {
            String denied = "权限不足：删除操作仅管理员可执行，当前角色为 " + role;
            return denied;
        }
        // ...
    }

    private String roleOf(ToolContext toolContext) {
        return (String) toolContext.getContext().getOrDefault("role", "USER");
    }
```

**挂载示例**（`ToolSecurityController`）：

```18:28:examples/15-tool-security-demo/src/main/java/com/flywhl/saa/toolsecurity/ToolSecurityController.java
    public ToolSecurityController(ChatClient.Builder chatClientBuilder, KnowledgeAdminTools tools) {
        this.chatClient = chatClientBuilder.defaultTools(tools).build();
    }
    // ...
        return chatClient.prompt()
                .toolContext(Map.of("role", role))
                .user(question)
```

---

### `model/dto/*.java`（model, request-response）

**Analog：** `24-milvus/DocumentRequest`

```12:16:examples/24-milvus-demo/src/main/java/com/flywhl/saa/milvus/DocumentRequest.java
public record DocumentRequest(
        @NotBlank String content,
        Map<String, Object> metadata
) {
}
```

各 DTO 用 Java record + `jakarta.validation` 注解；Controller 参数 `@Valid @RequestBody`。

---

### `model/entity/*.java`（model, CRUD）

**Analog：** `projects/knowledge-qa-platform/db/schema.sql`（SSOT，无 JPA 范例）

- `@Entity` + `@Table(name = "...")` 列名与 DDL 一致。
- `ddl-auto: none`，禁止 Hibernate 自动建表。
- 时间字段 `TIMESTAMPTZ` → `OffsetDateTime` 或 `Instant`。
- `qa_message.citations` / `audit_log.detail` → `@JdbcTypeCode(SqlTypes.JSON)` 或 `String` + 手动序列化。

---

### `repository/*.java`（middleware, CRUD）

**Analog：** Spring Data JPA 惯例（仓库无企业项目范例）

```java
public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {
    Page<KbDocument> findByStatusAndCategory(String status, String category, Pageable pageable);
}
```

派生查询命名按 `schema.sql` 索引列（`status`、`category`、`template_key` 等）。

---

### `mapper/*.java`（utility, transform）

**Analog：** 无仓库范例；按 MapStruct 标准：

```java
@Mapper(componentModel = "spring")
public interface DocumentConverter {
    DocumentVO toVo(KbDocument entity);
}
```

---

### 测试（波次 5）

#### `KnowledgeQaApplicationTests.java` + 待建 IT

**Analog（Phase 3 冒烟 IT）：** `44-stream/StreamDemoApplicationIT`

```19:37:examples/44-stream-demo/src/test/java/com/flywhl/saa/stream/StreamDemoApplicationIT.java
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class StreamDemoApplicationIT {

    @Autowired
    private ChatClient chatClient;

    @Test
    void streamReturnsNonEmptyContent() {
        List<String> chunks = chatClient.prompt()
                .user("用一句话介绍 Spring AI")
                .stream()
                .content()
                .collectList()
                .block(Duration.ofSeconds(60));

        assertThat(chunks).isNotEmpty();
    }
}
```

**必建 IT（CONTEXT D-29）：**
| 测试类 | 覆盖 | 门控 |
|--------|------|------|
| `AuthIT` | login 签发 JWT | `@EnabledIfEnvironmentVariable(AI_DASHSCOPE_API_KEY)` 可选；login 本身可无模型 |
| `QaAskIT` | `/api/qa/ask` + citations | 需 API Key + Milvus |
| `QaStreamIT` | SSE message/meta/done | 需 API Key + Milvus |
| `KnowledgeQaApplicationIT` | Context 加载 | 无 Key 可绿 |

**Testcontainers（CONTEXT D-28）：** 仓库内**无现有范例**。按 `.claude/skills/saa-conventions/SKILL.md`：

```java
@Testcontainers
@SpringBootTest
class XxxIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        // ...
    }
}
```

Milvus IT **可选**（文档声明手动 infra，不阻塞无 Docker 全绿）。

**单元测试 Analog：** `starter/.../FallbackModelRouterTest`（Mockito + AssertJ + `@DisplayName`）

```22:28:starter/src/test/java/com/flywhl/saa/starter/routing/FallbackModelRouterTest.java
    @Test
    @DisplayName("初始状态下路由到主模型")
    void shouldRouteToPrimaryByDefault() {
        FallbackModelRouter router = new FallbackModelRouter(primary, fallback);
        assertThat(router.route()).isSameAs(primary);
        assertThat(router.isFallbackActive()).isFalse();
    }
```

优先单测：`AuthService`、`DocumentEtlPipeline`（mock MinIO/VectorStore）、`CitationPostProcessor`、`RagPipelineFactory`（mock VectorStore）。

---

## Shared Patterns

### 统一响应 `Result<T>`

**Source:** `common/src/main/java/com/flywhl/saa/common/result/Result.java`
**Apply to:** 所有同步 Controller（非 SSE）

```41:43:common/src/main/java/com/flywhl/saa/common/result/Result.java
    public static <T> Result<T> ok(T data) {
        return new Result<>(CommonResultCode.SUCCESS.code(), CommonResultCode.SUCCESS.message(), data, currentTraceId());
    }
```

分页：`Result<PageResult<T>>`，`PageResult.of(pageNo, pageSize, total, records)`。

### 全局异常处理

**Source:** `common/.../GlobalExceptionHandler.java`
**Apply to:** 全项目（Starter `@Import` 已启用）

```41:49:common/src/main/java/com/flywhl/saa/common/exception/GlobalExceptionHandler.java
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        // ...
        return Result.fail(ex.getResultCode(), ex.getMessage());
    }
```

Service 层可预期错误：`throw new BizException(CommonResultCode.BAD_REQUEST, "文档不存在")`。

### Starter 审计 + 成本

**Source:** `starter/.../SaaLearningAutoConfiguration.java` + `AuditLoggingAdvisor` + `CostRecorder`
**Apply to:** `AiClientConfig`、`QaService` 流式/同步调用

- `saa.learning.audit-enabled: true` → 自动注册 `AuditLoggingAdvisor`。
- `saa.learning.cost-tracking.enabled: true` → `gen_ai.usage.*` + `LoggingCostRecorder`。
- 看板读取 `CostRecorder` 或 Micrometer 指标。

### ChatClient Advisor 链（禁用废弃 API）

| 组件 | 新写法 |
|------|--------|
| 会话记忆 | `MessageChatMemoryAdvisor` + `ChatMemory.CONVERSATION_ID` |
| RAG | `RetrievalAugmentationAdvisor`（非 `QuestionAnswerAdvisor`） |
| 审计 | `AuditLoggingAdvisor`（`CallAdvisor`/`StreamAdvisor`） |
| Options | `XxxOptions.builder()...build()` |

### Document metadata 约定（Citation 溯源）

**Apply to:** `DocumentEtlPipeline`、`CitationPostProcessor`、`QaAnswerVO.citations`

| metadata 键 | 用途 |
|-------------|------|
| `documentId` | PG `kb_document.id` |
| `chunkId` | PG `kb_chunk.id` |
| `title` | 文档标题 |
| `milvus_pk` | Milvus 主键（= `Document.getId()`） |

### 配置与密钥

**Source:** `application.yml` + `saa-conventions/SKILL.md`
- 密钥仅 `${AI_DASHSCOPE_API_KEY}` 等环境变量。
- 中间件：`bash scripts/infra.sh up core vector cloud` + kqa profile override。

---

## No Analog Found

| File / 域 | Role | Data Flow | Reason |
|-----------|------|-----------|--------|
| `config/SecurityConfig.java` | middleware | request-response | 仓库无 JWT/OAuth2 Resource Server 范例 |
| `config/MinioConfig.java` | config | file-I/O | 无 MinIO Demo |
| `config/AsyncConfig.java` | config | event-driven | 无 `@Async` 线程池范例 |
| `config/OpenApiConfig.java` | config | request-response | 无 `GroupedOpenApi` 代码范例（仅 ADR-006 文档） |
| `service/AuthService.java` | service | request-response | 无 JWT 签发范例 |
| `service/FeedbackService.java` | service | CRUD | 无反馈域范例 |
| `admin/service/UserAdminService.java` | service | CRUD | 无用户管理范例 |
| `rag/IngestStatusTracker.java` | service | CRUD | 无状态机跟踪范例 |
| `mapper/*.java`（4 个） | utility | transform | 仓库无 MapStruct 实现 |
| `model/entity/*.java`（8 个） | model | CRUD | 无 JPA `@Entity` 范例（以 `schema.sql` 为准） |
| `repository/*.java`（8 个） | middleware | CRUD | 无 JPA Repository 范例 |
| Testcontainers IT | test | bootstrap | 无 `@Testcontainers` 范例（遵循 skill 约定新建） |

**Planner 指引：** 上表项实现时以 `04-CONTEXT.md` 决策 + Spring Boot/Spring Security/Spring Data JPA 官方模式为准；Entity/Repository 列名严格对齐 `db/schema.sql`。

---

## Metadata

**Analog search scope:** `projects/knowledge-qa-platform/`、`examples/{27,28,24,17,44,47,15,08,45}-*`、`common/`、`starter/`、Phase 3 `*ApplicationIT.java`
**Files scanned:** ~120
**Pattern extraction date:** 2026-07-05

**波次与 pattern 源对照（CONTEXT D-04）：**

| 波次 | 包 | 主要 pattern 源 |
|------|-----|----------------|
| 0 | 路径修复 | — |
| 1 | `config/*` | 28-advanced-rag, 17-redis-memory, 47-routing, 44-stream, SaaLearningProperties |
| 2 | `rag/*` | 28-advanced-rag, 27-rag, 24-milvus |
| 3 | `controller` + `service` | 44-stream, 28-advanced-rag, 17-redis-memory, common/Result |
| 4 | `admin/*` | 24-milvus, 08-prompt-nacos, 45-observability, starter/CostRecorder |
| 5 | `src/test` | 44-stream IT, 47-routing IT, starter 单测, saa-conventions Testcontainers |
