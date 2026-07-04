# Phase 3 Batch 2 Research — Demo 20~34

**Researched:** 2026-07-04  
**Scope:** RAG / Embedding / VectorStore / MCP（20~34）  
**Status:** Complete（orchestrator inline after researcher interrupt）

## Summary

次批 15 个 Demo 全部缺失，从教程「可运行 Demo」小节 + examples/README 演示要点新建。依赖坐标均在父 POM 的 `spring-ai-bom` / `spring-ai-alibaba-bom` / `spring-ai-alibaba-extensions-bom` 管理下，子模块零版本号。计划按能力域拆为 5 个 plan（03-04~03-08），wave 1 并行交付四个能力域，wave 2 编译门禁。

## Standard Stack

| Item | Value |
|------|-------|
| Parent | `com.flywhl.saa:spring-ai-alibaba-learning:1.0.0-SNAPSHOT` + `relativePath=../../pom.xml` |
| Common | `saa-learning-common` + `@Import(GlobalExceptionHandler.class)` + `Result<T>` |
| Model | `spring-ai-alibaba-starter-dashscope`，密钥 `${AI_DASHSCOPE_API_KEY}` |
| Embedding | 一律 DashScope `text-embedding-v4`，`dimensions: 1024`（ADR-003） |
| Port | `180NN`；34 Client = `18134` |
| Docs | README + `api.http`；中间件 profile 写 README 顶部 |

## Maven Artifacts（零版本号，BOM 管理）

| Demo | Key artifacts |
|------|---------------|
| 20/21 | `spring-ai-alibaba-starter-dashscope` + common（`.entity()` / `BeanOutputConverter` 内置于 ChatClient） |
| 22 | 同上（`EmbeddingModel` 由 DashScope starter 自动装配） |
| 23 | `spring-ai-starter-vector-store-pgvector` + `spring-boot-starter-jdbc` + DashScope |
| 24 | `spring-ai-starter-vector-store-milvus` + DashScope |
| 25 | `spring-ai-starter-vector-store-redis` + DashScope；**需 Redis Stack** |
| 26 | `spring-ai-starter-vector-store-elasticsearch` + DashScope |
| 27 | `spring-ai-advisors-vector-store`（`QuestionAnswerAdvisor`）+ milvus starter + DashScope |
| 28/29/30 | `spring-ai-advisors-vector-store`（`RetrievalAugmentationAdvisor`）+ milvus + DashScope |
| 31 | `spring-ai-starter-mcp-server-webmvc`（可无 DashScope，纯工具暴露） |
| 32 | `spring-ai-starter-mcp-client-webflux` 或 webmvc client + DashScope + 依赖本机 31 |
| 33 | mcp-server-webmvc + `TransportContextExtractor`（Bearer） |
| 34 | 双模块：server=`spring-ai-starter-mcp-server-webmvc` + SAA Nacos MCP；client=mcp client + `spring-ai-alibaba` nacos mcp + DashScope |

**PGVector 坐标：** 教程表未写 artifact，Spring AI 1.1.2 标准为 `spring-ai-starter-vector-store-pgvector`（与 milvus/redis/es 命名一致）。

**MCP Client 坐标：** 教程给配置形态；Starter 为 `spring-ai-starter-mcp-client-webmvc`（与 server-webmvc 对称）。若 BOM 仅有 webflux 变体，executor 以 BOM 实际坐标为准。

**Nacos MCP：** `com.alibaba.cloud.ai` 下 Nacos MCP Registry starter（extensions-bom），配置键见教程 §12.7 / 可运行 Demo：`spring.ai.alibaba.mcp.nacos.*`。

## Tutorial Spec Coverage

| Demo | Tutorial code completeness | Executor action |
|------|---------------------------|-----------------|
| 20 | 完整（DiagnosisResult + validateSchema + curl 18020） | 按教程落盘，包 `Result` |
| 21 | 无独立小节（README 要点：JSON Schema / 嵌套泛型 / 校验容错） | 基于 20 扩展：`ParameterizedTypeReference`、嵌套 Record、失败重试展示 |
| 22 | 完整（EmbeddingBenchmarkController + curl 18022） | 按教程；补成本字段（token/维度估算） |
| 23/24/26 | 教程给「profile 切换对比」设计，路径写 `vectorstore-compare-demo` | **拆成独立工程**（SSOT 是 examples/README），共享 Controller 模式：`add` + `search` + metadata filter |
| 25 | 教程仅选型表 + Redis Stack 警告 | 独立工程：add/search + 语义缓存 TTL 演示；自带 `docker-compose.override` 或 README 声明 `redis/redis-stack-server` |
| 27 | 点名 Naive RAG，无完整代码 | `QuestionAnswerAdvisor` + ETL seed endpoint + `/ask` |
| 28 | 点名查询改写+重排序 | `RetrievalAugmentationAdvisor` + `RewriteQueryTransformer` / `DocumentRanker`（教程 §9.4 API） |
| 29 | **完整** Hybrid RAG + Citation | 按教程，包 `Result` |
| 30 | 无完整代码（忠实度/相关性/缓存） | 最小评测：固定问答集 + 简单 faithfulness/relevance 打分接口 + 可选响应缓存 |
| 31 | 基础 Server 点名 | `@McpTool` 订单/天气工具，Streamable HTTP，端口 18031 |
| 32 | Client 消费多 Server | 配置连接 `http://localhost:18031/mcp`（及可选第二 mock），`SyncMcpToolCallbackProvider` |
| 33 | API 片段（TransportContextExtractor） | Server 校验 `Authorization: Bearer <token>`，无 token 拒绝 |
| 34 | **完整** 双模块 order-mcp-server(18034) + office-assistant-client(18134) | 目录 `examples/34-mcp-nacos-demo/{order-mcp-server,office-assistant-client}` |

## Infra Profiles

| Profile | Services | Demos |
|---------|----------|-------|
| （无） | — | 20, 21, 22, 31, 32, 33 |
| `core` | postgres(pgvector), redis(普通), mysql, minio | 23（postgres）；25 **不能**用 core 的 redis |
| `vector` | milvus:19530 (+etcd, minio) | 24, 27~30 |
| `search` | elasticsearch:9200 | 26 |
| `cloud` | nacos:8848 | 34 |

**Redis Stack 缺口（已知）：** `docker/docker-compose.yml` 的 `redis` 是 `redis:7.4-alpine`，缺 RediSearch/RedisJSON。25-redis-vector-demo 必须在 Demo 内提供 override（`redis/redis-stack-server`，端口可映射 6380 避免与 core 冲突）或 README 明确手动启动命令。与 17-redis-memory 首批决策一致（17 用普通 Redis 自定义 Repository；25 **必须** Stack）。

**连接凭据（dev）：** postgres `saa/saa123456@saa_learning:5432`；milvus `localhost:19530`；es `localhost:9200`；nacos `127.0.0.1:8848`。

## Package / Port Map

| NN | Package root | Port |
|----|--------------|------|
| 20 | `com.flywhl.saa.structured` | 18020 |
| 21 | `com.flywhl.saa.jsonschema` | 18021 |
| 22 | `com.flywhl.saa.embedding` | 18022 |
| 23 | `com.flywhl.saa.pgvector` | 18023 |
| 24 | `com.flywhl.saa.milvus` | 18024 |
| 25 | `com.flywhl.saa.redisvector` | 18025 |
| 26 | `com.flywhl.saa.eshybrid` | 18026 |
| 27 | `com.flywhl.saa.rag` | 18027 |
| 28 | `com.flywhl.saa.advancedrag` | 18028 |
| 29 | `com.flywhl.saa.hybridrag` | 18029 |
| 30 | `com.flywhl.saa.rageval` | 18030 |
| 31 | `com.flywhl.saa.mcpserver` | 18031 |
| 32 | `com.flywhl.saa.mcpclient` | 18032 |
| 33 | `com.flywhl.saa.mcpauth` | 18033 |
| 34 server | `com.flywhl.saa.mcpnacos.server` | 18034 |
| 34 client | `com.flywhl.saa.mcpnacos.client` | 18134 |

## Plan Grouping Recommendation

| Plan | Wave | Demos | Depends |
|------|------|-------|---------|
| 03-04 | 1 | 20, 21 | — |
| 03-05 | 1 | 22, 23, 24, 25, 26 | — |
| 03-06 | 1 | 27, 28, 29, 30 | —（逻辑依赖 22/24 API，但独立工程可并行写） |
| 03-07 | 1 | 31, 32, 33, 34 | —（32 真机 curl 依赖 31 运行，编译不依赖） |
| 03-08 | 2 | compile gate 20~34 | 03-04~07 |

**不覆盖** 03-01 / 03-02 / 03-03。

## Security Threats（须写入 PLAN `<threat_model>`）

| ID | Severity | Threat | Mitigation in demos |
|----|----------|--------|---------------------|
| T-20-01 | MED | 模型输出注入恶意 JSON 字段 | `validateSchema()` + Bean 强类型；不 `eval` 原始字符串 |
| T-23-01 | MED | Metadata filter 注入 | 仅用 `FilterExpressionBuilder`，禁止拼接用户输入为原生 filter 字符串 |
| T-27-01 | HIGH | 文档投毒 / prompt injection via RAG context | 系统提示约束「仅依据检索片段」；空上下文兜底不编造 |
| T-31-01 | HIGH | MCP 工具未鉴权被任意调用 | 33 演示 Bearer；31 教学可无鉴权但 README 标注生产必须鉴权 |
| T-32-01 | MED | Client 聚合过多工具导致越权面扩大 | `McpToolFilter` 或最小工具集；32 只连本机 31 |
| T-34-01 | MED | Nacos 明文/默认凭据 | 仅 dev 凭据；README 声明生产须改 |

## Risks & Landmines

1. **Redis Stack vs core redis** — 25 必踩坑，plan 必须含 override 或显式启动命令。
2. **Milvus 冷启动 30~60s** — 24/27~30 真机验证需等待健康检查。
3. **维度一致性** — `embedding-dimension` / `dimensions: 1024` 必须与 `text-embedding-v4` 一致，否则 pgvector/milvus 启动或检索失败。
4. **MCP import 包** — Spring AI 1.1.2 使用 `org.springframework.ai.tool.annotation.McpTool`（教程示例）；勿用 2.0 的 `org.springframework.ai.mcp.annotation`。
5. **禁用 API** — 勿用 `CallAroundAdvisor`；RAG 用 `QuestionAnswerAdvisor` / `RetrievalAugmentationAdvisor`（当前合法）。
6. **34 双模块** — 两个独立 Maven 工程在同一目录树下，各有 pom/parent。
7. **32 运行时依赖 31** — 编译门禁不启服务；README 写清启动顺序。

## Validation Architecture

### Automated (every plan)

```bash
mvn -pl common -am -q -DskipTests install
# per demo:
mvn -f examples/NN-xxx/pom.xml -q compile
```

### Smoke IT (D-18)

| Demo | IT focus | Guard |
|------|----------|-------|
| 20 | `.entity(DiagnosisResult.class)` 非空字段 | `@EnabledIfEnvironmentVariable(AI_DASHSCOPE_API_KEY)` |
| 22 | `EmbeddingModel.call` 返回非空向量 | 同上 |
| 27 | `/ask` 或 service 层 RAG 调用返回 content | 同上（可 `@DisabledIf` 无 milvus） |
| 31 | 应用 context 加载 + `@McpTool` bean 存在 | 可不调模型 |

中间件 Demo（23~26, 34）编译门禁即可；真机 curl 文档化。

### Manual

- curl 各 README「快速验证」
- 34：Nacos 控制台可见 `order-service-mcp`
- 25：确认连的是 Stack 而非 core redis

### Sampling

- 每完成一个 Demo：`mvn -f ... -q compile`
- Wave 结束：03-08 批量 compile 20~34
- 不要求本批 version-audit 全仓（Phase 3 收口）

## RESEARCH COMPLETE

- Artifacts: vector-store-{pgvector,milvus,redis,elasticsearch}；mcp-server-webmvc；mcp-client；advisors-vector-store；dashscope
- Plans: 03-04~08，wave1 四域并行 + wave2 门禁
- Top risks: Redis Stack、维度对齐、MCP 鉴权教学边界、34 双模块端口
