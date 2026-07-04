# 第 17 章：Streaming 流式输出

## 学习目标

- 掌握 `.stream()` API 的完整用法，理解 `Flux<String>` 与 `Flux<ChatResponse>` 的区别；
- 能在 Spring MVC 与 WebFlux 两种技术栈下正确实现 SSE 流式接口；
- 理解流式场景下工具调用、结构化输出的现实限制与应对策略；
- 建立本仓库统一的 SSE 事件协议约定（呼应第 00 章总体架构规范）。

## 前置知识

- 完成第 01~16 章，尤其是第 04 章（ChatClient 基础 API）与第 16 章（结构化输出限制）。

## 核心概念

### 17.1 为什么需要流式

模型生成通常需要几秒到十几秒，同步等待完整响应会让用户长时间面对空白界面。流式输出让文本"逐字/逐句"实时呈现，是几乎所有主流 AI 产品（ChatGPT、Claude 等）的标配交互模式，也是本教程第 13/15 章 Agent/多智能体场景中管理用户等待预期的关键手段。

### 17.2 两种流式返回类型

```java
// 只要文本增量
Flux<String> textStream = chatClient.prompt().user("讲个笑话").stream().content();

// 要完整的 ChatResponse（含每个 chunk 的 Metadata）
Flux<ChatResponse> responseStream = chatClient.prompt().user("讲个笑话").stream().chatResponse();
```

`Usage` 信息通常只在流的**最后一个 chunk**中完整携带（第 04 章已提及这个细节），如果需要统计流式请求的 token 消耗，应该用 `chatResponse()` 并在 `.doOnComplete()`/最后一条消息中读取。

## API 深入解析

### 17.3 Spring MVC 下的 SSE 实现

```java
@RestController
public class StreamController {

    private final ChatClient chatClient;

    public StreamController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}
```

Spring MVC（Servlet 栈）对 `Flux<String>` 返回值有原生支持，框架会自动把它转换为 SSE 响应流——这是最简单的流式实现方式，本仓库大多数 Demo 采用此模式（与父 POM 统一的 `spring-boot-starter-web` 技术栈一致）。

### 17.4 WebFlux 下的注意事项

```java
// ⚠️ 错误示例：在 WebFlux（Reactor 非阻塞线程）里调用 .call()（阻塞方法）
@GetMapping("/chat")
public Mono<String> chat(String message) {
    return Mono.fromCallable(() -> chatClient.prompt().user(message).call().content());
    // 若不显式调度到边界线程池，会抛出：
    // block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-3
}
```

如果应用是 `spring-boot-starter-webflux` 技术栈（本仓库统一用 `starter-web`，此处作为知识点补充），必须使用真正的响应式调用（`.stream()`）或显式把阻塞调用调度到专用线程池（`Schedulers.boundedElastic()`），不能在 Reactor 的 event-loop 线程里做阻塞调用——这是响应式编程的基本纪律，与你在 LangGraph/FastAPI 生态里对"不要在 async 函数里做同步阻塞 IO"的认知是相通的。

### 17.5 统一 SSE 事件协议（本仓库约定）

第 00 章总体架构规划中约定了统一的 SSE 事件类型（`message`/`meta`/`error`/`done`），本节给出具体实现：

```java
@RestController
public class UnifiedStreamController {

    private final ChatClient chatClient;

    public UnifiedStreamController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

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
                .onErrorResume(ex -> {
                    var errorPayload = Result.fail(CommonResultCode.INTERNAL_ERROR, ex.getMessage());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(toJson(errorPayload))
                            .build());
                });
    }

    private String toJson(Result<?> result) {
        // 实际项目注入 ObjectMapper 完成序列化，此处示意
        return "{\"code\":%d,\"message\":\"%s\"}".formatted(result.code(), result.message());
    }
}
```

`error` 事件的 payload 复用第 00 章 `common` 模块的 `Result` 结构——这保证了前端无论收到同步接口的错误响应还是流式接口的错误事件，处理逻辑都是统一的，不需要写两套错误处理代码。

## 可运行 Demo：统一协议流式问答

对应仓库位置：`examples/44-stream-demo`。

### application.yml

```yaml
server:
  port: 18044

spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
```

### StreamDemoController.java

（完整实现参照 §17.5 `UnifiedStreamController`，实际工程中注入真实 `ObjectMapper` 替换示意的 `toJson` 方法。）

### 运行与验证

```bash
cd examples/44-stream-demo
mvn spring-boot:run
curl -N "http://localhost:18044/chat/stream-unified?message=用三句话介绍Spring AI Alibaba"
```

### 预期输出（SSE 原始流，`-N` 参数关闭 curl 缓冲以实时查看）

```text
event:message
data:Spring

event:message
data: AI Alibaba 是

event:message
data:阿里巴巴基于 Spring AI 构建的企业级智能体开发框架...

event:done
data:
```

可以观察到文本是分多个 `message` 事件逐步到达的（具体分片方式取决于模型 API 的流式返回粒度），最后以一个 `done` 事件标记流结束——前端据此可以做打字机效果渲染和加载状态管理。

## 关键源码解读

Spring MVC 对 `Flux<T>` 返回值的原生支持，底层依赖 Servlet 3.1+ 的异步请求处理能力（`AsyncContext`），Spring 框架把 Reactor 的响应式流适配到了 Servlet 的异步 IO 模型上——这意味着即便你的应用整体是"传统" Servlet 栈（而非全响应式的 WebFlux），依然可以享受流式响应带来的非阻塞 IO 优势，不需要为了流式功能被迫迁移整个技术栈到 WebFlux，这也是本仓库统一选择 `spring-boot-starter-web` 而非 `webflux` 的实用主义考量之一。

## 企业实践建议

- **全仓库统一 SSE 事件协议是团队协作的效率保障**（本章 §17.5 已经落地），避免每个 Demo/项目发明一套自己的事件格式，前端团队可以用统一的解析逻辑对接所有流式接口；
- **流式接口也要有超时保护**：模型响应异常缓慢或卡住时，应用层应设置合理的超时并通过 `error` 事件通知前端，而不是让连接无限挂起；
- **移动端网络环境下 SSE 的重连策略需要额外设计**：原生 SSE 协议支持自动重连，但重连后如何恢复到断连前的状态（是否需要重新发起请求、是否需要幂等处理）需要结合具体业务场景设计。

## 性能优化建议

- 流式响应能显著改善**用户感知延迟**（首字节时间 TTFB 远小于完整响应时间），但不会改变**总处理时间**，如果业务关注的是后端吞吐量而非用户体验，流式并不会带来吞吐量提升；
- 高并发流式连接会长时间占用连接资源（相比同步请求"请求-响应"后立即释放），需要评估应用服务器的连接数上限配置是否匹配预期并发量。

## 安全建议

- 流式接口同样需要走第 06 章的 Advisor 安全链路（审计、内容安全过滤），`SafeGuardAdvisor` 等安全类 Advisor 对 `adviseStream` 的实现要确保不会因为异步特性而遗漏安全检查（第 06 章已提示此踩坑点）；
- SSE 长连接相比短连接更容易成为连接耗尽型攻击（大量客户端发起流式请求但不消费/不断开）的目标，生产环境需要在网关层配置合理的并发连接限制。

## 常见踩坑

| 现象 | 原因 | 解决 |
|---|---|---|
| 流式接口在 Nginx/网关后完全不流式，变成"卡住很久后一次性返回" | 反向代理默认开启了响应缓冲（buffering） | Nginx 需配置 `proxy_buffering off;` 等针对 SSE 的专门设置 |
| curl 测试时看不到流式效果，卡住后一次性输出 | curl 默认有输出缓冲 | 使用 `curl -N`（`--no-buffer`）参数 |
| 流式接口无法结合 `.entity()` 使用 | 官方当前限制，`stream()` 尚不直接支持结构化输出（第 16 章已提及） | 先聚合完整文本流再用 `BeanOutputConverter.convert()` 手动转换，或改用非流式 `.call().entity()` |
| WebFlux 应用里偶发阻塞异常 | 在响应式线程里调用了同步阻塞方法 | 参照 §17.4，确保全链路使用响应式 API 或显式线程池调度 |

## 版本差异

| 项 | 早期 | 本教程写法 |
|---|---|---|
| 流式 + 结构化输出 | 官方文档明确"未来会提供便利方法直接返回流式 Java 实体" | 截至本教程编写仍需手动聚合后转换，是一个持续关注的官方路线图项 |

## 为什么这样设计

Spring AI 选择让 `.stream()` 返回 Reactor 的 `Flux` 而不是自定义一套流式抽象，是"拥抱 Java 生态既有标准"的体现——`Flux` 是 Project Reactor（Spring 官方响应式编程库）的核心类型，选择它意味着开发者可以复用整个 Reactor 生态的操作符（`map`/`filter`/`doOnNext`/`onErrorResume` 等，本章 §17.5 都用到了）来处理流式数据，而不需要学习一套 AI 框架专属的流处理 API。这与本教程反复强调的"复用而非发明"设计哲学一致，也是为什么熟悉 Reactor（哪怕只是通过 Spring WebFlux 或 R2DBC 接触过）的开发者能非常快速地上手 Spring AI 的流式编程。

## FAQ

**Q：流式响应中途出错，前端能感知到吗？**
能，本章统一协议设计中 `.onErrorResume()` 捕获流中的异常并转换成 `error` 事件（而不是让连接直接断开且没有任何说明），前端应该对 `error` 事件类型做专门处理（如展示友好的错误提示而非"连接意外中断"这种模糊反馈）。

**Q：流式接口下 Advisor（第 06 章）的行为和非流式一致吗？**
基本一致，但要注意 `adviseStream` 的实现要用 Reactor 操作符而非同步阻塞逻辑（第 06 章思考题已经埋下这个伏笔），本章进一步印证了这一点在生产实现中的重要性。

**Q：SSE 和 WebSocket 该怎么选？**
SSE 是单向（服务端到客户端）、基于标准 HTTP 的协议，天然适合"用户提问、模型流式作答"这种单向为主的场景，且能穿透大多数网络环境（不需要协议升级）；WebSocket 是双向协议，适合需要客户端频繁主动推送的场景（如语音实时交互，第 02 章提到的 Voice Agent 场景）。本教程绝大多数问答类场景用 SSE 已经足够。

## 本章总结

本章讲清了流式输出从 API 用法到生产工程落地的完整链路：`.stream()` 返回 `Flux<String>`/`Flux<ChatResponse>` 两种粒度，Spring MVC 对响应式返回值的原生支持让流式接口的实现异常简洁，本仓库统一的 SSE 事件协议（`message`/`meta`/`error`/`done`）保证了跨 Demo/跨项目的前端对接一致性。流式与结构化输出结合的现实限制，是当前需要留意的官方能力边界。

## 延伸阅读

- Spring AI ChatClient 官方参考（Streaming 章节）：<https://docs.spring.io/spring-ai/reference/api/chatclient.html>
- Project Reactor 官方文档：<https://projectreactor.io/docs/core/release/reference/>

## 下一章预告

第 18 章进入 Observability：Micrometer 指标体系、`gen_ai.*` 语义化命名约定、Token/成本看板、Prometheus + Grafana 落地，以及如何安全地记录 Prompt/Completion 内容用于调试（默认关闭，按需开启）。

## 思考题

1. 本章统一 SSE 协议里 `meta` 事件（携带 Usage/Citation 等元信息）没有在代码示例中展开，你会把它插入在流的什么位置（开头/结尾/穿插）？为什么？
2. 如果一个 Agent（第 13 章）的推理过程涉及多轮工具调用，你会如何设计流式接口，让前端能实时展示"正在调用XX工具"这样的中间状态，而不只是最终文本？
3. 结合你之前做的 FastAPI SSE 经验，Spring MVC 基于 `Flux` 的流式实现和 FastAPI 的 `StreamingResponse`/`EventSourceResponse` 相比，在错误处理和背压（backpressure）管理上有什么异同？
