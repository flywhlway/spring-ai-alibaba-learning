# starter —— 统一 AI Starter（`saa-learning-starter`）

本模块是仓库的 **企业统一 AI 接入层**，在 Phase 2 第 19 章（BestPractice）落地，并已被三个企业项目与相关 Demo 复用。  
`v1.0` 已交付完成；本文件描述模块能力与用法，**整仓学习大纲与入口见根目录 [README.md](../README.md)**。

对应教程：[docs/tutorial/19-BestPractice.md](../docs/tutorial/19-BestPractice.md)（自动装配源码解读与 `AutoConfiguration.imports` 机制）。

---

## 目标

把三个企业项目中重复的 AI 基建收敛为一个内部 Starter：

| 能力 | 说明 |
|---|---|
| 统一 Advisor | 默认注入 `AuditLoggingAdvisor`（脱敏审计，`CallAdvisor` + `StreamAdvisor`），顺序见 `AdvisorOrder` |
| 统一模型路由 | `ModelRouter` + `FallbackModelRouter`：主备 ChatModel 无锁熔断降级 |
| 统一成本采集 | `CostRecorder` / `LoggingCostRecorder` + `CostTrackingObservationHandler`（基于 `gen_ai.usage.*`） |
| 统一异常处理 | 导入 `saa-learning-common` 的 `GlobalExceptionHandler`；存在 Spring Security 时条件装配 `AccessDeniedExceptionHandler` |
| 统一配置 | `saa.learning.*` → `SaaLearningProperties`（record）+ IDE 友好前缀 |

业务方可随时用 `@ConditionalOnMissingBean` 覆盖任一默认 Bean。

---

## 模块结构

```
starter/
├── pom.xml                          # artifactId: saa-learning-starter
├── README.md
└── src/
    ├── main/java/com/flywhl/saa/starter/
    │   ├── autoconfigure/
    │   │   ├── SaaLearningAutoConfiguration.java
    │   │   └── SaaLearningProperties.java      # 前缀 saa.learning
    │   ├── advisor/
    │   │   ├── AdvisorOrder.java
    │   │   └── AuditLoggingAdvisor.java
    │   ├── routing/
    │   │   ├── ModelRouter.java
    │   │   └── FallbackModelRouter.java
    │   └── metrics/
    │       ├── CostRecorder.java
    │       ├── LoggingCostRecorder.java
    │       └── CostTrackingObservationHandler.java
    ├── main/resources/META-INF/spring/
    │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── test/java/.../routing/FallbackModelRouterTest.java
```

---

## 配置项（`saa.learning`）

| 属性 | 默认值 | 说明 |
|---|---|---|
| `primary-model` | `dashScopeChatModel` | 主模型 Bean 名 |
| `fallback-model` | `deepSeekChatModel` | 降级模型 Bean 名 |
| `audit-enabled` | `true` | 是否装配默认审计 Advisor |
| `cost-tracking.enabled` | `true` | 是否装配成本 ObservationHandler |
| `cost-tracking.price-per1k-input-tokens` | `0.0008` | 每千输入 token 单价（元，示例） |
| `cost-tracking.price-per1k-output-tokens` | `0.002` | 每千输出 token 单价（元，示例） |

`ModelRouter` 仅在容器中**同时存在**主/备两个具名 `ChatModel` Bean 时装配（`@ConditionalOnBean`）。

---

## 如何引入

子模块 `pom.xml` 以本仓库父 POM 为 parent，声明依赖即可（**不要硬编码 SAA / Spring AI 版本号**）：

```xml
<dependency>
    <groupId>com.flywhl.saa</groupId>
    <artifactId>saa-learning-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

确保应用已装配 DashScope / DeepSeek 等 ChatModel（命名与配置一致），然后按需在 `ChatClient` 上挂载 `AuditLoggingAdvisor`，或注入 `ModelRouter` 做主备调用。

---

## 本地验证

```bash
# 在仓库根目录
mvn -pl common,starter -am clean install
mvn -pl starter test
```

相关质量脚本：

```bash
bash scripts/version-audit.sh
bash scripts/spring-ai-2-readiness.sh .
```

---

## 与整仓的关系（学习入口）

| 路径 | 角色 |
|---|---|
| [根 README](../README.md) | **学习大纲与仓库入口**（推荐从这里开始） |
| [docs/README.md](../docs/README.md) | 22 章教程索引 |
| [examples/README.md](../examples/README.md) | 48 Demo 清单与端口约定 |
| [projects/README.md](../projects/README.md) | 三个企业项目（已交付）与蓝图 SSOT |
| 本模块 | 第 19～20 章工程化能力的可复用实现 |

建议学习顺序：总览文档 → 教程对应章 → 跑 Demo → 读本 Starter 源码 → 对照企业项目中的复用方式。
