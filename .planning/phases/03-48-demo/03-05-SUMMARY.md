---
phase: 03-48-demo
plan: 05
subsystem: vectorstore
tags: [embedding, vectorstore, pgvector, milvus, redis-stack, elasticsearch, hybrid-search, rrf]

requires:
  - phase: 01-scaffold
    provides: parent POM BOM、saa-learning-common、docker infra profiles
provides:
  - examples/22-embedding-demo（EmbeddingModel 维度/成本基准）
  - examples/23-pgvector-demo（PGVector VectorStore）
  - examples/24-milvus-demo（Milvus VectorStore）
  - examples/25-redis-vector-demo（Redis Stack VectorStore + 语义缓存 TTL）
  - examples/26-es-hybrid-demo（ES 向量 + 全文 RRF 混合检索）
affects: [03-06 RAG demos, 企业项目向量后端选型]

tech-stack:
  added:
    - spring-ai-starter-vector-store-pgvector
    - spring-ai-starter-vector-store-milvus
    - spring-ai-starter-vector-store-redis
    - spring-ai-starter-vector-store-elasticsearch
    - redis/redis-stack-server（Demo 级 override）
  patterns:
    - VectorStore.add + similaritySearch + FilterExpressionBuilder
    - Embedding 一律 text-embedding-v4 / dimensions=1024
    - Result<T> + GlobalExceptionHandler
    - ES 混合检索：向量通道 + match 全文 + RRF（应用层）

key-files:
  created:
    - examples/22-embedding-demo/
    - examples/23-pgvector-demo/
    - examples/24-milvus-demo/
    - examples/25-redis-vector-demo/
    - examples/25-redis-vector-demo/docker-compose.override.yml
    - examples/26-es-hybrid-demo/
  modified: []

key-decisions:
  - "25 使用 redis/redis-stack-server 映射 6380，避免与 core 普通 Redis 6379 冲突"
  - "26 混合检索在应用层实现 RRF（Spring AI 1.1.2 ES VectorStore 无内置 Hybrid API）"
  - "Metadata Filter 一律 FilterExpressionBuilder，禁止拼接原生 filter 字符串"

patterns-established:
  - "VectorStore Demo 统一 REST：POST /documents、GET /search、GET /search/filter"
  - "四库 artifact 零版本号，dimensions/embedding-dimension=1024"
  - "中间件依赖写 README 顶部（infra profile 或 Stack 启动命令）"

requirements-completed: [REQ-phase-3-demos]

duration: 12min
completed: 2026-07-04
---

# Phase 3 Plan 05: Embedding + VectorStore Demos Summary

**五个独立 Demo（22~26）覆盖 Embedding 基准与 ADR-004 四库 VectorStore，统一 dimensions=1024 / text-embedding-v4，25 强制 Redis Stack。**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-04T14:36:17Z
- **Completed:** 2026-07-04T14:48:00Z
- **Tasks:** 5/5
- **Files modified:** 38（新建）

## Accomplishments

- 22-embedding-demo：多维度基准（64/256/1024/2048），展示耗时、向量长度、存储字节与 token；冒烟 IT 有 Key 时断言非空
- 23~26：各自独立工程，统一 `VectorStore.add` + `similaritySearch` + `FilterExpressionBuilder` metadata filter
- 25：`docker-compose.override.yml` 提供 `redis/redis-stack-server:latest`（6380），README 明确禁止 core 普通 Redis
- 26：向量检索 + ES 全文 match + RRF 混合检索端点，README 说明 Spring AI 1.1.2 能力边界
- 五 Demo 均 `mvn -f examples/<demo>/pom.xml -q compile` 通过

## Task Commits

Each task was committed atomically:

1. **Task 1: 新建 22-embedding-demo** - `52ff993` (feat)
2. **Task 2: 新建 23-pgvector-demo** - `4ca5ae8` (feat)
3. **Task 3: 新建 24-milvus-demo** - `4f466d0` (feat)
4. **Task 4: 新建 25-redis-vector-demo** - `c221653` (feat)
5. **Task 5: 新建 26-es-hybrid-demo** - `3eb07fb` (feat)

**Plan metadata:** （本 commit）

## Files Created/Modified

- `examples/22-embedding-demo/` — EmbeddingModel 维度/成本基准，端口 18022，无中间件
- `examples/23-pgvector-demo/` — PGVector，端口 18023，`infra.sh up core`
- `examples/24-milvus-demo/` — Milvus，端口 18024，`infra.sh up vector`，响应含 score
- `examples/25-redis-vector-demo/` — Redis Stack，端口 18025，语义缓存 TTL，override 编排
- `examples/26-es-hybrid-demo/` — ES Hybrid，端口 18026，`infra.sh up search`

## Decisions Made

- **25 端口 6380 + override 编排**：与 core profile `redis:7.4-alpine`（6379）并存，避免误用普通 Redis
- **26 应用层 RRF**：Spring AI 1.1.2 `ElasticsearchVectorStore` 仅有 `similaritySearch`，全文通道用 `RestClient` + Jackson 安全编码 `match` 查询，再 RRF 融合
- **Filter 安全（T-23-01）**：department 等过滤值经 `FilterExpressionBuilder.eq` 传入，不拼接原生 SQL/filter 字符串

## Deviations from Plan

None - plan executed exactly as written.

（实现细节补齐属计划允许范围：22 用 `Result` 包装；23~26 统一增加 `/search/filter`；25 语义缓存端点；26 拆出 `HybridSearchService`。）

## Issues Encountered

None

## User Setup Required

真机运行需：

| Demo | 中间件 |
|------|--------|
| 22 | 无；`AI_DASHSCOPE_API_KEY` |
| 23 | `bash scripts/infra.sh up core` |
| 24 | `bash scripts/infra.sh up vector`（冷启动 30~60s） |
| 25 | `docker compose -f examples/25-redis-vector-demo/docker-compose.override.yml up -d`（`redis/redis-stack-server`，**禁止** core 普通 Redis） |
| 26 | `bash scripts/infra.sh up search` |

## Next Phase Readiness

- Embedding + 四库 VectorStore 教学底座就绪，后续 RAG demos（27~30）可复用同一配置约定（dimensions=1024、DashScope embedding、FilterExpressionBuilder）
- 未修改 STATE.md / ROADMAP.md（按执行指令）

## Self-Check: PASSED

- FOUND: `examples/22-embedding-demo/pom.xml`
- FOUND: `examples/23-pgvector-demo/pom.xml`
- FOUND: `examples/24-milvus-demo/pom.xml`
- FOUND: `examples/25-redis-vector-demo/pom.xml`
- FOUND: `examples/25-redis-vector-demo/docker-compose.override.yml`
- FOUND: `examples/26-es-hybrid-demo/pom.xml`
- FOUND commits: `52ff993`, `4ca5ae8`, `4f466d0`, `c221653`, `3eb07fb`
- ALL COMPILE OK（五模块）

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
