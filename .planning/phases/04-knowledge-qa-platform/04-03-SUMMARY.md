---
phase: 04-knowledge-qa-platform
plan: 03
subsystem: rag
tags: [modular-rag, milvus, etl, minio, chatclient, citation, async-etl]

requires:
  - phase: 04-02
    provides: MessageChatMemoryAdvisor、VectorStoreConfig、AsyncConfig etlExecutor、PromptTemplateProvider
provides:
  - RagPipelineFactory（RetrievalAugmentationAdvisor + documentRetriever Bean）
  - CitationPostProcessor（Document → CitationVO）
  - DocumentEtlPipeline 异步 ETL + reindex/deleteIndex
  - IngestStatusTracker 状态机
  - AiClientConfig ChatClient 完整 Advisor 链
  - DemoKnowledgeSeeder 启动补齐演示向量
affects: [04-04-qa, 04-05-admin]

tech-stack:
  added: [RetrievalAugmentationAdvisor, RewriteQueryTransformer, TikaDocumentReader, TokenTextSplitter]
  patterns: ["Modular RAG mutate() 防递归", "单例 documentRetriever 供 Citation 复用", "MinIO 缺失内联样本降级", "@Lazy self 触发 @Async"]

key-files:
  created:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/DemoKnowledgeSeeder.java
  modified:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/rag/RagPipelineFactory.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/rag/CitationPostProcessor.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/rag/DocumentEtlPipeline.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/rag/IngestStatusTracker.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/AiClientConfig.java

key-decisions:
  - "RagPipelineFactory 使用 @Configuration 暴露 Bean（@Configuration 即 @Component 语义）"
  - "chunkOverlap 以字符滑动窗口近似（TokenTextSplitter 1.1.2 无 overlap API）"
  - "Citation chunkId 通过 milvus_pk 回查 kb_chunk，避免向量 metadata 二次更新"

patterns-established:
  - "ETL metadata 键：documentId/title/seqNo，溯源经 milvus_pk 关联 chunkId"
  - "ChatClient Advisor 顺序：MessageChatMemoryAdvisor → RetrievalAugmentationAdvisor → AuditLoggingAdvisor"

requirements-completed: [REQ-phase-4-knowledge-qa]

duration: 12min
completed: 2026-07-05
---

# Phase 4 Plan 03: RAG 管线 + ETL + ChatClient Summary

**Modular RAG（top-k=5、threshold=0.35）+ 异步 MinIO/Tika ETL + Memory→RAG→Audit ChatClient 链，Seeder 补齐 data.sql 演示向量**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-05T15:02:44Z
- **Completed:** 2026-07-05T15:14:44Z
- **Tasks:** 3
- **Files modified:** 6

## Accomplishments

- RagPipelineFactory：RewriteQueryTransformer（mutate 防递归）+ VectorStoreDocumentRetriever + 分数降序 Top-K + allowEmptyContext(false)
- DocumentEtlPipeline：@Async ETL、MinIO/Tika 解析、Milvus 写入、状态机、reindex/deleteIndex；MinIO 缺失时内联演示样本
- AiClientConfig：FallbackModelRouter 路由 ChatModel，Advisor 链 Memory→RAG→Audit
- DemoKnowledgeSeeder：INDEXED 且无 kb_chunk 的文档启动时 reindex

## Task Commits

Each task was committed atomically:

1. **Task 1: RagPipelineFactory + CitationPostProcessor** - `aeabd38` (feat)
2. **Task 2: DocumentEtlPipeline + IngestStatusTracker** - `32ecc59` (feat)
3. **Task 3: AiClientConfig + DemoKnowledgeSeeder** - `4d63036` (feat)

**Plan metadata:** `ead52b0` (docs: complete plan)

## Files Created/Modified

- `rag/RagPipelineFactory.java` — RetrievalAugmentationAdvisor + documentRetriever Bean
- `rag/CitationPostProcessor.java` — 检索结果 → CitationVO，支持同一 retriever 复用
- `rag/DocumentEtlPipeline.java` — 异步 ETL、向量索引、级联删除、内联样本
- `rag/IngestStatusTracker.java` — UPLOADED/PARSING/INDEXED/FAILED 状态封装
- `config/AiClientConfig.java` — ChatClient Bean 装配
- `config/DemoKnowledgeSeeder.java` — 启动补齐演示 Milvus 向量

## Decisions Made

- RagPipelineFactory 采用 @Configuration(proxyBeanMethods=false) 暴露 Advisor Bean（与 28-advanced-rag-demo 一致）
- TokenTextSplitter 仅 withChunkSize；overlap 用字符级滑动窗口（chunkOverlap×4）近似 64 token
- Citation chunkId 通过 milvus_pk 查 kb_chunk，避免向量写入后再更新 metadata

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

运行时需配置（compile 不依赖）：
- `AI_DASHSCOPE_API_KEY` — 查询改写与 Embedding
- `DEEPSEEK_API_KEY` — FallbackModelRouter 备用通道
- Milvus + MinIO + PostgreSQL — Seeder reindex 与完整问答 UAT

## Next Phase Readiness

- 04-04 可注入 ChatClient、CitationPostProcessor、documentRetriever 实现 QaController/QaService
- 应用完整启动仍依赖 04-04 Controller/Service 实现
- Seeder 在 Milvus 可达时自动补齐演示向量

## Self-Check: PASSED

- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/rag/RagPipelineFactory.java
- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/DemoKnowledgeSeeder.java
- FOUND: aeabd38, 32ecc59, 4d63036
- `mvn -f projects/knowledge-qa-platform/pom.xml compile` — PASSED
- `rg QuestionAnswerAdvisor|PromptChatMemoryAdvisor` projects/knowledge-qa-platform/src — 无匹配

---
*Phase: 04-knowledge-qa-platform*
*Completed: 2026-07-05*
