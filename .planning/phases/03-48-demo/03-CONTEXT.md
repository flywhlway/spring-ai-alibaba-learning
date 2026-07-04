# Phase 3: 48 个独立 Demo - Context

**Gathered:** 2026-07-04
**Status:** Ready for planning
**Mode:** `--auto`（用户已锁定 HANDOFF §1.4 / §3，并要求安全续接限额中断后的首批 01~08）

<domain>
## Phase Boundary

将教程各章核心 API 落成 `examples/` 下 48 个可独立 `mvn spring-boot:run` 的最小 Demo，编号/命名/端口以 `examples/README.md` 为 SSOT，满足 HANDOFF §3 验收与 §7 质量门禁。

**本执行周期（首批）范围锁定为 Demo 01~08：**
01-quickstart · 02-autoconfig · 03-multi-model · 04-chat · 05-retry · 06-prompt · 07-prompt-builder · 08-prompt-nacos

后续批次（09~48）属同一 Phase 3，但不在本周期交付；由后续 plan 覆盖。

</domain>

<decisions>
## Implementation Decisions

### 交付批次与续接策略
- **D-01:** 本周期只交付/验收 Demo 01~08（ROADMAP Plan priority note 首批）。
- **D-02:** 安全续接：已存在且结构完整的 Demo（01/02/03/04/06/07）以审计修齐为主，不重写；缺失的 05、空壳的 08 从教程规格新建。
- **D-03:** 限额中断残留的 09+ 半成品（11/12/14/15/16/17 等）本周期不触碰、不删除、不纳入验收。

### 工程约定（HANDOFF §1.4，不可偏离）
- **D-04:** 包根 `com.flywhl.saa.<模块>`，作者 `@author flywhl`。
- **D-05:** 端口 `examples/NN-xxx` → `180NN`；密钥仅 `${AI_DASHSCOPE_API_KEY}` / `${DEEPSEEK_API_KEY}`。
- **D-06:** 子模块 `pom.xml` parent 指向仓库父 POM，零版本号；双 BOM 由父 POM 管理。
- **D-07:** 禁用废弃 API：`PromptChatMemoryAdvisor`、`CallAroundAdvisor`/`AdvisedRequest`、`FunctionCallback`、可变 Options setter。
- **D-08:** 零 TODO / 零伪代码；每个 Demo 具备 README + `api.http` + REST + curl 与预期输出。

### common / starter 复用
- **D-09:** 教学最小链路 Demo（01 quickstart、02 autoconfig、03 multi-model）可按教程返回纯文本/自包含装配，不强行包 `Result`。
- **D-10:** 结构化 API Demo（04+，含 05/06/07/08）依赖 `saa-learning-common`，`@Import(GlobalExceptionHandler.class)`，统一 `Result<T>`。
- **D-11:** `saa-learning-starter`（审计/路由/成本）留给后续 advisor/routing/fallback 批次；首批不强制引入。

### 实现规格来源
- **D-12:** 每个 Demo 的接口/目录/配置以对应 `docs/tutorial/NN-*.md`「可运行 Demo」小节为权威规格；README 模板遵循 `examples/README.md` §3。
- **D-13:** 05-retry-demo 规格来自教程第 04 章 §4.8（`spring.ai.retry.*` + 可自定义 `RetryTemplate`）与 examples/README「重试、超时、错误处理策略」。
- **D-14:** 08-prompt-nacos-demo 规格来自教程第 05 章「可运行 Demo：Prompt 模板 + Nacos 热更新」（`ConfigurablePromptTemplateFactory`、默认模板兜底、`bash scripts/infra.sh up cloud`）。

### 测试与门禁（首批）
- **D-15:** 首批硬门禁：`mvn -pl common -am clean install` 后，各 Demo `mvn -f examples/NN-xxx/pom.xml -q compile` 全绿。
- **D-16:** 模型调用集成测试：首批至少为 05、08 各加一个 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")` 的冒烟 IT；其余 Demo 的 IT 可在后续 plan 补齐。
- **D-17:** examples 保持独立应用（parent `relativePath`），不强制挂入父 POM `<modules>`（与 HANDOFF §1.3 一致）。

### Claude's Discretion
- Retry Demo 的具体 REST 路径与「重试统计/策略可读」展示形式可自定，只要能演示 `spring.ai.retry.*` 与自定义 `RetryTemplate`。
- 既有 Demo 仅在违反 D-04~D-08 或无法编译时修改。

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
- `docs/tutorial/01-为什么需要SpringAIAlibaba.md` — 01-quickstart-demo
- `docs/tutorial/02-整体架构.md` — 架构验证入口（复用 01）
- `docs/tutorial/03-AutoConfiguration.md` — 02-autoconfig-demo
- `docs/tutorial/04-ChatClient.md` — 03/04/05（§4.8 retry）
- `docs/tutorial/05-Prompt.md` — 06/07/08（Nacos 热更新完整代码）

### 版本与 ADR
- `docs/00-overview/02-版本调研报告.md` — Boot 3.5.16 / SAA 1.1.2.2
- `docs/00-overview/04-技术选型ADR.md` — ADR-001~006

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `saa-learning-common`：`Result<T>`、`BizException`、`GlobalExceptionHandler`（04+ Demo 已 `@Import`）
- `saa-learning-starter`：审计 Advisor / ModelRouter / CostRecorder（首批不用）
- 父 POM 双 BOM：`spring-ai-alibaba-bom` + `spring-ai-alibaba-extensions-bom`

### Established Patterns（已落盘 Demo）
- parent：`com.flywhl.saa:spring-ai-alibaba-learning:1.0.0-SNAPSHOT` + `relativePath=../../pom.xml`
- DashScope：`spring-ai-alibaba-starter-dashscope`，密钥 `${AI_DASHSCOPE_API_KEY}`
- 结构化 Demo：`Result.ok(...)` + `@Import(GlobalExceptionHandler.class)`
- README + `api.http` 成对交付

### 续接盘点（2026-07-04）
| Demo | 状态 |
|------|------|
| 01-quickstart-demo | 完整（pom/README/api.http/yml/java） |
| 02-autoconfig-demo | 完整 |
| 03-multi-model-demo | 完整 |
| 04-chat-demo | 完整（common + Result） |
| 05-retry-demo | **缺失** |
| 06-prompt-demo | 完整 |
| 07-prompt-builder-demo | 完整 |
| 08-prompt-nacos-demo | **空壳**（仅空 src 目录） |

### Integration Points
- 08 依赖 `bash scripts/infra.sh up cloud`（Nacos）
- 03 额外需要 `DEEPSEEK_API_KEY`
- common 须先 `mvn -pl common -am install`

</code_context>

<specifics>
## Specific Ideas

- 用户明确要求：按相位循环推进 Phase 3 第一批（01~08），严格遵守 HANDOFF §1.4 与 §3。
- 任务在 Claude Code 限额中断后续接，不得破坏已完成文件。
- `--next --auto`：discuss → plan → execute 自动链式推进，直至首批完成或遇阻塞决策。

</specifics>

<deferred>
## Deferred Ideas

- Demo 09~48（advisor/tool/memory/RAG/MCP/Agent/Graph/…）→ Phase 3 后续 plan
- 限额中断残留的 11~17 半成品收尾 → 后续批次 plan
- 全量 Demo 的集成测试补齐、version-audit / spring-ai-2-readiness 全仓门禁 → Phase 3 收口或 Phase 7
- 父 POM `<modules>` 挂载 examples → 按需，非首批必须

</deferred>

---

*Phase: 3-48 个独立 Demo*
*Context gathered: 2026-07-04*
*Auto decisions log:*
- `[auto] Selected all gray areas: 交付批次与续接策略, common/starter 复用, 实现规格来源, 测试与门禁`
- `[auto] 交付批次 — Q: 本周期范围？ → Selected: 仅 01~08（recommended）`
- `[auto] 续接 — Q: 如何处理已有文件？ → Selected: 审计修齐，不重写（recommended）`
- `[auto] common — Q: 是否全部强制 Result？ → Selected: 01~03 按教程最小形态，04+ 用 common（recommended）`
- `[auto] starter — Q: 首批是否引入？ → Selected: 否，留给后续批次（recommended）`
- `[auto] IT — Q: 首批测试深度？ → Selected: 编译硬门禁 + 05/08 冒烟 IT（recommended）`
)
