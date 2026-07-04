# Decisions (ADR)

Synthesized from locked ADR source. All entries status=locked (Accepted, 2026-07-03).
source: docs/00-overview/04-技术选型ADR.md

---

## ADR-001：AI 应用框架选 Spring AI Alibaba 1.1.2.2

- **status:** locked
- **source:** docs/00-overview/04-技术选型ADR.md
- **scope:** AI application framework selection
- **decision:** 选 SAA 1.1.2.2（官方最新稳定版；1.1.2.1 因缺陷被官方撤回推荐）。其"Spring AI 超集"性质意味着教程前半部（ChatClient/Advisor/RAG/MCP）同时就是标准 Spring AI 教学，后半部（Agent/Graph/Nacos 系）是差异化增值。
- **consequences:** 绑定 SAA 发版节奏（其对 Spring AI 的对齐存在 1~2 个补丁版滞后）；换来多智能体与企业云原生能力开箱即用。

---

## ADR-002：Spring Boot 锁定 3.5.16（而非 4.x）

- **status:** locked
- **source:** docs/00-overview/04-技术选型ADR.md
- **scope:** Spring Boot / Spring AI major version line
- **decision:** 选 Boot 3.5.16 + Spring AI 1.1.x + SAA 1.1.2.2。"官方兼容矩阵内的全 GA 组合"优先级高于"Boot 支持期"。配套：第 22 章提供 Spring AI 2.0 / Boot 4 迁移准备清单；所有代码规避已知 2.0 破坏点（Options Builder、新 Memory API）。
- **consequences:** 版本组合稳定、教程写法唯一；若 3.5 线曝出高危 CVE 需评估临时缓解或商业支持；SAA 发布对齐 Spring AI 2.0 的 GA 版本时需重访。

---

## ADR-003：模型策略 = DashScope 主通道 + DeepSeek 直连副通道

- **status:** locked
- **source:** docs/00-overview/04-技术选型ADR.md
- **scope:** model providers and routing
- **decision:**
  - 主通道：`spring-ai-alibaba-starter-dashscope`（qwen 对话系列 + text-embedding 系列 + 多模态），Embedding 一律走 DashScope 保证向量空间一致；
  - 副通道：`spring-ai-starter-model-deepseek` 直连 DeepSeek 官方 API（OpenAI 兼容），作为切换/降级/成本优化的第二模型来源；
  - 演示第三通道：OpenAI 兼容 base-url 方式再接百炼托管的 deepseek 模型；
  - 路由/降级在应用层实现（第 20 章），基于 Spring AI `ChatModel` 抽象，不绑定任何单一 SDK。
  - 禁止 Ollama/本地模型/GPU。
- **consequences:** 所有 Demo 无需 GPU；离线环境无法运行模型相关 Demo（Testcontainers 与纯工程测试不受影响）。

---

## ADR-004：向量库主 Milvus、轻量 pgvector、缓存层 Redis

- **status:** locked
- **source:** docs/00-overview/04-技术选型ADR.md
- **scope:** vector stores and cache per project
- **decision:**
  - 教学：Milvus / Redis / PGVector / Elasticsearch 四种全部覆盖（第 11 章），统一走 Spring AI `VectorStore` 抽象；
  - 项目一（知识库）：主库 **Milvus**；
  - 项目二（办公助手）：**pgvector**；
  - 项目三（客服）：Milvus + **Redis** 语义缓存层；ES 用于混合检索章节与客服全文检索。
- **consequences:** docker-compose 需以 profile 管理 6+ 中间件；换库成本被抽象层压到配置级。

---

## ADR-005：Maven 多模块 + 四 BOM 叠加导入

- **status:** locked
- **source:** docs/00-overview/04-技术选型ADR.md
- **scope:** build system and dependency management
- **decision:** 单仓多模块；父 POM 依序导入 `spring-boot-dependencies` → `spring-ai-bom` → `spring-ai-alibaba-bom` → `spring-ai-alibaba-extensions-bom`；不使用 `spring-boot-starter-parent` 作为 parent；子模块与所有 Demo/项目**禁止出现任何版本号**。禁止 Gradle。
- **consequences:** 升级任何组件 = 改父 POM 一处 + 跑全量构建；BOM import 顺序即冲突仲裁顺序。

---

## ADR-006：API 文档采用 SpringDoc OpenAPI + Knife4j 增强 UI

- **status:** locked
- **source:** docs/00-overview/04-技术选型ADR.md
- **scope:** API documentation stack
- **decision:** 全部 Web 工程引 SpringDoc（协议层）+ Knife4j（UI 层），文档地址统一 `/doc.html`；接口注解规范在 common 章节给出模板。
- **consequences:** 调试体验统一；若 Knife4j 对 Boot 3.5 出现兼容问题，退化为 SpringDoc 原生 swagger-ui（仅 UI 层替换）。
