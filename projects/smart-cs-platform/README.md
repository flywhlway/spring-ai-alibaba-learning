# 项目三 · 智能客服 Agent 平台（smart-cs-platform）

> **Phase 6 企业项目** · 端口 **19300** · 蓝图 SSOT：[`projects/README.md`](../README.md)「项目三」
>
> **当前状态**：✅ **v1.0 / Phase 6 已交付**——可 `mvn spring-boot:run`；HUMAN-UAT 5/5 + REVIEW-FIX；脚本见 `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`。

---

## 1. 业务场景

多渠道客服中台——FAQ 秒答、复杂问题多智能体协作、工单创建流转、人工接管、运营看板：

- **客户**：SSE 流式对话，标准问题（政策/流程/FAQ）秒回，复杂业务（订单/售后/技术）由多智能体协作处理，可随时要求转人工；
- **坐席**：接手 `PENDING_HUMAN` 会话，审批/继续处理工单，工单全生命周期留痕；
- **管理员**：模型/Prompt 配置化管理（Nacos 热更新）、FAQ 知识维护、运营看板（问答量/成本/缓存命中率/工单分布）。

```mermaid
flowchart TB
    C[客户/坐席] -->|SSE /api/chat/stream| GW[会话网关]
    GW --> ROUTER[LlmRoutingAgent 意图路由]
    ROUTER -->|FAQ| FAQ[FAQ 链路]
    ROUTER -->|业务| SUP[Supervisor ReactAgent]
    ROUTER -->|工单| TKT[Ticket Agent + TicketTools]
    ROUTER -->|升级| HITL[HumanInTheLoopHook]

    FAQ --> CACHE[(Redis Stack 6380 语义缓存)]
    FAQ -->|miss| HYB[HybridSearch Milvus+ES RRF]
    HYB --> MV[(Milvus scs_faq)]
    HYB --> ES[(Elasticsearch scs-faq)]
    HYB --> RAG[RetrievalAugmentationAdvisor]
    RAG --> CACHE

    SUP --> OA[订单 Agent]
    SUP --> AA[售后 Agent]
    SUP --> TA[技术 Agent]

    TKT --> PG[(PostgreSQL cs_ticket)]
    HITL --> PG
    GW --> MEM[(Redis 6379 ChatMemory)]

    OPS[运营 ADMIN] --> ADM[admin: 模型/Prompt/看板]
    ADM --> NA[(Nacos)]
    ADM --> PG
    PROM[Prometheus] -->|scrape| ACT[/actuator/prometheus]
```

## 2. 技术栈与落点

| 需求项 | 技术落点 | 代码位置 |
|---|---|---|
| 多 Agent + 意图路由 | `LlmRoutingAgent` + `ReactAgent`(Supervisor) + `ParallelAgent` | `agent/CsAgentConfig` |
| Handoffs / 人工升级 | `@Tool requestHumanHandoff` + 工单状态机 + `HumanInTheLoopHook` | `tool/HandoffTools`、`service/TicketService` |
| FAQ 语义缓存 | Redis Stack VectorStore（阈值 0.95） | `service/SemanticCacheService` |
| Milvus + ES 混合检索 | `HybridSearchService`（应用层 RRF k=60） | `rag/HybridSearchService` |
| RAG 生成 | `RetrievalAugmentationAdvisor` | `rag/RagPipelineFactory` |
| 工单 / 状态机 | `cs_ticket` + `cs_ticket_event` 审计轨迹 | `service/TicketService` |
| 模型/Prompt 后台 | `model_profile` CRUD + Nacos 推送 | `admin/*`、`prompt/*` |
| 权限/审计 | Spring Security（JWT 资源服务器）+ `audit_log` | `config/SecurityConfig` |
| 可观测/成本 | Actuator + Prometheus + starter `CostRecorder` | `application.yml` `saa.learning.*` |
| API 文档 | Knife4j（`/doc.html`） | `config/OpenApiConfig` |

**复用底座**：`saa-learning-common`（`Result`/`PageResult`/`BizException`/全局异常处理器）+ `saa-learning-starter`（审计 Advisor / 模型路由降级 / 成本采集），不重复造轮子。

## 3. 目录结构

```
smart-cs-platform/
├── pom.xml                        # parent 指向仓库父 POM，零版本号
├── README.md                      # 本文件
├── docker-compose.override.yml    # smartcs profile：scs-db-init + scs-redis-stack(6380)
├── db/
│   ├── schema.sql                 # PostgreSQL DDL（SSOT，JPA ddl-auto=none）
│   └── data.sql                   # 演示数据（账号/FAQ/历史工单）
├── http/
│   └── api.http                   # 全接口 REST Client 文件（Auth/Chat/Ticket/Handoff/Admin）
└── src/main/java/com/flywhl/saa/smartcs/
    ├── SmartCsApplication.java
    ├── config/         # Security / OpenAPI / AI 装配 / VectorStore×3 / Memory / Nacos / Observability / ScsProperties
    ├── controller/     # Auth / Chat(SSE) / Ticket / HumanHandoff
    ├── service/        # Auth / Chat / Ticket / FaqAnswer / SemanticCache / CsOrchestrator / ModelAdmin
    ├── agent/           # CsAgentConfig：LlmRoutingAgent + Supervisor + 子 Agent + HITL hooks
    ├── tool/            # OrderTool / TicketTools / HandoffTools（@Tool + 权限校验）
    ├── rag/             # FaqEtlPipeline / RagPipelineFactory / HybridSearchService
    ├── prompt/          # PromptTemplateProvider / PromptPublishService
    ├── admin/           # 后台域：模型/Prompt/看板/审计/FAQ 管理
    ├── model/           # entity（11 表）· dto · vo
    ├── mapper/          # MapStruct Converter
    └── repository/      # Spring Data JPA
```

## 4. 快速开始

### 4.1 前置条件

- Java 21、Maven、Docker（OrbStack）
- 密钥（环境变量注入，严禁硬编码）：

```bash
source scripts/setup-env.local.sh && bash scripts/env-check.sh
# 需要：AI_DASHSCOPE_API_KEY（必须）、DEEPSEEK_API_KEY（备用通道）
```

### 4.2 启动中间件（仓库根目录执行）

```bash
docker compose -f docker/docker-compose.yml \
               -f projects/smart-cs-platform/docker-compose.override.yml \
               --profile core --profile vector --profile search --profile cloud --profile smartcs up -d
```

拉起：Redis(6379) / PostgreSQL / Milvus(+etcd) / Elasticsearch / Nacos，并叠加本项目 `scs-db-init`（建库 `scs_platform` + 导入 DDL/演示数据）与 `scs-redis-stack`（6380，语义缓存专用）。**Milvus 冷启动约 30~60s**，等 `docker compose ps` 全部 healthy 再启动应用。

**排障（冷启动）：**
- override 内 db volume 相对首文件目录 `docker/`，已写为 `../projects/smart-cs-platform/db`；若曾用错误 `./db` 挂载起过 init，请 `--force-recreate scs-db-init`。
- 已有 `scs_platform` 且 `query-rewrite` Prompt 缺 `{target}`：重跑 init（`data.sql` 含幂等 UPDATE），或 `DROP DATABASE scs_platform` 后重建。仅改 classpath `.st` 不够——DB 中 PUBLISHED 模板优先。
- 健康检查：`curl http://localhost:19300/actuator/health` 期望 UP。

可选监控栈（Prometheus + Grafana）：

```bash
docker compose -f docker/docker-compose.yml \
               -f projects/smart-cs-platform/docker-compose.override.yml \
               --profile monitor up -d
```

### 4.3 编译与运行

```bash
mvn -f projects/smart-cs-platform/pom.xml clean package
mvn -f projects/smart-cs-platform/pom.xml spring-boot:run
```

### 4.4 验证

```bash
curl http://localhost:19300/actuator/health
# 登录（演示账号见下）
curl -X POST http://localhost:19300/api/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"username":"customer1","password":"customer123"}'
# 全接口见 http/api.http；在线文档 http://localhost:19300/doc.html
```

## 5. 演示账号

| 账号 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | 模型/Prompt/看板后台全部功能 |
| `agent1` | `agent123` | AGENT | 坐席：接手 HITL 会话、处理工单 |
| `customer1` | `customer123` | CUSTOMER | 客户：对话、建单、催单 |
| `customer2` | `customer123` | CUSTOMER | 客户：对话、建单、催单 |

> 演示口令在 `db/data.sql` 中以 `{noop}` 前缀存储（DelegatingPasswordEncoder），仅限本机；生产用户一律 BCrypt。

## 6. 接口总览

统一协议：同步接口返回 `Result<T>`（`code=0` 成功）；分页 `Result<PageResult<T>>`；流式接口 `text/event-stream`。

| 域 | 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|---|
| 认证 | POST | `/api/auth/login` | 匿名 | 登录签发 JWT |
| 认证 | GET | `/api/auth/me` | 登录 | 当前用户信息 |
| 会话 | POST | `/api/chat/stream` | CUSTOMER/AGENT/ADMIN | SSE 流式对话（JSON body，意图路由） |
| 会话 | POST | `/api/chat/ask` | CUSTOMER/AGENT/ADMIN | 同步问答 |
| 工单 | POST | `/api/tickets` | CUSTOMER | 创建工单 |
| 工单 | GET | `/api/tickets/{ticketNo}` | CUSTOMER/AGENT/ADMIN | 工单详情 |
| 工单 | GET | `/api/tickets` | AGENT/ADMIN | 工单分页（状态过滤） |
| 人工接管 | POST | `/api/handoff/start` | CUSTOMER/AGENT | 触发人工升级（HITL interrupt） |
| 人工接管 | POST | `/api/handoff/approve` | AGENT | 坐席审批恢复（HITL resume） |
| 后台-模型 | GET/POST | `/api/admin/model-profiles` | ADMIN | 模型配置 CRUD + Nacos 推送 |
| 后台-Prompt | GET/POST | `/api/admin/prompts` | ADMIN | 模板版本列表/新建/发布 |
| 后台-FAQ | GET/POST | `/api/admin/faq` | ADMIN | FAQ 维护（触发 ETL 索引） |
| 后台-看板 | GET | `/api/admin/dashboard/stats` | ADMIN | 会话量/成本/缓存命中率/工单分布 |
| 运维 | GET | `/actuator/health` `/actuator/prometheus` | 匿名/内网 | 健康与指标 |

## 7. 监控（Prometheus / Grafana）

应用已暴露 Micrometer Prometheus 端点：`http://localhost:19300/actuator/prometheus`（Security 匿名放行）。

### 7.1 启动 monitor profile

```bash
docker compose -f docker/docker-compose.yml \
               -f projects/smart-cs-platform/docker-compose.override.yml \
               --profile monitor up -d
```

| 服务 | 镜像（pin LTS） | 端口 | 说明 |
|---|---|---|---|
| `scs-prometheus` | `prom/prometheus:v2.55.1` | 9090 | scrape `host.docker.internal:19300/actuator/prometheus` |
| `scs-grafana` | `grafana/grafana:11.2.0` | 3000 | 默认账号 `admin` / `admin` |

配置文件：`projects/smart-cs-platform/monitor/prometheus.yml`。

### 7.2 Grafana 添加 Prometheus 数据源

1. 打开 http://localhost:3000 ，登录后进入 **Connections → Data sources → Add data source → Prometheus**
2. **Prometheus server URL** 填：`http://scs-prometheus:9090`（同一 Docker 网络内服务名）
3. Save & test 应显示绿色成功

### 7.3 示例面板指标名

| 面板意图 | PromQL / 指标名 |
|---|---|
| Token 用量（成本相关） | `gen_ai_client_token_usage_total`（对应代码 `gen_ai.client.token.usage`） |
| JVM 内存 | `jvm_memory_used_bytes` |
| HTTP 请求耗时 | `http_server_requests_seconds_count` |
| 进程 CPU | `process_cpu_usage` |

运营看板聚合 API（ADMIN）：`GET /api/admin/dashboard/stats?days=7` 返回会话数、消息数、工单按状态分布、FAQ 缓存命中率、`route_agent` 分布与 token 成本（优先 Micrometer，DB 回退）。

## 8. 数据与存储

| 存储 | 用途 | 初始化 |
|---|---|---|
| PostgreSQL `scs_platform` | 用户/FAQ元数据/会话/消息/工单/工单事件/Prompt版本/模型配置/审计/反馈（11 表） | `db/schema.sql` + `db/data.sql`（compose 自动执行，幂等） |
| Milvus `scs_faq` | FAQ Chunk 向量（1024 维，COSINE） | 应用启动 `initialize-schema: true` |
| Elasticsearch `scs-faq` | FAQ 全文 + 向量双通道（应用层 RRF 混合） | 应用启动 `initialize-schema: true` |
| Redis Stack `6380` | FAQ 语义缓存（阈值 0.95，TTL） | compose `scs-redis-stack` |
| Redis `6379` | 会话记忆（滚动窗口 + TTL 7d） | 无需初始化 |
| Nacos | Prompt 热更新 + `model_profile` 配置推送 | 后台发布接口自动推送 |

## 9. 测试与部署

- **单元测试**：JUnit 5 + AssertJ；**集成测试**：PostgreSQL/Redis/ES 走 Testcontainers，真实模型用例以 `AI_DASHSCOPE_API_KEY` 环境变量门控，无 Key 环境 `mvn clean install` 保持全绿。
- **部署**：`mvn clean package` 产出 `target/smart-cs-platform.jar`；生产以 `java -jar` + 上述 compose 编排（替换演示口令、Nacos 鉴权与 JWT Secret）。

## 10. 交付对照（v1.0 已完成）

| Wave | 内容 | 状态 |
|---|---|---|
| 1 | `config/*`：Security / Milvus / ES / Redis / Nacos / AI 装配 | ✅ |
| 2 | FAQ ETL、混合检索、RAG、语义缓存 | ✅ |
| 3 | `LlmRoutingAgent` + Supervisor + 子 Agent + HITL | ✅ |
| 4 | 会话 SSE、工单状态机、人工接管 | ✅ |
| 5 | admin：模型/Prompt/FAQ/看板 | ✅ |
| 6 | 测试与质量门禁脚本 | ✅ |

已知限制：HITL pending 为进程内 Map（演示级，多实例不共享）——见 `.planning/STATE.md` Deferred Items。
