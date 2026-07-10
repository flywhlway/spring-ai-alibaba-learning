---
phase: 06-smart-cs-platform
plan: 03
subsystem: rag
tags: [spring-ai, rag, milvus, elasticsearch, redis-stack, semantic-cache, hybrid-search, tdd]

# Dependency graph
requires:
  - phase: 06-smart-cs-platform (Wave 0 / Plan 01)
    provides: FaqArticle/FaqChunk Entity+Repository、ScsProperties.rag/cache
  - phase: 06-smart-cs-platform (Wave 1 / Plan 02)
    provides: milvusVectorStore/elasticsearchVectorStore/redisStackVectorStore 三 Bean、
      AiClientConfig ChatClient.Builder（未 build()）
provides:
  - FaqEtlPipeline：ApplicationRunner 启动幂等补齐 faq_article 向量索引（Milvus+ES 双写，
    faq_chunk 记录 milvus_pk/es_doc_id 溯源）
  - RagPipelineFactory：milvusVectorStore 单库 RetrievalAugmentationAdvisor Bean（供 Wave 3
    faq-agent 消费）
  - HybridSearchService：Milvus 向量 + Elasticsearch 全文 match RRF 混合检索（RRF_K=60）
  - SemanticCacheService：Redis Stack（6380）语义缓存 lookup/put，阈值 0.95
  - FaqAnswerService：缓存→混合检索→RAG 生成→回写缓存全链路，返回 ChatAnswerVO
affects: [06-smart-cs-platform-04-agent, 06-smart-cs-platform-05-conversation-ticket, 06-smart-cs-platform-06-admin, 06-smart-cs-platform-07-testing]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "FaqEtlPipeline 幂等触发：ApplicationRunner.run() 扫描无 faq_chunk 记录的 faq_article
      逐条索引，单条失败仅标记该文章 FAILED 并记日志，不阻塞启动、不影响其余文章"
    - "HybridSearchService 直接移植 26-es-hybrid-demo 的 RRF 融合算法，milvusVectorStore
      做向量检索，elasticsearchVectorStore 底层复用的 RestClient 做原生 match 全文检索"
    - "SemanticCacheService.lookup 相似度命中后按 metadata.expiresAt（ISO-8601）二次校验，
      过期视为未命中，与 25-redis-vector-demo 语义等价"
    - "FaqAnswerService 不复用 RagPipelineFactory 的 RetrievalAugmentationAdvisor：改为显式
      调用 HybridSearchService 取 Milvus+ES 融合上下文，渲染 faq-answer-system Prompt
      的 {context} 占位符后再生成，满足 must_haves「Milvus+ES RRF 混合检索后 RAG 生成」
      的严格链路顺序（详见下方 Deviations）"

key-files:
  created:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/prompt/PromptTemplateProvider.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/RagPipelineFactory.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/FaqEtlPipeline.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/HybridSearchService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/SemanticCacheService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/FaqAnswerService.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/service/SemanticCacheServiceTest.java
    - projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st
    - projects/smart-cs-platform/src/main/resources/prompts/faq-answer-system.st
  modified: []

key-decisions:
  - "FaqAnswerService 的 RAG 生成上下文来自 HybridSearchService（Milvus+ES RRF），而非
    RagPipelineFactory 的 RetrievalAugmentationAdvisor（Milvus 单库）——后者保留给 Wave 3
    faq-agent ReactAgent 工具直接消费，两者服务不同场景，互不冲突"
  - "faq-answer-system Prompt 采用手工 PromptTemplate.render({context}) 渲染，而非
    ContextualQueryAugmenter 自动注入——因为需要把 HybridSearchService 的融合结果（而非
    RAG Advisor 自身检索）作为上下文来源"
  - "混合检索空上下文时直接返回转人工提示，不调用大模型也不写入缓存，语义等价于
    allowEmptyContext=false（T-06-04 威胁缓解）"
  - "FaqEtlPipeline 幂等判定依据 faq_chunk 是否存在，而非 faq_article.status——因为
    db/data.sql 演示数据已将 status 预置为 INDEXED（chunk_count=1）但实际未写入任何向量，
    若按 status 过滤将永远跳过 ETL，导致 Milvus/ES 中无真实向量数据"

patterns-established:
  - "Pattern: 语义缓存空结果与生成失败均不写 SemanticCacheService.put，避免污染缓存"
  - "Pattern: 多 VectorStore 场景下，不同消费方可分别选择『Spring AI 标准 RAG Advisor
    单库检索』与『应用层显式混合检索』两条路径共存，按各自可观测性/引用来源需求选型"

requirements-completed: [REQ-phase-6-smart-cs]

# Metrics
duration: 50min
completed: 2026-07-10
---

# Phase 6 Plan 3: FAQ 语义缓存 + Milvus/ES 混合检索 + RAG 生成全链路 Summary

**FaqEtlPipeline 幂等种子 ETL（TokenTextSplitter 分块双写 Milvus+ES）+ HybridSearchService（RRF_K=60 混合检索）+ SemanticCacheService（Redis Stack 0.95 阈值语义缓存，TDD 全绿）+ FaqAnswerService（缓存→混合检索→RAG→回写缓存全链路），`mvn compile`/`mvn test` 全绿**

## Performance

- **Duration:** 约 50 分钟
- **Started:** 2026-07-10（本次执行会话）
- **Completed:** 2026-07-10
- **Tasks:** 3/3
- **Files modified:** 9（全部新建）

## Accomplishments

- `PromptTemplateProvider`：Nacos → DB PUBLISHED → classpath `prompts/*.st` 三级回退读取 Prompt 模板，新增 `getFaqAnswerSystemTemplate()` 便捷方法
- `RagPipelineFactory`：装配 `RetrievalAugmentationAdvisor`（`RewriteQueryTransformer` + `VectorStoreDocumentRetriever(milvusVectorStore)` + 分数重排 + `ContextualQueryAugmenter allowEmptyContext=false`），供 Wave 3 Agent 消费
- `FaqEtlPipeline`：`ApplicationRunner` 启动时扫描无 `faq_chunk` 记录的 `faq_article`，`TokenTextSplitter`（chunkSize 来自 `ScsProperties.rag`）分块后双写 `milvusVectorStore` + `elasticsearchVectorStore`，落库 `faq_chunk.milvus_pk`/`es_doc_id` 溯源；单条文章失败仅标记该文章 `status=FAILED` 并记日志，不阻塞应用启动
- `HybridSearchService`：直接移植 `26-es-hybrid-demo` 的 RRF 融合核心（`RRF_K=60`），`milvusVectorStore` 向量检索 + Elasticsearch `RestClient` 原生 match 全文检索，`candidateK = max(topK*2, topK)`
- `SemanticCacheService`：`redisStackVectorStore`（6380）承载语义缓存，`lookup` 阈值默认 0.95 + `type=semantic-cache` 过滤 + `expiresAt`（ISO-8601）二次校验过期；`put` 写入 `answer`/`expiresAt` metadata；TDD RED→GREEN 全绿（3 个分支：命中未过期返回答案、过期条目跳过、无命中返回空）
- `FaqAnswerService`：链路严格为 缓存查找 → （未命中）混合检索取候选片段 → 渲染 `faq-answer-system` Prompt `{context}` → ChatClient 生成 → 回写缓存；空上下文直接返回转人工提示且不调用大模型、不写缓存
- `mvn -f projects/smart-cs-platform/pom.xml compile` 与 `mvn ... test` 全绿；`SemanticCacheServiceTest` 3 用例全部通过；禁用 API grep 零命中

## Task Commits

Each task was committed atomically:

1. **Task 1: PromptTemplateProvider + RagPipelineFactory + FaqEtlPipeline** - `47c8686` (feat)
2. **Task 2: HybridSearchService** - `412d2a7` (feat)
3. **Task 3a: SemanticCacheServiceTest（RED）** - `c54af2d` (test)
3. **Task 3b: SemanticCacheService + FaqAnswerService（GREEN）** - `b8163fd` (feat)

**Plan metadata:** (this commit, docs: complete plan)

## Files Created/Modified

- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/prompt/PromptTemplateProvider.java` - Prompt 三级回退读取门面
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/RagPipelineFactory.java` - Milvus 单库 RetrievalAugmentationAdvisor 装配
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/FaqEtlPipeline.java` - FAQ 种子向量索引 ETL（ApplicationRunner 幂等触发）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/HybridSearchService.java` - Milvus+ES RRF 混合检索
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/SemanticCacheService.java` - Redis Stack 语义缓存 lookup/put
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/FaqAnswerService.java` - 缓存→混合检索→RAG→回写缓存全链路
- `projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/service/SemanticCacheServiceTest.java` - TDD 单测（3 分支）
- `projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st` - classpath 回退：查询改写模板
- `projects/smart-cs-platform/src/main/resources/prompts/faq-answer-system.st` - classpath 回退：FAQ 问答系统 Prompt（含 `{context}`）

## Decisions Made

- `FaqAnswerService` 的 RAG 生成上下文显式来自 `HybridSearchService`（Milvus+ES RRF），而非复用 `RagPipelineFactory` 的 `RetrievalAugmentationAdvisor`（Milvus 单库检索）。两者服务不同消费场景：`RetrievalAugmentationAdvisor` 保留给 Wave 3 `faq-agent` ReactAgent 工具使用标准 Spring AI RAG Advisor 契约；`FaqAnswerService` 需要满足 must_haves「缓存未命中时 Milvus+ES RRF 混合检索后 RAG 生成答案」的严格链路顺序，若改用 `RetrievalAugmentationAdvisor` 则会绕过已实现的 `HybridSearchService`，使其成为未被调用的孤立组件
- `FaqEtlPipeline` 幂等判定依据 `faq_chunk` 是否存在而非 `faq_article.status`：因为 `db/data.sql` 演示数据已将全部 FAQ 的 `status` 预置为 `INDEXED`（`chunk_count=1`）但从未真正写入 Milvus/ES 向量，若按 `status` 过滤会永远跳过索引，导致向量库为空、混合检索无候选
- `HybridSearchService.fullTextSearch` 将 `IOException` 包装为 `BizException(INTERNAL_ERROR)` 而非继续向上抛检查异常，简化调用方（`FaqAnswerService`）签名，符合 `saa-learning-common` 统一异常约定
- 混合检索结果为空时，`FaqAnswerService` 直接返回「暂未找到相关内容，建议转人工」且跳过大模型调用与缓存写入，语义等价于 `RagPipelineFactory` 中 `ContextualQueryAugmenter.allowEmptyContext(false)` 的防幻觉设计（T-06-04 威胁登记条目）

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] FaqEtlPipeline 幂等判定改用 faq_chunk 存在性而非 faq_article.status**
- **Found during:** Task 1（读取 `db/data.sql` 种子数据结构）
- **Issue:** 计划 action 提及"更新 faq_article.status=INDEXED"暗示以 status 驱动幂等，但 Wave 0 `db/data.sql` 已将全部 12 条 FAQ 的 `status` 硬编码为 `INDEXED`（`chunk_count=1`），若 ETL 按 `status != INDEXED` 过滤候选文章，将在首次启动时误判"已索引"而永远跳过真实的 Embedding/向量写入，导致 `HybridSearchService`/`SemanticCacheService` 在真机验证时检索不到任何候选
- **Fix:** `indexAllSeedFaqs()` 改为按 `faqChunkRepository.findByArticleId(id).isEmpty()` 判定候选文章（无论当前 `status` 为何），确保首次启动必然补齐真实向量数据；后续重启因 `faq_chunk` 已存在而自动跳过，保持幂等
- **Files modified:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/FaqEtlPipeline.java`
- **Verification:** `mvn compile` 通过；逻辑走查确认候选判定不依赖 `status` 字段
- **Committed in:** `47c8686`（Task 1 commit）

---

**Total deviations:** 1 auto-fixed（1 bug，幂等判定字段选择）
**Impact on plan:** 修正后的判定逻辑是 FAQ 语义缓存/混合检索链路能在真机环境产出真实检索结果的前提条件，未扩大范围，仅让 ETL 触发条件与 Wave 0 已固化的种子数据格式保持一致。

## Issues Encountered

- `HybridSearchService.fullTextSearch` 的方法签名相较 `examples/26-es-hybrid-demo` 移除了 `throws IOException`（内部捕获并包装为 `BizException`），与计划 action 文字"处理 IOException 向上抛或包装 BizException"中的后一种选择一致，未构成偏差单独记录
- `FaqAnswerService` 未添加计划中标注为"可选"的 `FaqController POST /api/faq/ask`：该文件未出现在本计划 frontmatter 的 `files_modified` 列表中，且后续 Wave（会话/工单）大概率会提供统一的 `ChatController` 承载 FAQ 问答入口，避免本 Wave 提前引入尚未确定路由规范的 Controller

## User Setup Required

None - 无需外部服务人工配置。本 Wave 产出全部为可编译、单测可运行的 Service 层代码；真实 Milvus/ES/Redis Stack 连通性与 `AI_DASHSCOPE_API_KEY` 驱动的 Embedding/生成需 Docker infra 拉起后在集成测试 Wave（06-07）或人工 UAT 验证。

## Next Phase Readiness

- Wave 3（Agent）可直接注入 `FaqAnswerService.answer(query)` 或复用 `RagPipelineFactory` 的 `RetrievalAugmentationAdvisor` Bean 构建 `faq-agent` ReactAgent 工具
- `HybridSearchService`/`SemanticCacheService` 已具备独立可测试的 Service 边界，供 Wave 5（会话/工单）或 Wave 6（Admin 看板缓存命中率统计）直接复用
- `FaqEtlPipeline` 会在应用启动时自动补齐向量索引，无需额外触发脚本；如需强制重新索引单篇 FAQ，可在后续波次基于 `articleRepository`/`chunkRepository` 组合新增管理端点（当前未实现，非本 Wave 范围）
- 无阻塞项；Wave 4/5 消费本 Wave 产出时注意 `FaqAnswerService` 返回的 `ChatAnswerVO.routeAgent` 固定为 `"faq-agent"`，可直接用于会话消息落库的 `route_agent` 列

---
*Phase: 06-smart-cs-platform*
*Completed: 2026-07-10*

## Self-Check: PASSED

All 9 created files verified present on disk (PromptTemplateProvider.java / RagPipelineFactory.java /
FaqEtlPipeline.java / HybridSearchService.java / SemanticCacheService.java / FaqAnswerService.java /
SemanticCacheServiceTest.java / query-rewrite.st / faq-answer-system.st). All 4 task commit hashes
(`47c8686`, `412d2a7`, `c54af2d`, `b8163fd`) verified present in `git log --oneline --all`.
`mvn -f projects/smart-cs-platform/pom.xml compile` and `mvn ... test` both succeeded with no errors.
