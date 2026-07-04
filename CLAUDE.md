# CLAUDE.md — spring-ai-alibaba-learning 项目记忆

> Claude Code 每次会话自动加载本文件。完整上下文见 `HANDOFF-TO-CLAUDE-CODE.md`。

## 这是什么
Spring AI Alibaba 企业级教学仓库：教程正文 + 可运行源码 + 三个企业项目，7 阶段交付。
**Phase 1（脚手架）+ Phase 2（22 章教程 + starter + QA）已完成，当前在 Phase 3（40~60 个独立 Demo）。**

## 版本锁定（父 POM 唯一真源，勿改动、勿回退）
- Java 21 · Spring Boot **3.5.16** · SAA **1.1.2.2** · SAA Extensions **1.1.2.2** · Spring AI **1.1.2**
- Lombok 1.18.36 · MapStruct 1.6.3 · springdoc 2.8.9 · knife4j 4.5.0
- **两个 BOM 必须同时导入**：`spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom`
- Spring AI 2.0 已 GA（2026-06-12）但 **SAA 无对齐版** → 当前锁死 Boot 3.5.x，勿升 Boot 4/Spring AI 2.0

## 硬约定（每个新工程必须遵守）
- 包根 `com.flywhl.saa`，作者标识 `@author flywhl`
- Demo 端口 `examples/NN-xxx` → `180NN`；Server/Client 配对时 Client 用 `+100` 偏移
- 图示一律 Mermaid；代码零 TODO/零伪代码，`mvn spring-boot:run` 直接可跑
- 密钥仅经环境变量（`AI_DASHSCOPE_API_KEY` 等），严禁提交
- 集成测试：中间件用 Testcontainers；模型调用用 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")`
- 复用 `saa-learning-common`（Result/异常/全局处理器）与 `saa-learning-starter`（审计/路由/成本），不重复造轮子

## 禁用 API（已废弃，一律用新写法）
- `PromptChatMemoryAdvisor` → `MessageChatMemoryAdvisor`
- `CallAroundAdvisor`/`AdvisedRequest`/`AdvisedResponse` → `CallAdvisor`/`ChatClientRequest`/`ChatClientResponse`
- `FunctionCallback` → `@Tool`/`ToolCallback`
- 可变 Options setter → 一律 Builder（为 2.0 迁移提前对齐）

## 常用命令
```bash
source scripts/setup-env.sh && bash scripts/env-check.sh   # 校验密钥
bash scripts/infra.sh up core vector                       # 起中间件（profiles: core/vector/mq/search/cloud）
mvn -pl common,starter -am clean install                   # 编译公共底座
mvn -pl starter test                                       # 跑 starter 单测
bash scripts/version-audit.sh                              # BOM 对齐自检
bash scripts/spring-ai-2-readiness.sh .                    # 2.0 破坏点扫描
```

## 已知注意点
- **starter 尚未真机编译**（Phase 2 沙箱限制）→ 落地第一件事：编译并跑通 starter 单测。
- Milvus 冷启动 30~60s（依赖 etcd+MinIO 健康检查）。
- Redis 向量/记忆需 `redis/redis-stack-server`（非普通 redis）。

## GSD 流程
用户级已装 open-gsd（skill 形态，`~/.claude/skills/gsd-*/`）。命令用连字符 `/gsd-*`，以 `/gsd-help` 实际输出为准。
存量项目 → `/gsd-init` 登记既有路线图（不重做 Phase 1-2 需求），再走 discuss→research→plan→execute→verify 相位循环推进 Phase 3+。
每章 Demo 的实现规格（源码位置/端口/curl/预期输出）在对应 `docs/tutorial/NN-*.md`，execute 前用 `@` 引用该章最精确。
