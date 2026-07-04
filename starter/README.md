# starter —— 统一 AI Starter（saa-learning-starter）

本模块在 **Phase 2 第 19 章（BestPractice / 企业实践）** 随教程落地实现，届时挂载到父 POM `<modules>`。当前为规划占位（无代码，不参与构建），设计蓝图如下，实现阶段不得偏离。

## 目标

将三个企业项目中重复的 AI 基建收敛为一个内部 Starter，体现"企业统一 AI 接入层"最佳实践：

| 能力 | 说明 |
|---|---|
| 统一 ChatClient 装配 | 按配置装配 DashScope / DeepSeek 双通道 ChatClient，命名 Bean 隔离 |
| 统一 Advisor 链 | 默认注入：日志审计 Advisor → 敏感词/脱敏 Advisor →（可选）Memory / RAG Advisor，顺序号集中管理 |
| 统一模型路由 | `ModelRouter` 抽象：按场景/成本/健康度选择 ChatModel，支持降级链 |
| 统一可观测 | Token/耗时/成本 Micrometer 指标 + traceId 贯通 |
| 统一配置 | `saa.learning.*` 前缀 ConfigurationProperties + IDE 元数据 |

## 规划的模块结构

```
starter/
├── pom.xml
└── src/main/java/com/flywhl/saa/starter/
    ├── autoconfigure/   # AutoConfiguration + @ConditionalOn* 装配
    ├── chat/            # ChatClient 工厂与命名策略
    ├── advisor/         # 默认 Advisor 实现与顺序常量
    ├── routing/         # ModelRouter / 降级链
    └── metrics/         # 指标绑定
```

对应教程：`docs/tutorial/19-BestPractice.md`（含自动装配源码逐行解读与 AutoConfiguration.imports 机制）。
