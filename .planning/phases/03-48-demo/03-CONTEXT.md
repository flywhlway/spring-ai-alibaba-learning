# Phase 3: 48 个独立 Demo - Context

**Gathered:** 2026-07-04
**Status:** Ready for planning (batch 2)
**Mode:** `--auto`（次批 RAG / Embedding / VectorStore / MCP）

<domain>
## Phase Boundary

将教程各章核心 API 落成 `examples/` 下 48 个可独立 `mvn spring-boot:run` 的最小 Demo，编号/命名/端口以 `examples/README.md` 为 SSOT，满足 HANDOFF §3 验收与 §7 质量门禁。

**本执行周期（次批 / batch 2）范围锁定为 Demo 20~34：**
20-structured-output · 21-json-schema · 22-embedding · 23-pgvector · 24-milvus · 25-redis-vector · 26-es-hybrid · 27-rag · 28-advanced-rag · 29-hybrid-rag · 30-rag-eval · 31-mcp-server · 32-mcp-client · 33-mcp-auth · 34-mcp-nacos

首批 01~19 已交付（plans 03-01/02/03），本周期不修改。再次批 35~48 属同一 Phase 3，由后续 plan 覆盖。

</domain>

<decisions>
## Implementation Decisions

### 交付批次与续接策略
- **D-01:** 本周期只交付/验收 Demo 20~34（ROADMAP Plan priority note 次批）。
- **D-02:** 01~19 已交付且 UAT complete，本周期不触碰、不重写、不纳入验收。
- **D-03:** 20~34 当前均不存在，全部从教程规格新建（非审计修齐）。

### 工程约定（HANDOFF §1.4，不可偏离）
- **D-04:** 包根 `com.flywhl.saa.<模块>`，作者 `@author flywhl`。
- **D-05:** 端口 `examples/NN-xxx` → `180NN`；Server/Client 配对时 Client = Server+100（34-mcp-nacos：Server 18034 / Client 18134）。
- **D-06:** 子模块 `pom.xml` parent 指向仓库父 POM，零版本号；双 BOM 由父 POM 管理。
- **D-07:** 禁用废弃 API：`PromptChatMemoryAdvisor`、`CallAroundAdvisor`/`AdvisedRequest`/`AdvisedResponse`、`FunctionCallback`、可变 Options setter。
- **D-08:** 零 TODO / 零伪代码；每个 Demo 具备 README + `api.http` + REST + curl 与预期输出。

### common / starter 复用
- **D-09:** 20~34 全部依赖 `saa-learning-common`，`@Import(GlobalExceptionHandler.class)`，统一 `Result<T>`（与首批 04+ 一致）。
- **D-10:** `saa-learning-starter`（审计/路由/成本）留给 44~48 best-practice 批次；本批不强制引入。
- **D-11:** Embedding 一律 DashScope（ADR-003），保证向量空间一致；不混用其他 Embedding 提供商。

### 实现规格来源
- **D-12:** 每个 Demo 的接口/目录/配置以对应 `docs/tutorial/NN-*.md`「可运行 Demo」小节为权威规格；README 模板遵循 `examples/README.md` §3。
- **D-13:** 章节映射（examples/README SSOT）：
  - 20/21 → `docs/tutorial/16-StructuredOutput.md`
  - 22 → `docs/tutorial/10-Embedding.md`
  - 23/24/25/26 → `docs/tutorial/11-VectorStore.md`
  - 27/28/29/30 → `docs/tutorial/09-RAG.md`
  - 31/32/33/34 → `docs/tutorial/12-MCP.md`
- **D-14:** 教程若只给部分 Demo 完整代码，其余 Demo 按同章 API 与 examples/README 演示要点补齐最小可运行形态，风格对齐已交付的 09~19。

### 中间件与 infra profile
- **D-15:** 中间件依赖在各 Demo README 顶部声明 `bash scripts/infra.sh up <profiles>`：
  - 23-pgvector → `core`（PostgreSQL）
  - 24-milvus → `vector`（Milvus + etcd + MinIO）
  - 25-redis-vector → `core`（**必须** `redis/redis-stack-server`，非普通 redis）
  - 26-es-hybrid → `search`（Elasticsearch）
  - 27~30 RAG → 按实现选型声明 `vector` 和/或 `core`（默认 Milvus 作主向量库，与 ADR-004 / Phase 4 对齐）
  - 31~33 MCP → 无中间件（进程内 / HTTP）
  - 34-mcp-nacos → `cloud`（Nacos）
- **D-16:** 无中间件的 Demo（20/21/22）README 写「无」。

### 测试与门禁（次批）
- **D-17:** 次批硬门禁：`mvn -pl common -am clean install` 后，20~34 各 Demo `mvn -f examples/NN-xxx/pom.xml -q compile` 全绿。
- **D-18:** 模型调用冒烟 IT：至少为 20、22、27、31 各加一个 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")` 的 IT；中间件 Demo（23~26）用 Testcontainers 或文档声明手动 infra（与首批 08/17/18 一致，不强求全量 Testcontainers）。
- **D-19:** examples 保持独立应用（parent `relativePath`），不强制挂入父 POM `<modules>`。

### Plan 分组（供 planner）
- **D-20:** 建议按能力域拆 plan，便于并行 wave：
  1. Structured Output（20~21）
  2. Embedding + VectorStore（22~26）
  3. RAG（27~30）
  4. MCP（31~34，含 Server/Client 端口偏移）
  5. 次批编译门禁（20~34 compile 全绿）
- **D-21:** 新 plan 编号从 `03-04` 起，**不覆盖**已交付的 `03-01`/`03-02`/`03-03`。

### Claude's Discretion
- 教程未给出完整代码的 Demo（如 21/25/30/33）按同章 API 与相邻 Demo 模式补齐。
- RAG 默认向量后端选型（Milvus vs pgvector）可按教程示例与 docker profile 可用性决定，须在 README 声明。
- MCP Server 传输优先 Streamable HTTP（教程规格），认证 Demo（33）用最小 API Key / Bearer 演示即可。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 交接与约定
- `HANDOFF-TO-CLAUDE-CODE.md` §1.4 — 全局约定（包根/端口/禁用 API/密钥）
- `HANDOFF-TO-CLAUDE-CODE.md` §3 — Phase 3 验收标准
- `HANDOFF-TO-CLAUDE-CODE.md` §7 — 质量门禁
- `CLAUDE.md` — 版本锁定与硬约定
- `.claude/skills/saa-conventions/SKILL.md` — 工程约定 skill

### Demo 清单与教程规格
- `examples/README.md` — Demo 编号/命名/端口 SSOT
- `docs/tutorial/16-StructuredOutput.md` — 20/21
- `docs/tutorial/10-Embedding.md` — 22
- `docs/tutorial/11-VectorStore.md` — 23/24/25/26
- `docs/tutorial/09-RAG.md` — 27/28/29/30
- `docs/tutorial/12-MCP.md` — 31/32/33/34

### 已交付参考（模式对齐）
- `examples/04-chat-demo/` — common + Result 模式
- `examples/11-tool-demo/` — @Tool 模式（MCP 工具暴露可参考）
- `examples/16-memory-demo/` — Advisor 装配模式
- `.planning/phases/03-48-demo/03-01-PLAN.md` ~ `03-03-PLAN.md` — plan 结构参考

### 版本与 ADR
- `docs/00-overview/02-版本调研报告.md` — Boot 3.5.16 / SAA 1.1.2.2
- `docs/00-overview/04-技术选型ADR.md` — ADR-001~006（Embedding=DashScope；VectorStore 教学全覆盖）
- `docker/docker-compose.yml` — infra profiles: core / vector / mq / search / cloud

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `saa-learning-common`：`Result<T>`、`BizException`、`GlobalExceptionHandler`（04+ Demo 已 `@Import`）
- `saa-learning-starter`：审计 Advisor / ModelRouter / CostRecorder（本批不用）
- 父 POM 双 BOM：`spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom`

### Established Patterns（已落盘 Demo 01~19）
- parent：`com.flywhl.saa:spring-ai-alibaba-learning:1.0.0-SNAPSHOT` + `relativePath=../../pom.xml`
- DashScope：`spring-ai-alibaba-starter-dashscope`，密钥 `${AI_DASHSCOPE_API_KEY}`
- 结构化 Demo：`Result.ok(...)` + `@Import(GlobalExceptionHandler.class)`
- README + `api.http` 成对交付
- 端口 180NN；独立 Maven 工程

### 次批盘点（2026-07-04）
| Demo | 状态 |
|------|------|
| 20~34 全部 | **缺失**（需新建） |
| 01~19 | 已交付，本周期不触碰 |

### Integration Points
- 23/25 依赖 `bash scripts/infra.sh up core`
- 24 依赖 `bash scripts/infra.sh up vector`（Milvus 冷启动 30~60s）
- 26 依赖 `bash scripts/infra.sh up search`
- 27~30 依赖向量库 profile（默认 vector）
- 34 依赖 `bash scripts/infra.sh up cloud`（Nacos）
- common 须先 `mvn -pl common -am install`

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求：`/gsd-plan-phase 3 --auto` 规划次批 RAG / Embedding / VectorStore / MCP（20~34）。
- ROADMAP Plan priority note 第 2 项。
- 首批 01~19 UAT complete；plans 03-01/02/03 保留，新 plan 从 03-04 起。
- `--auto`：research → plan → verify → execute 自动链式推进。

</specifics>

<deferred>
## Deferred Ideas

- Demo 35~48（Agent / Graph / Multi-Agent / best-practice）→ Phase 3 再次/末批 plan
- 全量 Demo 的集成测试补齐、version-audit / spring-ai-2-readiness 全仓门禁 → Phase 3 收口或 Phase 7
- 父 POM `<modules>` 挂载 examples → 按需，非本批必须
- starter 在 44~48 批次引入

</deferred>

---

*Phase: 3-48 个独立 Demo*
*Context gathered: 2026-07-04 (batch 2 refresh)*
*Auto decisions log:*
- `[auto] Batch scope — Q: 本周期范围？ → Selected: 仅 20~34（ROADMAP 次批）`
- `[auto] Existing 01~19 — Q: 如何处理？ → Selected: 不触碰（recommended）`
- `[auto] common — Q: 是否全部 Result？ → Selected: 20~34 全部用 common（recommended）`
- `[auto] starter — Q: 是否引入？ → Selected: 否，留给 44~48（recommended）`
- `[auto] Embedding — Q: 提供商？ → Selected: 一律 DashScope（ADR-003）`
- `[auto] IT — Q: 测试深度？ → Selected: 编译硬门禁 + 20/22/27/31 冒烟 IT（recommended）`
- `[auto] Plans — Q: 编号策略？ → Selected: 从 03-04 起，不覆盖 01~03`
)
