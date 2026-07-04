---
phase: 03-48-demo
plan: 10
subsystem: graph-workflow
tags: [StateGraph, KeyStrategy.REPLACE, MemorySaver, SaverConfig, addEdge, addConditionalEdges, Saga, DashScope]

requires:
  - phase: 03-48-demo
    provides: RESEARCH JAR API 映射（Pattern 3 / Saga；禁止 addAggregatedEdge）
provides:
  - examples/38-workflow-demo（线性 StateGraph + MemorySaver + 冒烟 IT）
  - examples/39-graph-parallel-demo（并行 fan-out/fan-in addEdge List）
  - examples/40-graph-saga-demo（条件边 + compensateInventory）
affects: [03-11, FlowAgent demos, graph-core patterns]

tech-stack:
  added: [spring-ai-alibaba-agent-framework（传递 graph-core）]
  patterns:
    - "KeyStrategy.REPLACE 常量 + KeyStrategyFactory 显式转型"
    - "CompileConfig.saverConfig(SaverConfig.register(MemorySaver))"
    - "并行：addEdge(START, List) + addEdge(List, merge)，无 addAggregatedEdge"
    - "Saga：edge_async + addConditionalEdges + 内存补偿节点"

key-files:
  created:
    - examples/38-workflow-demo/pom.xml
    - examples/38-workflow-demo/src/main/java/com/flywhl/saa/workflow/WorkflowGraphConfig.java
    - examples/38-workflow-demo/src/test/java/com/flywhl/saa/workflow/WorkflowDemoApplicationIT.java
    - examples/39-graph-parallel-demo/pom.xml
    - examples/39-graph-parallel-demo/src/main/java/com/flywhl/saa/graphparallel/ParallelGraphConfig.java
    - examples/40-graph-saga-demo/pom.xml
    - examples/40-graph-saga-demo/src/main/java/com/flywhl/saa/graphsaga/SagaGraphConfig.java
    - examples/40-graph-saga-demo/src/main/java/com/flywhl/saa/graphsaga/SagaController.java
  modified: []

key-decisions:
  - "38/39/40 均不引入 saa-learning-starter；只声明 agent-framework（graph-core 传递）"
  - "Checkpoint 一律 SaverConfig + MemorySaver，README 写无中间件（D-15/D-16）"
  - "并行边用 JAR addEdge(List) 真 API，禁止教程伪 API addAggregatedEdge / KeyStrategy.replace()"
  - "Saga 用 ConcurrentHashMap 模拟库存，forceFail 查询参数触发补偿路径"

patterns-established:
  - "线性图：START→rewrite→retrieve→generate→END + invoke(Map, RunnableConfig)"
  - "并行图：addEdge(START, List.of(a,b)) + addEdge(List.of(a,b), merge)"
  - "Saga：addConditionalEdges(node, edge_async(...), Map success/failure)"

requirements-completed: [REQ-phase-3-demos]

duration: 3min
completed: 2026-07-04
---

# Phase 03 Plan 10: Graph/Workflow demos 38~40 Summary

**线性 / 并行 / Saga 三种 StateGraph 形态按 1.1.2.2 JAR 真源交付：KeyStrategy.REPLACE、SaverConfig/MemorySaver、addEdge(List) fan-out/fan-in、edge_async 条件边补偿，端口 18038~18040，common+Result，38 含 Key 门控冒烟 IT。**

## Performance

- **Duration:** 3 min
- **Started:** 2026-07-04T15:47:12Z
- **Completed:** 2026-07-04T15:49:53Z
- **Tasks:** 3/3
- **Files modified:** 26

## Accomplishments

- 38-workflow-demo：线性图 `START→rewrite→retrieve→generate→END`，`MemorySaver` 进程内 Checkpoint，`GET /workflow/run`，冒烟 IT 受 `AI_DASHSCOPE_API_KEY` 门控
- 39-graph-parallel-demo：`addEdge(START, List.of(searchKb, searchHistory))` + `addEdge(List, generateAnswer)`，无 `addAggregatedEdge`
- 40-graph-saga-demo：扣库存→扣款→条件边；`forceFail=true` 走 `compensateInventory`，响应体 `SagaOutcome.compensated`

## Task Commits

1. **Task 1: 新建 38-workflow-demo（线性 StateGraph + 冒烟 IT）** - `23136e2` (feat)
2. **Task 2: 新建 39-graph-parallel-demo（并行 fan-out/fan-in）** - `7c8ef24` (feat)
3. **Task 3: 新建 40-graph-saga-demo（补偿节点）** - `b75dffa` (feat)

## Files Created/Modified

- `examples/38-workflow-demo/**` — 线性工作流 + IT
- `examples/39-graph-parallel-demo/**` — 并行诊断图
- `examples/40-graph-saga-demo/**` — Saga 补偿图

## Decisions Made

- 只声明 `spring-ai-alibaba-agent-framework`，不显式钉 `graph-core` 版本（RESEARCH Anti-Patterns）
- `CompileConfig.builder().saverConfig(SaverConfig.builder().register(new MemorySaver()).build())` 替代教程伪 API `checkpointSaver(...)`
- 条件边用 `edge_async(EdgeAction)` 包装，匹配 `addConditionalEdges(String, AsyncEdgeAction, Map)` 签名

## Deviations from Plan

None - plan executed exactly as written.

## Threat Flags

无新增威胁面：REST 查询参数仅驱动进程内图状态；Saga 无真实支付（T-40-01 已按 threat_model 用内存 Map 缓解）。

## Known Stubs

None — 节点均为可执行实现（retrieve/并行检索用内存假数据属规格内演示，非 TODO）。

## Self-Check: PASSED

- FOUND: examples/38-workflow-demo/pom.xml
- FOUND: examples/39-graph-parallel-demo/pom.xml
- FOUND: examples/40-graph-saga-demo/pom.xml
- FOUND: 23136e2, 7c8ef24, b75dffa
- compile 三模块退出码 0
