---
name: saa-conventions
description: >
  spring-ai-alibaba-learning 仓库的强制工程约定。当在本仓库内新建/修改任何 Demo 工程
  (examples/) 或企业项目 (projects/) 模块、编写 pom.xml、application.yml、Controller、
  或做 Spring AI Alibaba 相关编码时，必须先加载并遵守本 skill。GSD 的 project-skills
  discovery 会自动发现它。
---

# Spring AI Alibaba Learning — 工程约定（强制）

在本仓库编写任何代码前，遵守以下约定。完整背景见仓库根 `HANDOFF-TO-CLAUDE-CODE.md` 与 `CLAUDE.md`。

## 版本（父 POM 唯一真源，勿在子模块硬编码版本号）
Java 21 · Spring Boot 3.5.16 · SAA 1.1.2.2 · SAA Extensions 1.1.2.2 · Spring AI 1.1.2。
子模块 `pom.xml` 的 `<parent>` 指向仓库父 POM，继承 `dependencyManagement`；**不写死任何 SAA/Spring AI 版本号**。
父 POM 已同时导入 `spring-ai-alibaba-bom` 与 `spring-ai-alibaba-extensions-bom`——二者缺一不可。

## 命名与端口
- 包根 `com.flywhl.saa.<模块名>`；类头 `@author flywhl`。
- Demo 端口：`examples/NN-名称` → `server.port=180NN`（example 29 → 18029）。
- Server/Client 配对：Client 端口 = Server 端口 + 100（Server 18034 → Client 18134）。
- 新建模块前先查 `examples/README.md` 与相邻章节 Demo，确认端口不冲突。
- **小 DTO/record 优先与使用方同包**（`com.flywhl.saa.<mod>`）。仅被 1～2 个类使用时不要拆 `model` 子包——JDT 偶发解析不到 `*.model`，IDE 报「包不存在/找不到符号」而 `mvn compile` 仍绿（见 `13-http-tool-demo` / `StockPrice`）。多处复用或字段较多时再抽 `model`。

## 代码质量
- `mvn spring-boot:run` 直接可跑；零 TODO、零伪代码、零"请自行补充"。
- 图示用 Mermaid。
- 复用 `saa-learning-common`（`Result<T>`/`PageResult`/`BizException`/`GlobalExceptionHandler`）
  与 `saa-learning-starter`（`AuditLoggingAdvisor`/`ModelRouter`/`CostRecorder`/统一装配）——不重复实现。

## 禁用 API（一律新写法）
- `PromptChatMemoryAdvisor` → `MessageChatMemoryAdvisor` + 显式 `conversationId`
- `CallAroundAdvisor`/`AdvisedRequest`/`AdvisedResponse` → `CallAdvisor`/`ChatClientRequest`/`ChatClientResponse`
- `FunctionCallback` → `@Tool`/`ToolCallback`
- 可变 `XxxOptions` setter → 一律 `XxxOptions.builder()...build()`

## 配置与密钥
- `application.yml` 用 `${AI_DASHSCOPE_API_KEY}` 等占位注入，**绝不硬编码密钥**。
- 中间件依赖用 `docker/docker-compose.yml` 的 profile 起：`bash scripts/infra.sh up <core|vector|mq|search|cloud>`。
- Redis 向量/记忆场景用 `redis/redis-stack-server`（普通 redis 缺 RedisJSON/RediSearch）。

## 测试
- 涉及中间件：Testcontainers（`@Testcontainers` + `@DynamicPropertySource`）。
- 涉及真实模型调用：`@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")`。

## 每个新 Demo 的验收 checklist
1. `mvn -pl <模块> -am clean install` 真实编译通过。
2. 端口 180NN，无冲突。
3. `mvn spring-boot:run` 起得来，`curl` 返回预期输出（与对应章节一致）。
4. `bash scripts/version-audit.sh` 依赖版本唯一。
5. `bash scripts/spring-ai-2-readiness.sh .` 破坏点低位。
6. 对应实现规格见 `docs/tutorial/NN-*.md`——编码前先读该章。
