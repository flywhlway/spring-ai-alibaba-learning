# 技术选型决策记录（ADR）

> 记录 Phase 1 锁定的关键架构决策。状态均为 **Accepted**，决策日期 2026-07-03。后续阶段如需变更，新增 ADR 标注 Supersedes，不直接改写历史。

---

## ADR-001：AI 应用框架选 Spring AI Alibaba 1.1.2.2

**Context**：Java 技术栈下构建企业 AI 应用，候选：纯 Spring AI、Spring AI Alibaba（SAA）、LangChain4j；团队已有 LangGraph（Python）经验与阿里系中间件（Nacos）使用背景。

| 维度 | 纯 Spring AI 1.1.x | **SAA 1.1.2.2** | LangChain4j |
|---|---|---|---|
| 底层抽象（Chat/RAG/Tool/MCP） | ✅ 原生 | ✅ 完全继承 Spring AI | ✅ 自成体系 |
| 多智能体/工作流 | ✗ 无官方编排框架 | ✅ Agent Framework + Graph（对标 LangGraph） | △ 社区方案 |
| 国内模型与百炼集成 | △ 需自配 OpenAI 兼容 | ✅ DashScope 一等公民 | △ |
| 企业云原生（Nacos 配置/Prompt 热更新、分布式 MCP Registry、A2A） | ✗ | ✅ 差异化核心 | ✗ |
| 平台化（Admin/Studio/评测） | ✗ | ✅ | ✗ |
| 生态兼容 | — | ✅ 兼容全部 Spring AI API | ✗ 与 Spring AI 不互通 |

**Decision**：选 SAA 1.1.2.2（官方最新稳定版；1.1.2.1 因缺陷被官方撤回推荐，见版本调研报告 §2.1）。其"Spring AI 超集"性质意味着教程前半部（ChatClient/Advisor/RAG/MCP）同时就是标准 Spring AI 教学，后半部（Agent/Graph/Nacos 系）是差异化增值。

**Consequences**：绑定 SAA 发版节奏（其对 Spring AI 的对齐存在 1~2 个补丁版滞后）；换来多智能体与企业云原生能力开箱即用。

---

## ADR-002：Spring Boot 锁定 3.5.16（而非 4.x）

**Context**：用户硬约束"Boot 3.x 最新稳定版"；调研发现 3.5 线 OSS 支持已于 2026-06-30 到期，而 SAA 1.1.2.x 官方兼容矩阵仅覆盖 Boot 3.5.x。**二次核验更正**：Spring AI 2.0.0 已于 2026-06-12 GA（并非早期调研记录的 Milestone 状态），但 SAA 官方**尚未发布任何对齐 Spring AI 2.0 / Boot 4 的版本**——这不是"新版本不够成熟"的问题，而是"选择 SAA 这个框架就必然停留在 Boot 3.5.x 线"这一当下无法绕开的客观约束（详见第 22 章）。

**Options**：
- A. Boot 3.5.16 + Spring AI 1.1.x + SAA 1.1.2.2 —— 全 GA、官方互相对齐、SAA 全部差异化能力（Agent Framework/Graph/Nacos 系）可用；代价：3.5 线不再有 OSS 补丁。
- B. Boot 4.x + Spring AI 2.0.0 GA —— 支持期内、API 已稳定；**代价：SAA 无任何对齐版本，选 B 等于放弃 SAA 全部差异化能力，退化为"只用裸 Spring AI 官方能力"**，直接违背用户"需要 Agent/多智能体/企业云原生"的核心诉求。

**Decision**：选 A。"官方兼容矩阵内的全 GA 组合"优先级高于"Boot 支持期"。配套措施：第 22 章提供 Spring AI 2.0 / Boot 4 迁移准备清单；所有代码规避已知 2.0 破坏点（如统一使用 Options Builder、新 Memory API），降低未来迁移成本。

**Consequences**：变得容易——版本组合稳定、教程写法唯一；变得困难——若 3.5 线曝出高危 CVE 需评估临时缓解或商业支持；需要重访——SAA 发布对齐 Spring AI 2.0 的 GA 版本时。

---

## ADR-003：模型策略 = DashScope 主通道 + DeepSeek 直连副通道

**Context**：约束为全云端 API（DashScope + DeepSeek），禁止 Ollama/本地模型/GPU；教程需覆盖多模型切换、降级、容灾、成本路由。

**Decision**：
- 主通道：`spring-ai-alibaba-starter-dashscope`（qwen 对话系列 + text-embedding 系列 + 多模态），Embedding 一律走 DashScope 保证向量空间一致；
- 副通道：`spring-ai-starter-model-deepseek` 直连 DeepSeek 官方 API（OpenAI 兼容），作为切换/降级/成本优化的第二模型来源；
- 演示第三通道：OpenAI 兼容 base-url 方式再接百炼托管的 deepseek 模型，讲清"同一模型的两种接入路径"与网关兜底模式；
- 路由/降级在应用层实现（第 20 章），基于 Spring AI `ChatModel` 抽象做统一封装，不绑定任何单一 SDK。

**Consequences**：所有 Demo 无需 GPU、48GB M5 Pro 内存全部留给中间件；模型账单可控（教程将给出各 Demo 的预估 token 消耗）；离线环境无法运行模型相关 Demo（Testcontainers 测试与纯工程测试不受影响）。

---

## ADR-004：向量库主 Milvus、轻量 pgvector、缓存层 Redis

**Context**：需覆盖 Milvus / Redis / PGVector / Elasticsearch 四类 VectorStore 教学，且企业项目要选定主力方案。

**Decision**：
- 教学：四种全部覆盖（第 11 章），统一走 Spring AI `VectorStore` 抽象，演示"换库只改依赖与配置"；
- 项目一（知识库）：主库 **Milvus**（大规模、Hybrid Search、生产代表性强）；
- 项目二（办公助手）：**pgvector**（业务库与向量同库，运维最简，适合中小规模）；
- 项目三（客服）：Milvus + **Redis** 语义缓存层（FAQ 高频命中降成本）；ES 用于混合检索章节与客服全文检索。

**Consequences**：docker-compose 需以 profile 管理 6+ 中间件避免内存挤兑（已落地）；换库成本被抽象层压到配置级。

---

## ADR-005：Maven 多模块 + 四 BOM 叠加导入

**Context**：禁止 Gradle；仓库含 40~60 Demo + 3 项目，版本漂移风险极高。

**Decision**：单仓多模块；父 POM 依序导入 `spring-boot-dependencies` → `spring-ai-bom` → `spring-ai-alibaba-bom` → `spring-ai-alibaba-extensions-bom`；不使用 `spring-boot-starter-parent` 作为 parent（保留自定义父 POM 的继承位），Boot 插件与编译链在父 POM pluginManagement 统一。子模块与所有 Demo/项目**禁止出现任何版本号**。

**Consequences**：升级任何组件 = 改父 POM 一处 + 跑全量构建；BOM import 顺序即冲突仲裁顺序（先导入者优先），教程第 3 章将用依赖树实例讲解。

---

## ADR-006：API 文档采用 SpringDoc OpenAPI + Knife4j 增强 UI

**Context**：要求 OpenAPI 且"Knife4j 如适配"。Knife4j 4.5.0 的 `knife4j-openapi3-jakarta-spring-boot-starter` 基于 SpringDoc，支持 Boot 3 / Jakarta。

**Decision**：全部 Web 工程引 SpringDoc（协议层）+ Knife4j（UI 层），文档地址统一 `/doc.html`；接口注解规范在 common 章节给出模板。

**Consequences**：调试体验统一；若 Knife4j 对 Boot 3.5 出现兼容问题，退化为 SpringDoc 原生 swagger-ui 仅是 UI 层替换，协议层不受影响（已验证退路）。
