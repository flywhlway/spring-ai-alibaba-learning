---
phase: 03-48-demo
plan: 06
subsystem: rag
tags: [rag, milvus, question-answer-advisor, retrieval-augmentation-advisor, citation, eval]

requires:
  - phase: 01-scaffold
    provides: parent POM、saa-learning-common、Result/GlobalExceptionHandler
provides:
  - examples/27-rag-demo（Naive RAG / QuestionAnswerAdvisor）
  - examples/28-advanced-rag-demo（Modular RAG / 查询改写+重排序）
  - examples/29-hybrid-rag-demo（Citation 应用层拼接）
  - examples/30-rag-eval-demo（忠实度/相关性评测 + 缓存）
affects: [03-48-demo batch2, enterprise-project-1 knowledge-base]

tech-stack:
  added:
    - spring-ai-starter-vector-store-milvus
    - spring-ai-advisors-vector-store
    - spring-ai-rag
  patterns:
    - Naive RAG via QuestionAnswerAdvisor
    - Modular RAG via RetrievalAugmentationAdvisor pipeline
    - Application-layer Citation from metadata.source
    - Rule-based faithfulness/relevance eval with in-memory answer cache

key-files:
  created:
    - examples/27-rag-demo/src/main/java/com/flywhl/saa/rag/RagConfig.java
    - examples/28-advanced-rag-demo/src/main/java/com/flywhl/saa/advancedrag/AdvancedRagConfig.java
    - examples/29-hybrid-rag-demo/src/main/java/com/flywhl/saa/hybridrag/RagController.java
    - examples/30-rag-eval-demo/src/main/java/com/flywhl/saa/rageval/RagEvalService.java
  modified: []

key-decisions:
  - "RetrievalAugmentationAdvisor 使用真实包名 org.springframework.ai.rag.advisor（教程 import 有误）"
  - "重排序用 DocumentPostProcessor 按 Document.score 截断 Top-5（1.1.2 无内置 DocumentRanker）"
  - "评测采用关键词覆盖启发式，优先可编译最小实现而非 LLM-as-judge"
  - "四 Demo 独立 Milvus collection，避免互相污染"

patterns-established:
  - "RAG yml：Milvus + text-embedding-v4 dimensions=1024 + initialize-schema"
  - "T-27-01：system 提示仅依据知识库；空上下文明确拒绝编造"
  - "Citation 必须应用层从 metadata.source 拼接，不依赖模型自述"

requirements-completed: [REQ-phase-3-demos]

duration: 7min
completed: 2026-07-04
---

# Phase 3 Plan 06: RAG 四件套（27~30）Summary

**Naive → Advanced → Hybrid+Citation → Eval：四个可编译 RAG Demo，默认 Milvus 1024 维，Citation 应用层溯源**

## Performance

- **Duration:** 7 min
- **Started:** 2026-07-04T14:36:00Z
- **Completed:** 2026-07-04T14:42:49Z
- **Tasks:** 4/4
- **Files modified:** 31（新建）

## Accomplishments

- **27-rag-demo**：`QuestionAnswerAdvisor` Naive RAG，端口 18027，T-27-01 system 提示 + 空上下文兜底
- **28-advanced-rag-demo**：`RetrievalAugmentationAdvisor` + `RewriteQueryTransformer` + score 重排序
- **29-hybrid-rag-demo**：对齐教程 `/ask` 返回 `answer` / `citations` / `evidenceCount`（`metadata.source`）
- **30-rag-eval-demo**：`POST /eval/run` 忠实度/相关性评分 + 内存响应缓存

## Task Commits

Each task was committed atomically:

1. **Task 1: 新建 27-rag-demo（Naive RAG）** - `ea0db26` (feat)
2. **Task 2: 新建 28-advanced-rag-demo** - `9563d4a` (feat)
3. **Task 3: 新建 29-hybrid-rag-demo（Citation）** - `2f62ae0` (feat)
4. **Task 4: 新建 30-rag-eval-demo** - `e296779` (feat)

**Plan metadata:** （见本 SUMMARY 提交）

## Files Created/Modified

- `examples/27-rag-demo/` — Naive RAG 全模块（含冒烟 IT）
- `examples/28-advanced-rag-demo/` — Advanced Modular RAG
- `examples/29-hybrid-rag-demo/` — Hybrid RAG + Citation
- `examples/30-rag-eval-demo/` — 评测端点与规则指标

## Decisions Made

- 教程中 `RetrievalAugmentationAdvisor` 的 import 包名错误，实现使用 Spring AI 1.1.2 真实包 `org.springframework.ai.rag.advisor`
- 无内置 Reranker 类时，用 `DocumentPostProcessor` 按 `Document.getScore()` 降序截断演示重排序扩展点
- 评测优先规则启发式（关键词覆盖），README 明确局限；缓存用 `ConcurrentHashMap` 演示降本
- 各 Demo 使用独立 collection（`saa_rag_naive` / `saa_rag_advanced` / `saa_knowledge` / `saa_rag_eval`）

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 修正 RetrievalAugmentationAdvisor 包名**
- **Found during:** Task 2/3
- **Issue:** 教程写 `org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor`，1.1.2 jar 中实际为 `org.springframework.ai.rag.advisor`
- **Fix:** 使用真实包名，否则无法编译
- **Files modified:** 28/29/30 相关 Java 源码
- **Committed in:** `9563d4a` / `2f62ae0` / `e296779`

**2. [Rule 2 - Missing Critical] Milvus 客户端认证与 initialize-schema**
- **Found during:** Task 1
- **Issue:** 教程 09 yml 缺 username/password 与 `initialize-schema`，本地 compose Milvus 默认需认证且需自动建表
- **Fix:** 对齐教程 11 的 Milvus 配置片段
- **Files modified:** 四个 Demo 的 `application.yml`
- **Committed in:** 各 task commit

**3. [Rule 2 - Missing Critical] 29 增加 `/ingest` 与 Result 包装**
- **Found during:** Task 3
- **Issue:** 教程仅给 `/ask`，无入库则 curl 无证据；D-09 要求 Result 包装
- **Fix:** 增加样例 `/ingest`；`/ask` 返回 `Result<Map>`（data 内仍为 answer/citations/evidenceCount）
- **Files modified:** `examples/29-hybrid-rag-demo/.../RagController.java`
- **Committed in:** `2f62ae0`

---

**Total deviations:** 3 auto-fixed（1 blocking, 2 missing critical）
**Impact on plan:** 均为可编译/可运行与安全约定所必需，无范围蔓延。

## Issues Encountered

None beyond the package-name correction above.

## User Setup Required

真机运行需：

```bash
bash scripts/infra.sh up vector   # 或 core vector
export AI_DASHSCOPE_API_KEY=...
```

各 Demo：`cd examples/NN-xxx && mvn spring-boot:run`

## Next Phase Readiness

- RAG 能力域 27~30 已交付，可进入同批 Embedding/VectorStore/MCP plans（03-04/05/07/08）
- 未改动 demos 01~19 及其他 plan 的 demos
- STATE.md / ROADMAP.md 按执行指令未修改（由 orchestrator 统一更新）

## Self-Check: PASSED

- 四个 `pom.xml` 与核心源码文件均存在
- 提交 `ea0db26` / `9563d4a` / `2f62ae0` / `e296779` 均在 git log
- `mvn -f examples/{27,28,29,30}-*/pom.xml -q compile` 全绿

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
