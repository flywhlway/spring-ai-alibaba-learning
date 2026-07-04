# Phase 3: 48 个独立 Demo (Batch 3: 35~48) - Pattern Map

**Mapped:** 2026-07-04
**Files analyzed:** 98（14 Demo × 工程骨架 + 源码/配置/文档/IT）
**Analogs found:** 86 / 98
**Scope:** 仅 demos 35~48；01~34 不触碰

> Planner 注意：Agent/Graph 核心 API **无仓库内既有 Demo**，以 `03-RESEARCH.md`「教程 → 1.1.2.2 JAR API 映射」为准；本文件只映射**工程骨架、REST、Tools、双模块、starter 装配**等可复制模式。

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `examples/35-agent-demo/pom.xml` | config | — | `examples/11-tool-demo/pom.xml` | exact |
| `examples/35-agent-demo/.../AgentDemoApplication.java` | provider | request-response | `examples/11-tool-demo/.../ToolDemoApplication.java` | exact |
| `examples/35-agent-demo/.../DtcLookupTools.java` | service | request-response | `examples/11-tool-demo/.../MemberTools.java` | exact |
| `examples/35-agent-demo/.../VehicleDiagnosisAgentConfig.java` | config | transform | `examples/16-memory-demo/.../MemoryConfig.java` | role-match |
| `examples/35-agent-demo/.../AgentController.java` | controller | request-response | `examples/11-tool-demo/.../ToolController.java` | exact |
| `examples/35-agent-demo/src/main/resources/application.yml` | config | — | `examples/04-chat-demo/.../application.yml` | exact |
| `examples/35-agent-demo/README.md` + `api.http` | config | — | `examples/11-tool-demo/README.md` + `api.http` | exact |
| `examples/35-agent-demo/.../AgentDemoApplicationIT.java` | test | request-response | `examples/20-structured-output-demo/.../StructuredOutputDemoApplicationIT.java` | exact |
| `examples/36-agent-skills-demo/**`（骨架同 35） | controller/config/service | request-response | `examples/11-tool-demo/` + `16-memory-demo/` | role-match |
| `examples/36-.../skills/**/SKILL.md` | config | file-I/O | **无**（RESEARCH Skills API） | none |
| `examples/37-agent-hitl-demo/**`（骨架同 35） | controller/config/service | request-response | `examples/16-memory-demo/`（会话 id）+ `11-tool-demo/` | role-match |
| `examples/37-.../HitlController.java`（start/approve） | controller | request-response | `examples/16-memory-demo/.../MemoryController.java` | partial |
| `examples/38-workflow-demo/**` | controller/config | request-response | `examples/04-chat-demo/`（骨架）+ RESEARCH StateGraph | partial |
| `examples/39-graph-parallel-demo/**` | controller/config | request-response | 同 38 + RESEARCH 并行边 | partial |
| `examples/40-graph-saga-demo/**` | controller/config | request-response | 同 38 + RESEARCH Saga | partial |
| `examples/38~40` Graph 节点类 | service | transform | **无**（RESEARCH NodeAction） | none |
| `examples/41-multi-agent-demo/**` | controller/config | request-response | `examples/11-tool-demo/` + RESEARCH FlowAgent | role-match |
| `examples/42-supervisor-demo/**` | controller/config/service | request-response | `examples/11-tool-demo/` + RESEARCH AgentTool | role-match |
| `examples/43-a2a-nacos-demo/README.md` | config | — | `examples/34-mcp-nacos-demo/README.md` | exact |
| `examples/43-.../inventory-a2a-server/pom.xml` | config | — | `examples/34-.../order-mcp-server/pom.xml` | exact |
| `examples/43-.../office-a2a-client/pom.xml` | config | — | `examples/34-.../office-assistant-client/pom.xml` | exact |
| `examples/43-.../server/application.yml` | config | — | `examples/34-.../server/application.yml` | role-match |
| `examples/43-.../client/application.yml` | config | — | `examples/34-.../client/application.yml` | role-match |
| `examples/43-.../server/*Application.java` | provider | — | `examples/34-.../OrderMcpServerApplication.java` | exact |
| `examples/43-.../client/*Application.java` | provider | — | `examples/34-.../OfficeAssistantClientApplication.java` | exact |
| `examples/43-.../client/*Controller.java` | controller | request-response | `examples/34-.../AssistantController.java` | exact |
| `examples/43` A2A AgentCard / A2aRemoteAgent | config/service | request-response | **无**（RESEARCH a2a-nacos 键） | none |
| `examples/44-stream-demo/pom.xml` | config | — | `examples/04-chat-demo/pom.xml` + starter dep | role-match |
| `examples/44-stream-demo/.../StreamConfig.java` | config | streaming | `examples/16-memory-demo/.../MemoryConfig.java` | role-match |
| `examples/44-stream-demo/.../StreamController.java` | controller | streaming | `examples/04-chat-demo/.../ChatController.java` + RESEARCH SSE | partial |
| `examples/44-stream-demo/.../StreamDemoApplicationIT.java` | test | streaming | `examples/20-structured-output-demo/...IT.java` | role-match |
| `examples/45-observability-demo/**` | controller/config | request-response | `examples/04-chat-demo/` + starter CostTracking | role-match |
| `examples/46-logging-demo/**` | middleware/config | request-response | `examples/16-memory-demo/` + starter AuditLoggingAdvisor | role-match |
| `examples/46-.../TraceIdFilter.java` | middleware | request-response | **无**（common `Result` 读 MDC；Filter 自写最小） | none |
| `examples/47-routing-demo/pom.xml` | config | — | `examples/03-multi-model-demo/pom.xml` + starter | exact |
| `examples/47-routing-demo/.../application.yml` | config | — | `examples/03-multi-model-demo/.../application.yml` | exact |
| `examples/47-routing-demo/.../RoutingController.java` | controller | request-response | `examples/03-multi-model-demo/.../MultiModelController.java` + starter ModelRouter | role-match |
| `examples/47-routing-demo/.../RoutingDemoApplicationIT.java` | test | request-response | `examples/20-structured-output-demo/...IT.java` | exact |
| `examples/48-fallback-demo/**` | controller/config | request-response | 同 47 + `FallbackModelRouter.isFallbackActive` | exact |
| `examples/35~48` 各 `README.md` / `api.http` | config | — | `examples/11-tool-demo/` | exact |

## Pattern Assignments

### 共享工程骨架（全部 35~48）

**Analog:** `examples/04-chat-demo/` + `examples/11-tool-demo/`

**Application + GlobalExceptionHandler**（`ToolDemoApplication.java` lines 1-20）:
```java
package com.flywhl.saa.tool;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class ToolDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolDemoApplication.class, args);
    }
}
```

**pom.xml parent + common**（`11-tool-demo/pom.xml` lines 7-36）:
```xml
<parent>
    <groupId>com.flywhl.saa</groupId>
    <artifactId>spring-ai-alibaba-learning</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
<!-- web + validation + dashscope + saa-learning-common；零版本号 -->
```

**35~42 额外依赖**（RESEARCH，无仓库内 agent-framework Demo）:
```xml
<dependency>
  <groupId>com.alibaba.cloud.ai</groupId>
  <artifactId>spring-ai-alibaba-agent-framework</artifactId>
</dependency>
```

**44~48 额外依赖**:
```xml
<dependency>
  <groupId>com.flywhl.saa</groupId>
  <artifactId>saa-learning-starter</artifactId>
</dependency>
```

**application.yml 端口/密钥**（`04-chat-demo` lines 1-13，端口改为 `180NN`）:
```yaml
server:
  port: 18004
spring:
  application:
    name: chat-demo
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
```

**Controller 返回 `Result.ok`**（`ToolController.java` lines 26-34）:
```java
@GetMapping("/tool/context")
public Result<String> context(@RequestParam String question,
                               @RequestParam(defaultValue = "u-1001") String userId) {
    String content = chatClient.prompt()
            .toolContext(Map.of("userId", userId))
            .user(question)
            .call()
            .content();
    return Result.ok(content);
}
```

**README 模板**（`11-tool-demo/README.md`）：前置条件（中间件/环境变量）→ 运行端口 → 接口表 → curl + 预期 JSON → 源码导读。

**api.http**（`11-tool-demo/api.http`）:
```http
### 11-tool-demo · ToolContext 身份注入
GET http://localhost:18011/tool/context?question=帮我查一下我的会员等级&userId=u-1001
```

---

### `examples/35-agent-demo/.../DtcLookupTools.java`（service, request-response）

**Analog:** `examples/11-tool-demo/.../MemberTools.java`

**Imports + @Tool 模式**（lines 1-40）:
```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MemberTools {
    @Tool(description = "查询当前登录用户的会员等级，用户身份由服务端注入，不接受调用方声称的用户身份")
    public String getMyMembershipLevel(ToolContext toolContext) { /* ... */ }

    @Tool(description = "查询指定城市对应的服务端标准时间，结果为精确数据，无需模型转述", returnDirect = true)
    public TimeVO getServerTime(@ToolParam(description = "城市名称，如：上海") String city) { /* ... */ }
}
```

**Agent 装配差异（RESEARCH，非 ChatClient.defaultTools）:**
```java
// 禁止教程 .tools(component) / .maxIterations(6)
ReactAgent agent = ReactAgent.builder()
    .name("vehicle-diagnosis-agent")
    .model(dashScopeChatModel)
    .systemPrompt("...")
    .methodTools(dtcLookupTools)
    .hooks(ModelCallLimitHook.builder().runLimit(6).build())
    .saver(new MemorySaver())
    .build();
AssistantMessage msg = agent.call(query);
```

**Config 装配风格**（`MemoryConfig.java` lines 18-36 — `@Configuration` + `@Bean`）:
```java
@Configuration(proxyBeanMethods = false)
public class MemoryConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
```
→ 35 的 `VehicleDiagnosisAgentConfig` 同结构，Bean 类型改为 `ReactAgent`。

**小 DTO 同包**（D-08b）：参考 `13-http-tool-demo` 的 `StockPrice` 与 `StockPriceTools` 同包，勿拆 `model`。

---

### `examples/36-agent-skills-demo/**`（config + resources, file-I/O）

**Analog（骨架）:** 同 35（`11-tool-demo` + `16-memory-demo` Config）

**Skills API（无仓库模拟 — RESEARCH）:**
```java
// 禁止 Skill.of(...)
// 使用 ClasspathSkillRegistry / FileSystemSkillRegistry + SkillsAgentHook / SkillsInterceptor
// 资源：src/main/resources/skills/**/SKILL.md（execute 时对照 SkillScanner）
```

---

### `examples/37-agent-hitl-demo/.../HitlController.java`（controller, request-response）

**Analog:** `examples/16-memory-demo/.../MemoryController.java`（会话 id 贯穿）

**会话隔离模式**（lines 39-46）:
```java
@PostMapping("/chat")
public Result<String> chat(@Valid @RequestBody ChatRequest request) {
    String content = chatClient.prompt()
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
            .user(request.message())
            .call()
            .content();
    return Result.ok(content);
}
```

**HITL REST 契约（RESEARCH）:**
```java
// POST /hitl/start → 返回 threadId（UUID，禁止自增）
// POST /hitl/approve?threadId= → 同一 MemorySaver + RunnableConfig.threadId 恢复
HumanInTheLoopHook hitl = HumanInTheLoopHook.builder()
    .approvalOn("execute_payment", "支付操作需人工确认")
    .build();
// 禁止 .interruptBefore("execute_payment")
```

---

### `examples/38~40` Graph demos（controller/config, request-response / transform）

**Analog（工程骨架）:** `examples/04-chat-demo/`（Application / Result / yml / README）

**Graph API（无仓库模拟 — RESEARCH Pattern 3）:**
```java
// 禁止 KeyStrategy.replace()、addAggregatedEdge、显式声明 graph-core 版本
StateGraph graph = new StateGraph("parallel-diagnosis", () -> Map.of(
    "question", KeyStrategy.REPLACE,
    "kbResults", KeyStrategy.REPLACE,
    "historyResults", KeyStrategy.REPLACE,
    "answer", KeyStrategy.REPLACE));

graph.addNode("searchKb", node_async(nodes::searchKnowledgeBase))
     .addNode("searchHistory", node_async(nodes::searchTicketHistory))
     .addNode("generateAnswer", node_async(nodes::generateAnswer))
     .addEdge(StateGraph.START, List.of("searchKb", "searchHistory"))
     .addEdge(List.of("searchKb", "searchHistory"), "generateAnswer")
     .addEdge("generateAnswer", StateGraph.END);
```

**38 线性图:** `START → rewrite → retrieve → generate → END` + `MemorySaver`

**40 Saga（RESEARCH）:**
```java
graph.addNode("deductInventory", node_async(nodes::deductInventory))
     .addNode("chargePayment", node_async(nodes::chargePayment))
     .addNode("compensateInventory", node_async(nodes::compensateInventory))
     .addEdge(StateGraph.START, "deductInventory")
     .addEdge("deductInventory", "chargePayment")
     .addConditionalEdges("chargePayment",
         state -> Boolean.TRUE.equals(state.value("paymentSuccess").orElse(false)) ? "success" : "failure",
         Map.of("success", StateGraph.END, "failure", "compensateInventory"))
     .addEdge("compensateInventory", StateGraph.END);
// query forceFail=true 触发补偿路径
```

**Controller:** 注入编译后的 Graph / Agent，`Result.ok(...)` 包装 invoke 结果（对齐 `ToolController`）。

---

### `examples/41-multi-agent-demo/**`（controller, request-response）

**Analog:** `examples/11-tool-demo/`（Tools + Controller）+ RESEARCH FlowAgent

**四模式端点（RESEARCH）:** `SequentialAgent` / `ParallelAgent` / `LlmRoutingAgent` / `LoopAgent`

**API 纠偏:**
```java
// LoopAgent: .subAgent(agent) + .loopStrategy(new CountLoopStrategy(n))
// LlmRoutingAgent: .model(chatModel).systemPrompt(...).subAgents(List.of(...))
// 调用: FlowAgent.invoke → Optional<OverAllState>；ReactAgent.call(String) → AssistantMessage.getText()
```

---

### `examples/42-supervisor-demo/**`（controller/config/service, request-response）

**Analog:** `examples/11-tool-demo/`（子 Agent Tools）+ RESEARCH Pattern 2

**禁止 `SupervisorAgent`；用 ReactAgent + AgentTool:**
```java
ReactAgent calendar = ReactAgent.builder()
    .name("calendar-agent")
    .description("处理日程查询、安排会议等日程相关任务")
    .model(chatModel)
    .methodTools(calendarTools)
    .hooks(ModelCallLimitHook.builder().runLimit(4).build())
    .build();

ReactAgent supervisor = ReactAgent.builder()
    .name("office-supervisor")
    .model(chatModel)
    .tools(AgentTool.create(calendar), AgentTool.create(emailAgent))
    .systemPrompt("你是企业办公助手总控...")
    .hooks(ModelCallLimitHook.builder().runLimit(6).build())
    .build();
// 禁止 AgentTool.from(agent, "desc")；描述在子 Agent .description(...)
// 包名: com.alibaba.cloud.ai.graph.agent.AgentTool
```

---

### `examples/43-a2a-nacos-demo/**`（dual-module, request-response）

**Analog:** `examples/34-mcp-nacos-demo/`（结构/端口/启动顺序/README；**配置键不同**）

**目录与端口:**
| 34 模拟 | 43 新建 |
|---------|---------|
| `order-mcp-server` @ 18034 | `inventory-a2a-server` @ **18043** |
| `office-assistant-client` @ 18134 | `office-a2a-client` @ **18143** |
| `relativePath=../../../pom.xml` | 同 |

**Server Application**（`OrderMcpServerApplication.java` lines 13-19）:
```java
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class OrderMcpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderMcpServerApplication.class, args);
    }
}
```

**Client Controller**（`AssistantController.java` lines 16-33）:
```java
@RestController
public class AssistantController {
    private final ChatClient chatClient;
    // 43: 注入 A2aRemoteAgent / NacosAgentCardProvider，非 ToolCallbackProvider
    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        return Result.ok(chatClient.prompt().user(question).call().content());
    }
}
```

**README 启动顺序**（`34-mcp-nacos-demo/README.md` lines 10-25）:
```markdown
## 前置条件
- 中间件：`bash scripts/infra.sh up cloud`（Nacos）
## 启动顺序
bash scripts/infra.sh up cloud
# 先 Server 后 Client
```

**Nacos yml 结构**（34 server lines 14-24 — **键前缀改为 a2a**）:
```yaml
# 34 用 spring.ai.alibaba.mcp.nacos.* — 43 禁止照抄
# 43 必须用（RESEARCH）:
spring:
  ai:
    alibaba:
      a2a:
        nacos:
          server-addr: 127.0.0.1:8848
          username: nacos
          password: nacos
          registry:
            enabled: true
        server:
          card:
            name: inventory-agent
            description: 库存查询智能体
# Client: discovery.enabled: true
# A2aRemoteAgent.builder().agentCardProvider(nacosAgentCardProvider) — 禁止 .nacosServiceName()
```

**依赖:** `spring-ai-alibaba-starter-a2a-nacos`（双模块）；Client 另加 dashscope。无冒烟 IT（D-18，文档手动 infra）。

---

### `examples/44-stream-demo/**`（controller, streaming）

**Analog:** `examples/04-chat-demo/`（ChatClient）+ `16-memory-demo`（Advisor 挂载）+ starter `AuditLoggingAdvisor`

**ChatClient + Advisor 挂载**（`MemoryConfig.java` lines 31-36，Advisor 换 starter）:
```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder, AuditLoggingAdvisor audit) {
    return builder.defaultAdvisors(audit).build();
}
// starter 不会自动挂 Advisor — 必须显式 defaultAdvisors
```

**SSE（RESEARCH §17.5，仓库无既有 SSE Demo）:**
```java
@GetMapping(value = "/chat/stream-unified", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> streamUnified(@RequestParam String message) {
    return chatClient.prompt().user(message).stream().chatResponse()
        .map(cr -> ServerSentEvent.<String>builder()
            .event("message")
            .data(Optional.ofNullable(cr.getResult().getOutput().getText()).orElse(""))
            .build())
        .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("").build()))
        .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder()
            .event("error")
            .data(objectMapper.writeValueAsString(Result.fail(...)))
            .build()));
}
```

**同步端点仍用 `Result.ok`**（对齐 `ChatController.simple`）。

---

### `examples/45-observability-demo/**`（controller/config, request-response）

**Analog:** `examples/04-chat-demo/` + starter `CostTrackingObservationHandler`

**禁止**复制教程 `@Component` 版 Cost Handler；依赖 starter 自动装配（`SaaLearningAutoConfiguration` lines 63-68）:
```java
@Bean
@ConditionalOnMissingBean
@ConditionalOnProperty(prefix = "saa.learning.cost-tracking", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public CostTrackingObservationHandler costTrackingObservationHandler(...) { ... }
```

**额外依赖:** `spring-boot-starter-actuator` + `micrometer-registry-prometheus`

**yml 暴露 metrics:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics
  endpoint:
    prometheus:
      enabled: true
```

README 写清 `/actuator/prometheus`；Grafana 仅文档化。

---

### `examples/46-logging-demo/**`（middleware + config, request-response）

**Analog:** starter `AuditLoggingAdvisor` + common `Result` 读 MDC

**AuditLoggingAdvisor**（`AuditLoggingAdvisor.java` lines 24-54）:
```java
public class AuditLoggingAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();
        logRequest(request);
        ChatClientResponse response = chain.nextCall(request);
        logResponse(System.currentTimeMillis() - start);
        return response;
    }
}
```

**Result.traceId**（`common/.../Result.java` — MDC key `"traceId"`）:
```java
// Result.ok 自动携带 MDC.get("traceId")
return org.slf4j.MDC.get("traceId");
```

**TraceIdFilter（无仓库模拟）:** `OncePerRequestFilter` 生成 UUID 写入 `MDC.put("traceId", id)`，finally `MDC.remove`；Config 显式 `defaultAdvisors(audit)`。

---

### `examples/47-routing-demo/**` + `examples/48-fallback-demo/**`（controller, request-response）

**Analog:** `examples/03-multi-model-demo/`（双模型 Bean）+ starter `ModelRouter` / `FallbackModelRouter`

**pom 双 starter**（`03-multi-model-demo/pom.xml` lines 24-33）+ `saa-learning-starter`:
```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-deepseek</artifactId>
</dependency>
<dependency>
    <groupId>com.flywhl.saa</groupId>
    <artifactId>saa-learning-starter</artifactId>
</dependency>
```

**yml 双 Key**（`03-multi-model-demo/application.yml` lines 13-24）:
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
```

**Bean 名消歧**（`ChatClientConfig.java` lines 23-28 — 参数名匹配自动装配 Bean）:
```java
@Bean
public ChatClient dashScopeChatClient(ChatModel dashScopeChatModel) {
    return ChatClient.builder(dashScopeChatModel)
            .defaultAdvisors(new SimpleLoggerAdvisor()) // 47/48 换 AuditLoggingAdvisor
            .build();
}
```

**ModelRouter 装配条件**（`SaaLearningAutoConfiguration.java` lines 40-48）:
```java
@Bean
@ConditionalOnMissingBean
@ConditionalOnBean(name = {"dashScopeChatModel", "deepSeekChatModel"})
public ModelRouter modelRouter(SaaLearningProperties properties,
                                ApplicationContext applicationContext) {
    ChatModel primary = applicationContext.getBean(properties.primaryModel(), ChatModel.class);
    ChatModel fallback = applicationContext.getBean(properties.fallbackModel(), ChatModel.class);
    return new FallbackModelRouter(primary, fallback);
}
```

**路由调用（RESEARCH Pattern 4）:**
```java
ChatModel model = modelRouter.route();
try {
    return ChatClient.builder(model).build().prompt().user(q).call().content();
} catch (Exception e) {
    modelRouter.reportFailure(model, e);
    return ChatClient.builder(modelRouter.route()).build().prompt().user(q).call().content();
}
```

**48 降级状态**（`FallbackModelRouter.java` lines 71-73）:
```java
public boolean isFallbackActive() {
    return state.get().usingFallback();
}
// GET /fallback/status → Result.ok(Map.of("fallbackActive", router.isFallbackActive()))
// 需将注入类型收窄为 FallbackModelRouter，或在 Demo 内暴露 status 端点
```

**阈值语义**（`FallbackModelRouterTest.java` lines 31-43）: 连续 3 次 `reportFailure(primary)` 后 `isFallbackActive()==true`。

---

### 冒烟 IT（35 / 38 / 41 / 44 / 47）

**Analog:** `examples/20-structured-output-demo/.../StructuredOutputDemoApplicationIT.java`

```java
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class AgentDemoApplicationIT {
    @Autowired ReactAgent vehicleDiagnosisAgent; // 或 Graph / ChatClient / ModelRouter

    @Test
    void diagnoseReturnsText() {
        assertThat(vehicleDiagnosisAgent.call("P0420是什么").getText()).isNotBlank();
    }
}
```

| Demo | 断言焦点 |
|------|----------|
| 35 | `ReactAgent.call(...).getText()` 非空 |
| 38 | Graph invoke 产出非空 answer |
| 41 | 任一 FlowAgent 模式返回非空 |
| 44 | SSE / stream 至少一条 message 事件 |
| 47 | `ModelRouter` Bean 非空 + `route()` 可调用 |

43 无 IT（文档手动）；36/37/39/40/42/45/46/48 无强制 IT。

## Shared Patterns

### Application 引导 + 统一异常
**Source:** `examples/11-tool-demo/.../ToolDemoApplication.java` lines 13-19
**Apply to:** 全部 35~48 Application（含 43 双模块）
```java
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
```
注：44~48 引入 starter 后 `SaaLearningAutoConfiguration` 也会 `@Import(GlobalExceptionHandler)`；Application 仍显式 `@Import` 与 04+ 一致，无害。

### REST 响应体
**Source:** `examples/11-tool-demo/.../ToolController.java` + `common/.../Result.java`
**Apply to:** 全部同步 REST（SSE 错误事件 payload 也用 `Result.fail`）
```java
return Result.ok(content);
```

### @Tool 工具类
**Source:** `examples/11-tool-demo/.../MemberTools.java`
**Apply to:** 35~37、41~42 的 Tools；43 Server 侧工具若用 `@Tool` 同模式
```java
@Component
public class XxxTools {
    @Tool(description = "...")
    public String method(@ToolParam(description = "...") String arg) { ... }
}
```

### Config `@Bean` 装配
**Source:** `examples/16-memory-demo/.../MemoryConfig.java`
**Apply to:** Agent/Graph/ChatClient/Advisor 装配类
```java
@Configuration(proxyBeanMethods = false)
public class XxxConfig {
    @Bean
    public ReactAgent / ChatClient / CompiledGraph ...(...) { ... }
}
```

### 双模块 + Nacos + 端口偏移
**Source:** `examples/34-mcp-nacos-demo/`
**Apply to:** 43 only
- Server `180NN` / Client `180NN+100`
- `relativePath=../../../pom.xml`
- README：`infra.sh up cloud` → 先 Server 后 Client
- 开发凭证 `nacos/nacos` + 生产更换警告
- **配置键前缀不同**：34=`mcp.nacos`，43=`a2a.nacos`

### starter 强制复用（44~48）
**Source:** `starter/.../SaaLearningAutoConfiguration.java`
**Apply to:** 44~48 only（35~42 **禁止**引入 starter）
| Bean | 条件 | Demo 职责 |
|------|------|-----------|
| `AuditLoggingAdvisor` | 默认 on | 显式 `defaultAdvisors(audit)` |
| `CostTrackingObservationHandler` | 默认 on | 45 依赖即可，勿手写 |
| `ModelRouter` | 需 `dashScopeChatModel` + `deepSeekChatModel` | 47/48 注入使用 |
| `CostRecorder` | 默认 `LoggingCostRecorder` | 勿重复实现 |

### 双模型 Bean 名
**Source:** `examples/03-multi-model-demo/.../ChatClientConfig.java` + `SaaLearningProperties`
**Apply to:** 47/48
- 参数名 / Bean 名：`dashScopeChatModel`、`deepSeekChatModel`
- 默认 primary=`dashScopeChatModel`，fallback=`deepSeekChatModel`

### 冒烟 IT
**Source:** `examples/20-structured-output-demo/.../StructuredOutputDemoApplicationIT.java`
**Apply to:** 35、38、41、44、47
```java
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
```

### 禁用 API（全批）
- `PromptChatMemoryAdvisor` → `MessageChatMemoryAdvisor`
- `CallAroundAdvisor` / `AdvisedRequest` / `AdvisedResponse` → `CallAdvisor` / `ChatClientRequest` / `ChatClientResponse`
- `FunctionCallback` → `@Tool` / `ToolCallback`
- 可变 Options setter → Builder
- 教程伪 API：`SupervisorAgent`、`Skill.of`、`.maxIterations`、`AgentTool.from`、`addAggregatedEdge`、`A2aRemoteAgent.nacosServiceName`

## No Analog Found

| File / Concern | Role | Data Flow | Reason |
|----------------|------|-----------|--------|
| ReactAgent / ModelCallLimitHook / MemorySaver 装配 | config | transform | 仓库无 Agent Demo；用 RESEARCH Pattern 1 |
| SkillsRegistry + SKILL.md 资源布局 | config | file-I/O | 无既有 Skills Demo；execute 对照 SkillScanner |
| HumanInTheLoopHook resume 语义 | controller | request-response | 无 HITL Demo；RESEARCH + InterruptionMetadata |
| StateGraph / NodeAction / 条件边 / 并行边 | service | transform | 无 Graph Demo；RESEARCH Pattern 3 / Saga |
| FlowAgent 四模式（Sequential/Parallel/LlmRouting/Loop） | config | request-response | 无 MultiAgent Demo；RESEARCH API 映射 |
| AgentTool.create 监督者拓扑 | config | request-response | 无 SupervisorAgent；RESEARCH Pattern 2 |
| A2aRemoteAgent + NacosAgentCardProvider | config | request-response | 34 是 MCP 非 A2A；仅结构可抄，API/配置键用 RESEARCH |
| SSE `Flux<ServerSentEvent>` | controller | streaming | 仓库无 SSE Demo；RESEARCH §17.5 |
| TraceId `OncePerRequestFilter` | middleware | request-response | common 只读 MDC；Filter 最小自写 |
| actuator + prometheus yml | config | — | 仓库无既有 actuator Demo；Boot 标准配置 |

## Metadata

**Analog search scope:**
- `examples/03-multi-model-demo/`
- `examples/04-chat-demo/`
- `examples/11-tool-demo/`
- `examples/13-http-tool-demo/`（同包 DTO）
- `examples/16-memory-demo/`
- `examples/20-structured-output-demo/`（IT）
- `examples/34-mcp-nacos-demo/`
- `starter/src/main/java/`
- `starter/src/test/java/`
- `common/src/main/java/com/flywhl/saa/common/result/`
- `.planning/phases/03-48-demo/03-RESEARCH.md`（JAR API）

**Files scanned:** ~45 analog sources
**Pattern extraction date:** 2026-07-04
**Batch:** 3（demos 35~48 only；覆盖原 batch 2 PATTERNS.md）
