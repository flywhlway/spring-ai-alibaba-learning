# Phase 3: 48 个独立 Demo - Context

**Gathered:** 2026-07-04
**Status:** Ready for planning (batch 3)
**Mode:** `--auto`（再次批：Agent / Graph / Multi-Agent / best-practice，剩余全部 Demo）

<domain>
## Phase Boundary

将教程各章核心 API 落成 `examples/` 下 48 个可独立 `mvn spring-boot:run` 的最小 Demo，编号/命名/端口以 `examples/README.md` 为 SSOT，满足 HANDOFF §3 验收与 §7 质量门禁。

**本执行周期（再次批 / batch 3）范围锁定为 Demo 35~48（剩余全部）：**
35-agent · 36-agent-skills · 37-agent-hitl · 38-workflow · 39-graph-parallel · 40-graph-saga · 41-multi-agent · 42-supervisor · 43-a2a-nacos · 44-stream · 45-observability · 46-logging · 47-routing · 48-fallback

首批 01~19（plans 03-01/02/03）与次批 20~34（plans 03-04~08）已交付，本周期不修改。本批完成后 Phase 3 清单 48/48 齐备，可进入全量验收。

</domain>

<decisions>
## Implementation Decisions

### 交付批次与续接策略
- **D-01:** 本周期只交付/验收 Demo 35~48（ROADMAP Plan priority note 再次+末批合并；用户明确「剩余所有 demo」）。
- **D-02:** 01~34 已交付且 compile gate 通过，本周期不触碰、不重写、不纳入本批验收。
- **D-03:** 35~48 当前均不存在，全部从教程规格新建（非审计修齐）。

### 工程约定（HANDOFF §1.4，不可偏离）
- **D-04:** 包根 `com.flywhl.saa.<模块>`，作者 `@author flywhl`。
- **D-05:** 端口 `examples/NN-xxx` → `180NN`；Server/Client 配对时 Client = Server+100（43-a2a-nacos 若双进程：Server 18043 / Client 18143）。
- **D-06:** 子模块 `pom.xml` parent 指向仓库父 POM，零版本号；双 BOM 由父 POM 管理。
- **D-07:** 禁用废弃 API：`PromptChatMemoryAdvisor`、`CallAroundAdvisor`/`AdvisedRequest`/`AdvisedResponse`、`FunctionCallback`、可变 Options setter。
- **D-08:** 零 TODO / 零伪代码；每个 Demo 具备 README + `api.http` + REST + curl 与预期输出。
- **D-08b:** 小 DTO/record 优先与使用方同包，勿盲目拆 `model` 子包（CLAUDE.md / saa-conventions）。

### common / starter 复用
- **D-09:** 35~48 全部依赖 `saa-learning-common`，`@Import(GlobalExceptionHandler.class)`，统一 `Result<T>`（与 04+ 一致）。
- **D-10:** `saa-learning-starter`（审计/路由/成本）在 **44~48 best-practice 批次强制引入**（与次批 D-10 对照：次批不引入，本批 44~48 必须复用 starter，不重复实现 ModelRouter/CostRecorder/AuditLoggingAdvisor）。
- **D-11:** Chat/Agent 模型一律 DashScope（ADR-003）；多模型路由 Demo（47）可额外引入 DeepSeek（`DEEPSEEK_API_KEY`），与 03-multi-model 模式一致。

### 实现规格来源
- **D-12:** 每个 Demo 的接口/目录/配置以对应 `docs/tutorial/NN-*.md`「可运行 Demo」小节为权威规格；README 模板遵循 `examples/README.md` §3。
- **D-13:** 章节映射（examples/README SSOT）：
  - 35/36/37 → `docs/tutorial/13-Agent.md`
  - 38/39/40 → `docs/tutorial/14-Workflow.md`
  - 41/42/43 → `docs/tutorial/15-MultiAgent.md`
  - 44 → `docs/tutorial/17-Streaming.md`
  - 45/46 → `docs/tutorial/18-Observability.md`
  - 47/48 → `docs/tutorial/20-企业实践.md`（路由/降级；starter 装配可参考 `docs/tutorial/19-BestPractice.md`）
- **D-14:** 教程若只给部分 Demo 完整代码，其余 Demo 按同章 API 与 examples/README 演示要点补齐最小可运行形态，风格对齐已交付的 09~34。

### 中间件与 infra profile
- **D-15:** 中间件依赖在各 Demo README 顶部声明 `bash scripts/infra.sh up <profiles>`：
  - 35~42 Agent/Graph/Multi-Agent → 默认无中间件（进程内）；若实现依赖 Redis checkpoint / 记忆则声明 `core`
  - 43-a2a-nacos → `cloud`（Nacos）
  - 44 stream → 无
  - 45 observability → 可选 `core` 若挂 Prometheus 本地抓取说明；默认无强制中间件，README 写清 metrics 端点
  - 46 logging → 无
  - 47/48 routing/fallback → 无（多模型 Key）
- **D-16:** 无中间件的 Demo README 写「无」。

### 测试与门禁（再次批）
- **D-17:** 再次批硬门禁：`mvn -pl common,starter -am clean install` 后，35~48 各 Demo `mvn -f examples/NN-xxx/pom.xml -q compile` 全绿。
- **D-18:** 模型调用冒烟 IT：至少为 35、38、41、44、47 各加一个 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")` 的 IT；中间件 Demo（43）用文档声明手动 infra（与 34 一致）。
- **D-19:** examples 保持独立应用（parent `relativePath`），不强制挂入父 POM `<modules>`。

### Plan 分组（供 planner）
- **D-20:** 建议按能力域拆 plan，便于并行 wave：
  1. Agent（35~37）
  2. Graph / Workflow（38~40）
  3. Multi-Agent（41~43，含 A2A/Nacos）
  4. Stream + Observability + Logging（44~46）
  5. Routing + Fallback（47~48，强制 starter）
  6. 再次批编译门禁（35~48 compile 全绿 + 约定扫描）
- **D-21:** 新 plan 编号从 `03-09` 起，**不覆盖**已交付的 `03-01`~`03-08`。

### Claude's Discretion
- 教程未给出完整代码的 Demo（如 36/37/40/46）按同章 API 与相邻 Demo 模式补齐。
- Agent/Graph 依赖坐标以父 POM BOM 实际 artifact 为准（ReactAgent / StateGraph 等 SAA Graph API）。
- HITL（37）用最小「暂停→人工确认→恢复」REST 演示即可。
- Saga（40）用最小补偿节点演示，不引入外部事务中间件。
- 45 observability：Micrometer 指标暴露 `/actuator/prometheus`；Grafana 看板可文档化，不强求 compose 内嵌 Grafana。
- 47/48：优先复用 `saa-learning-starter` 的 `ModelRouter` / 成本与降级策略，Demo 只做装配与 REST 演示层。

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
- `docs/tutorial/13-Agent.md` — 35/36/37
- `docs/tutorial/14-Workflow.md` — 38/39/40
- `docs/tutorial/15-MultiAgent.md` — 41/42/43
- `docs/tutorial/17-Streaming.md` — 44
- `docs/tutorial/18-Observability.md` — 45/46
- `docs/tutorial/19-BestPractice.md` — starter 装配参考（44~48）
- `docs/tutorial/20-企业实践.md` — 47/48

### 已交付参考（模式对齐）
- `examples/04-chat-demo/` — common + Result 模式
- `examples/11-tool-demo/` — @Tool 模式（Agent tools）
- `examples/16-memory-demo/` — Advisor / Memory 装配
- `examples/34-mcp-nacos-demo/` — 双模块 + Nacos + 端口偏移
- `starter/` — AuditLoggingAdvisor / ModelRouter / CostRecorder（44~48 复用）
- `.planning/phases/03-48-demo/03-04-PLAN.md` ~ `03-08-PLAN.md` — plan 结构参考（次批）

### 版本与 ADR
- `docs/00-overview/02-版本调研报告.md` — Boot 3.5.16 / SAA 1.1.2.2
- `docs/00-overview/04-技术选型ADR.md` — ADR-001~006
- `docker/docker-compose.yml` — infra profiles: core / vector / mq / search / cloud

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `saa-learning-common`：`Result<T>`、`BizException`、`GlobalExceptionHandler`（04+ Demo 已 `@Import`）
- `saa-learning-starter`：审计 Advisor / ModelRouter / CostRecorder（**本批 44~48 强制复用**）
- 父 POM 双 BOM：`spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom`

### Established Patterns（已落盘 Demo 01~34）
- parent：`com.flywhl.saa:spring-ai-alibaba-learning:1.0.0-SNAPSHOT` + `relativePath=../../pom.xml`
- DashScope：`spring-ai-alibaba-starter-dashscope`，密钥 `${AI_DASHSCOPE_API_KEY}`
- 结构化 Demo：`Result.ok(...)` + `@Import(GlobalExceptionHandler.class)`
- README + `api.http` 成对交付
- 端口 180NN；独立 Maven 工程
- 双模块 Demo（34）：子目录 server/client，Client 端口 +100

### 再次批盘点（2026-07-04）
| Demo | 状态 |
|------|------|
| 35~48 全部 | **缺失**（需新建） |
| 01~34 | 已交付，本周期不触碰 |

### Integration Points
- 43 依赖 `bash scripts/infra.sh up cloud`（Nacos）
- 35~42 默认无中间件；可选 Redis checkpoint 时声明 `core`
- 47 可能需 `DEEPSEEK_API_KEY`（多模型路由）
- common + starter 须先 `mvn -pl common,starter -am install`

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求：`/gsd-plan-phase 3 --auto` 规划再次批 35~48（剩余所有 demo）。
- ROADMAP Plan priority note 第 3+4 项合并为本周期。
- 首批/次批 plans 03-01~08 保留，新 plan 从 03-09 起。
- `--auto`：research → plan → verify → execute 自动链式推进。

</specifics>

<deferred>
## Deferred Ideas

- 全量 Demo 的集成测试补齐、version-audit / spring-ai-2-readiness 全仓门禁 → Phase 3 收口或 Phase 7
- 父 POM `<modules>` 挂载 examples → 按需，非本批必须
- Phase 3 全量 VERIFICATION.md（48/48）→ 本批 execute + UAT 之后

</deferred>

---

*Phase: 3-48 个独立 Demo*
*Context gathered: 2026-07-04 (batch 3 refresh — demos 35~48)*
*Auto decisions log:*
- `[auto] Batch scope — Q: 本周期范围？ → Selected: 35~48 全部剩余（再次+末批合并）`
- `[auto] Existing 01~34 — Q: 如何处理？ → Selected: 不触碰（recommended）`
- `[auto] common — Q: 是否全部 Result？ → Selected: 35~48 全部用 common（recommended）`
- `[auto] starter — Q: 是否引入？ → Selected: 44~48 强制复用 starter（ROADMAP 末批 + 次批 D-10 对照）`
- `[auto] IT — Q: 测试深度？ → Selected: 编译硬门禁 + 35/38/41/44/47 冒烟 IT（recommended）`
- `[auto] Plans — Q: 编号策略？ → Selected: 从 03-09 起，不覆盖 01~08`
)
