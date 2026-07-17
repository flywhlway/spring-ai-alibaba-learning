# spring-ai-alibaba-learning

Spring AI Alibaba **企业级实战教程** + 完整可运行源码仓库 + 三个真实企业项目。

面向已具备 Spring Boot / Spring Cloud / Docker / LLM / RAG / Agent 经验的高级开发者，基于 **2026 年当前最新稳定生态** 构建：

| 组件 | 版本 |
|---|---|
| JDK | 21 |
| Spring Boot | 3.5.16 |
| Spring AI | 1.1.2（1.1 线，可升 1.1.8） |
| Spring AI Alibaba | **1.1.2.2**（Agent Framework + Graph，官方最新稳定版） |
| SAA Extensions | 1.1.2.2（DashScope 等，随主线统一版本） |
| 模型 | DashScope（百炼）+ DeepSeek（全云端 API，无本地模型） |
| 中间件 | Redis · PostgreSQL(pgvector) · MySQL · MinIO · Milvus · Kafka · RabbitMQ · ES · Nacos（OrbStack Docker Compose） |

版本选型依据见 [docs/00-overview/02-版本调研报告.md](docs/00-overview/02-版本调研报告.md)。

---

## 快速开始

```bash
# 1. 克隆
git clone <your-repo-url> && cd spring-ai-alibaba-learning

# 2. 环境自检（JDK 21 / Maven / OrbStack / API Key / 端口）
bash scripts/env-check.sh

# 3. 配置模型 API Key（模板复制后填入真实 Key，该文件不入库）
cp scripts/setup-env.example.sh scripts/setup-env.local.sh
vi scripts/setup-env.local.sh
source scripts/setup-env.local.sh

# 4. 启动核心中间件（Redis / PG / MySQL / MinIO）
bash scripts/infra.sh up core

# 5. 构建全仓库
mvn clean install
```

跑 RAG 相关内容时追加向量库：`bash scripts/infra.sh up core vector`。

质量门禁（本地 / CI 共用）：`bash scripts/quality-gate.sh`。  
生产化与运维（CI、Compose 部署、排障）：[docs/00-overview/05-生产化与运维.md](docs/00-overview/05-生产化与运维.md)。

---

## 仓库结构

```
docs/       教程本体（00-overview 总览 + tutorial 01~22 章教材级教程）
common/     公共模块：统一 Result / 错误码 / 异常 / 全局异常处理器
starter/    统一 AI Starter（第 19 章企业实践落地）
examples/   40~60 个独立可运行 Demo（每个含 README / .http / curl）
projects/   三个企业级完整项目（知识库问答 / AI 办公助手 / 智能客服平台）
scripts/    环境自检 · API Key 模板 · 中间件生命周期脚本
docker/     统一 docker-compose（profile 分组：core / vector / mq / search / cloud）
images/     文档截图与静态资源
```

## 学习入口

1. [学习路线总览](docs/00-overview/01-学习路线.md) —— 含 LangGraph → SAA 概念映射表
2. [版本调研报告](docs/00-overview/02-版本调研报告.md)
3. [总体架构与目录规划](docs/00-overview/03-总体架构与目录规划.md)
4. [技术选型 ADR](docs/00-overview/04-技术选型ADR.md)
5. [生产化与运维](docs/00-overview/05-生产化与运维.md) —— CI / quality-gate / Compose 部署
6. [教程目录（docs/README）](docs/README.md)

## 交付阶段

| Phase | 内容 | 状态 |
|---|---|---|
| 1 | 学习路线 · 版本调研 · 总体架构 · 目录规划 · 技术选型 · 父工程与 common 初始化 · 中间件基座 | ✅ |
| 2 | docs/tutorial 01~22 章教材级教程 · starter 模块落地 · 全量 QA 勘误 | ✅ 已交付（22/22 章，见下表） |
| 3 | 40~60 个独立 Demo 工程 | ⏳ |
| 4 | 企业项目一：AI 企业知识库问答平台 | ⏳ |
| 5 | 企业项目二：企业 AI Agent 办公助手 | ⏳ |
| 6 | 企业项目三：智能客服 Agent 平台 | ⏳ |
| 7 | 统一测试 · CI/CD · 部署 · 调优 · 排障 · 总览 | ⏳ |

## 约定

- 所有工程 `git clone → docker compose up → mvn clean install → mvn spring-boot:run` 直接可跑；
- 源码零 TODO、零伪代码、零"请自行补充"；
- 全部图示 Mermaid，可直接渲染；
- API Key 仅通过环境变量注入，严禁提交任何密钥。

### Phase 2 章节交付进度

| 章 | 标题 | 状态 |
|---|---|---|
| 01 | 为什么需要 Spring AI Alibaba | ✅ |
| 02 | 整体架构 | ✅ |
| 03 | AutoConfiguration | ✅ |
| 04 | ChatClient | ✅ |
| 05 | Prompt 工程与 Nacos 热更新 | ✅ |
| 06 | Advisor 链 | ✅ |
| 07 | Tool Calling 全景 | ✅ |
| 08 | Memory 会话记忆 | ✅ |
| 09 | RAG 检索增强生成全链路 | ✅ |
| 10 | Embedding 向量化 | ✅ |
| 11 | VectorStore 向量存储 | ✅ |
| 12 | MCP 模型上下文协议 | ✅ |
| 13 | Agent 智能体开发 | ✅ |
| 14 | Workflow 图编排运行时 | ✅ |
| 15 | MultiAgent 多智能体协作 | ✅ |
| 16 | StructuredOutput 结构化输出 | ✅ |
| 17 | Streaming 流式输出 | ✅ |
| 18 | Observability 可观测体系 | ✅ |
| 19 | BestPractice 统一企业实践（starter 模块落地） | ✅ |
| 20 | 企业实践（路由/治理/成本/安全） | ✅ |
| 21 | 版本升级指南 1.0.x→1.1.2.2 | ✅ |
| 22 | Spring AI 2.0 现状与迁移前瞻 | ✅ |
