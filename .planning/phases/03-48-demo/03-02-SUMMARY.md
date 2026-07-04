---
phase: 03-48-demo
plan: 02
subsystem: examples-batch1
tags: [retry, nacos-prompt, demo]
provides:
  - examples/05-retry-demo
  - examples/08-prompt-nacos-demo
  - Phase 3 首批 01~08 编译门禁
affects: [phase-3-batch-2]
tech-stack:
  added: [spring-ai-alibaba-starter-nacos-prompt]
  patterns: [custom-RetryTemplate, ConfigurablePromptTemplateFactory-fallback]
key-files:
  created:
    - examples/05-retry-demo/
    - examples/08-prompt-nacos-demo/
  modified: []
key-decisions:
  - ConfigurablePromptTemplate 包名用 com.alibaba.cloud.ai.prompt（SAA 1.1.2.2 实际 API）
  - 08 IT 关闭 Nacos 订阅，仅验证 Factory 兜底
duration: 25min
completed: 2026-07-04
---

# Phase 3 Plan 02 Summary

**补齐 05-retry-demo 与 08-prompt-nacos-demo；首批 01~08 全部 compile + 05/08 IT 通过。**

## Accomplishments
- 05：`spring.ai.retry.*` + 自定义 `RetryTemplate`/`RetryAttemptCounter`，接口 `/chat` `/retry/policy` `/retry/stats`
- 08：`ConfigurablePromptTemplateFactory` + 默认模板兜底，端口 18008，依赖 Nacos（`infra.sh up cloud`）
- 01~08 编译全绿；05/08 `@EnabledIfEnvironmentVariable` 冒烟 IT 通过
- 无废弃 API / 硬编码密钥 / TODO

## Next Phase Readiness
首批（章节 01~08 对应 Demo）交付完成。Phase 3 仍有 09~48，需后续 plan（advisor/tool/memory → RAG → Agent → best-practice）。
限额中断残留的 11~17 半成品留给下一批次收尾。
