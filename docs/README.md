# docs —— 教程总目录

## 0. 总览文档（Phase 1，已交付）

| 文档 | 内容 |
|---|---|
| [00-overview/01-学习路线.md](00-overview/01-学习路线.md) | 总路线图、LangGraph→SAA 概念映射、章节/Demo/项目覆盖矩阵、学习节奏 |
| [00-overview/02-版本调研报告.md](00-overview/02-版本调研报告.md) | 版本 SSOT：SAA 1.1.2.2 / Spring AI 1.1.x / Boot 3.5.16 调研与决策依据 |
| [00-overview/03-总体架构与目录规划.md](00-overview/03-总体架构与目录规划.md) | 仓库结构、模块依赖、运行时架构、SSOT 清单、编码/接口/测试规范 |
| [00-overview/04-技术选型ADR.md](00-overview/04-技术选型ADR.md) | ADR-001~006：框架/Boot 版本/模型策略/向量库/构建/文档六大决策 |

## 1. 教程正文（tutorial/，Phase 2 交付，22 章）

| 章 | 文件 | 主题 |
|---|---|---|
| 01 | 01-为什么需要SpringAIAlibaba.md | 定位、选型对比、1.0→1.1 演进 |
| 02 | 02-整体架构.md | Agent Framework / Graph / Extensions / Admin 模块地图 |
| 03 | 03-AutoConfiguration.md | 自动装配原理与源码分析 |
| 04 | 04-ChatClient.md | ChatClient/ChatModel/Message/ChatOptions/Retry/Usage/多模型 |
| 05 | 05-Prompt.md | 模板、Few-shot/CoT/ReAct、版本化、Nacos 热更新 |
| 06 | 06-Advisor.md | Advisor 链模型、内置与自定义、顺序控制 |
| 07 | 07-ToolCalling.md | Tool 全景：定义/动态/异步/HTTP/DB/权限/安全 |
| 08 | 08-Memory.md | ChatMemory 新 API、Redis/JDBC、窗口/摘要、长短期 |
| 09 | 09-RAG.md | Naive→Advanced→Hybrid、ETL、Rerank、Citation、评测、降本 |
| 10 | 10-Embedding.md | EmbeddingModel、基准、成本 |
| 11 | 11-VectorStore.md | Milvus/PGVector/Redis/ES、Filter、Hybrid、Score/Rerank |
| 12 | 12-MCP.md | Client/Server、Streamable HTTP、Nacos Registry、认证 |
| 13 | 13-Agent.md | ReactAgent、Planning/Reflection/自纠错、Agent Skills、HITL |
| 14 | 14-Workflow.md | Graph：State/并行边/聚合/中断/补偿/Saga/Checkpoint |
| 15 | 15-MultiAgent.md | 内置四模式、Supervisor/Handoffs、A2A |
| 16 | 16-StructuredOutput.md | Bean/Record/Schema/泛型嵌套/Validation/容错 |
| 17 | 17-Streaming.md | stream API、SSE 协议约定、流式+Tool/结构化 |
| 18 | 18-Observability.md | Micrometer/Tracing/Token 与成本看板/Prometheus/Grafana |
| 19 | 19-BestPractice.md | 统一 Starter/异常/日志/配置/测试/CI/CD/Docker（starter 模块落地） |
| 20 | 20-企业实践.md | 路由/降级/容灾、Prompt 治理、成本、安全体系（注入/脱敏/审计） |
| 21 | 21-版本升级指南.md | 《Spring AI Alibaba 最新版本升级指南》1.0.x→1.1.2.2 |
| 22 | 22-SpringAI2.0前瞻.md | 2.0 已 GA 但 SAA 未跟进的现状分析、破坏性变更清单与迁移决策框架 |

## 2. 章节统一骨架（每章必须包含，Phase 2 执行标准）

```
学习目标 → 前置知识 → 核心概念 → 原理与架构（Mermaid：架构图/流程图/时序图按需）
→ API 深入解析 → 可运行 Demo（源码位置 + 运行命令 + 运行结果）
→ 关键源码解读 → 企业实践建议 → 性能优化建议 → 安全建议
→ 常见踩坑 → 版本差异（1.0.x 对比 + 1.1.2 与 1.1.8 差异标注）→ 为什么这样设计
→ FAQ → 本章总结 → 延伸阅读 → 下一章预告 → 思考题
```

篇幅要求 3000~10000 字/章；所有代码可复制运行；所有图 Mermaid 直接渲染；禁止已废弃 API（如 `PromptChatMemoryAdvisor`、可变 Options setter）。
