# examples —— 独立 Demo 工程目录

本目录在 **Phase 3** 交付 **48** 个完全独立的 Demo 工程，**v1.0 已全部落地**（UAT 48/48，见 `scripts/uat-phase3.sh`）。  
本 README 是 Demo 清单与规范的 SSOT：编号、命名与端口约定不得偏离；新增 Demo 须沿用下列规范。

## 1. 通用约定

- 每个 Demo = 独立 Maven 工程（`<parent>` 指向仓库父 POM，零版本号）+ 独立 README + `api.http`（IDEA/VS Code HTTP 文件）+ curl 示例 + Postman Collection + 运行结果说明与截图；
- 端口：`18000 + Demo 编号`（如 04 号 `chat-demo` → 18004）；
- 依赖中间件在各自 README 顶部声明所需 profile（如 `bash scripts/infra.sh up core vector`）；
- 模型 Key 统一环境变量注入：`AI_DASHSCOPE_API_KEY` / `DEEPSEEK_API_KEY`。

## 2. Demo 清单（48 个）

| # | 工程名 | 对应章节 | 演示要点 |
|---|---|---|---|
| 01 | quickstart-demo | 01/02 | 最小可运行 SAA 应用，验证环境与 Key |
| 02 | autoconfig-demo | 03 | 自动装配剖析、条件装配、属性绑定调试 |
| 03 | multi-model-demo | 04 | DashScope 与 DeepSeek 双 ChatModel 并存与显式选择 |
| 04 | chat-demo | 04 | ChatClient 全 API：Message/Role/ChatOptions/Usage |
| 05 | retry-demo | 04 | 重试、超时、错误处理策略 |
| 06 | prompt-demo | 05 | PromptTemplate、Few-shot、CoT、XML/JSON Prompt |
| 07 | prompt-builder-demo | 05 | Prompt 组装器与版本化管理 |
| 08 | prompt-nacos-demo | 05 | 基于 Nacos 的 Prompt 热更新 |
| 09 | advisor-demo | 06 | 内置 Advisor 链与顺序控制 |
| 10 | custom-advisor-demo | 06 | 自定义 Advisor（审计/脱敏/限流） |
| 11 | tool-demo | 07 | @Tool 定义、ToolContext、结果控制 returnDirect |
| 12 | dynamic-tool-demo | 07 | 运行时动态注册 Tool、异步 Tool |
| 13 | http-tool-demo | 07 | 外部 HTTP API 封装为 Tool |
| 14 | db-tool-demo | 07 | 数据库查询 Tool 与 SQL 安全防护 |
| 15 | tool-security-demo | 07 | Tool 权限控制与调用审计 |
| 16 | memory-demo | 08 | ChatMemory 新 API、滑动窗口 |
| 17 | redis-memory-demo | 08 | Redis 持久化会话记忆 |
| 18 | jdbc-memory-demo | 08 | JDBC 记忆仓库 + 会话管理接口 |
| 19 | summary-memory-demo | 08 | 摘要压缩长对话（长期/短期记忆） |
| 20 | structured-output-demo | 16 | Bean/Record/List/Map/枚举结构化输出 |
| 21 | json-schema-demo | 16 | JSON Schema 约束、嵌套泛型、校验与容错 |
| 22 | embedding-demo | 10 | EmbeddingModel、批量向量化、成本核算 |
| 23 | pgvector-demo | 11 | PGVector 存取、Metadata Filter |
| 24 | milvus-demo | 11 | Milvus 存取、索引与 Score |
| 25 | redis-vector-demo | 11 | Redis 向量检索与语义缓存 |
| 26 | es-hybrid-demo | 11 | Elasticsearch 全文 + 向量混合检索 |
| 27 | rag-demo | 09 | Naive RAG：ETL → 检索 → 生成 |
| 28 | advanced-rag-demo | 09 | 查询改写、Rerank、多路召回 |
| 29 | hybrid-rag-demo | 09 | 混合检索 RAG + Citation 溯源 |
| 30 | rag-eval-demo | 09 | RAG 评测（忠实度/相关性）与缓存降本 |
| 31 | mcp-server-demo | 12 | Spring 应用暴露 MCP Server（Streamable HTTP） |
| 32 | mcp-client-demo | 12 | MCP Client 消费多 Server 工具 |
| 33 | mcp-auth-demo | 12 | MCP 认证与权限控制 |
| 34 | mcp-nacos-demo | 12 | Nacos MCP Registry 分布式注册发现 |
| 35 | agent-demo | 13 | ReactAgent：Planning/循环/自纠错 |
| 36 | agent-skills-demo | 13 | Agent Skills 渐进式披露实战 |
| 37 | agent-hitl-demo | 13 | Human-in-the-Loop 人工确认 |
| 38 | workflow-demo | 14 | Graph：State/Node/Edge/Checkpoint |
| 39 | graph-parallel-demo | 14 | 并行条件边、AllOf/AnyOf 聚合、中断恢复 |
| 40 | graph-saga-demo | 14 | 失败补偿与 Saga 思想落地 |
| 41 | multi-agent-demo | 15 | Sequential/Parallel/Routing/Loop 四模式 |
| 42 | supervisor-demo | 15 | Supervisor + Handoffs + 并行子智能体 |
| 43 | a2a-nacos-demo | 15 | 跨进程 A2A 智能体互通 |
| 44 | stream-demo | 17 | SSE 流式输出、流式 + Tool、前端对接 |
| 45 | observability-demo | 18 | Micrometer + Prometheus + Grafana Token/成本看板 |
| 46 | logging-demo | 18 | AI 调用结构化日志与 TraceId 贯通 |
| 47 | routing-demo | 20 | 多模型智能路由与负载均衡 |
| 48 | fallback-demo | 20 | 模型降级、容灾与成本优化策略 |

## 3. Demo README 统一模板

```markdown
# <demo-name>

一句话说明。

## 前置条件
- 中间件：`bash scripts/infra.sh up <profiles>`（无需中间件则写"无"）
- 环境变量：AI_DASHSCOPE_API_KEY（按需列出）

## 运行
mvn spring-boot:run    # 端口 180xx

## 接口
（表格：方法 / 路径 / 说明）

## 快速验证
（curl 命令 + 关键 .http 片段 + 预期输出）

## 源码导读
（关键类 → 职责 → 对应教程章节小节）

## 运行结果
（终端/接口截图，存放于 images/examples/<demo-name>/）
```
