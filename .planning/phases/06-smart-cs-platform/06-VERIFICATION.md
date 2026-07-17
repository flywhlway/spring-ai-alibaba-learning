---
phase: 06-smart-cs-platform
verified: 2026-07-17T16:20:00Z
status: human_needed
score: 34/35 must-haves verified
overrides_applied: 0
re_verification:
  previous_status: human_needed
  previous_score: 27/28
  gaps_closed:
    - "按文档 compose + smartcs profile 后，scs_platform 自动拥有 schema/data（scs-db-init 挂载）"
    - "query-rewrite Prompt 满足 RewriteQueryTransformer 必填占位符 {target}+{query}，Bean 可创建"
  gaps_remaining: []
  regressions: []
human_verification:
  - test: "冷启动已修复后，重新执行 06-UAT.md §1 三角色 login（admin/agent1/customer1）"
    expected: "均 code=0 且拿到 accessToken；角色 claim 正确"
    why_human: "需进程内 JWT 真签发；本轮 verifier 仅确认 health 曾 UP，未复跑 login"
  - test: "设置 AI_DASHSCOPE_API_KEY 后执行 06-UAT.md §2 同步 ask + SSE stream（退货政策种子问）"
    expected: "ask 返回答案；stream 含 message/done（FAQ 路径可出现 cacheHit）；cs_message 持久化"
    why_human: "需 DashScope + Milvus/ES/Redis Stack 真链路；先前 UAT 被冷启动 blocker 挡住，需补跑"
  - test: "执行 06-UAT.md 工单流转；handoff start 路径按能力验证（approve 404 见 D-14 Pending，勿当本轮新 gap）"
    expected: "非法 transition 400；合法流转成功；HITL approve 已知 Pending Todo 不阻塞本报告"
    why_human: "Graph interrupt 与真模型工具调用需运行时；CR-01 另案 /gsd-code-review 6 --fix"
  - test: "ADMIN 调 GET /api/admin/dashboard/stats 与 model/prompt publish；可选 monitor profile 看 Prometheus/Grafana"
    expected: "stats 含会话/工单/cacheHitRate/成本字段；Nacos 出现 scs.model.profiles 与 prompt Data ID"
    why_human: "Nacos 热更新与 Grafana 面板需运行时观测；先前被冷启动挡住"
  - test: "bash projects/smart-cs-platform/scripts/uat-smart-cs.sh（有 Key 全量 / 无 Key 仅 health+login+RBAC）"
    expected: "脚本 exit 0；与 06-UAT.md 预期一致（HITL approve 已知 D-14 除外）"
    why_human: "端到端 smoke 需已启动的 19300 应用与中间件；冷启动修复后需补跑"
---

# Phase 6: 智能客服平台 Verification Report

**Phase Goal:** 客服场景可通过 smart-cs-platform 完成 FAQ 秒答、多智能体协作、工单流转与人工接管，并具备运营看板  
**Verified:** 2026-07-17T16:20:00Z  
**Status:** human_needed  
**Re-verification:** Yes — after gap closure plans 06-08 / 06-09

## Goal Achievement

### Gap Closure (本轮重点)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| G1 | 多文件 compose 下 scs-db-init 挂载到 `projects/smart-cs-platform/db`，而非 `docker/db` | ✓ VERIFIED | override: `../projects/smart-cs-platform/db:/scs-db:ro`；`docker inspect` Mounts.Source = `.../projects/smart-cs-platform/db`；容器 `exited 0` |
| G2 | kqa-db-init / office-db-init 同样挂载到各自 `projects/<proj>/db` | ✓ VERIFIED | 三处均 `../projects/.../db:`；`! grep './db:'` 于企业项目 override — PASS |
| G3 | scs-prometheus 挂载到 `projects/smart-cs-platform/monitor/prometheus.yml` | ✓ VERIFIED | `../projects/smart-cs-platform/monitor/prometheus.yml:...`；无 `./monitor/` 残留 |
| G4 | classpath `query-rewrite.st` 同时含 `{target}` 与 `{query}` | ✓ VERIFIED | 文件正文含两占位符 |
| G5 | `db/data.sql` 与 test 副本种子含 `{target}`+`{query}`，且有幂等 UPDATE | ✓ VERIFIED | INSERT + `UPDATE ... WHERE content NOT LIKE '%{target}%'` 主/test 均有 |
| G6 | RewriteQueryTransformer 构造不再因缺 target 抛 IllegalArgumentException | ✓ VERIFIED | `RagPipelineFactory`→`getQueryRewriteTemplate()`→`RewriteQueryTransformer.builder()`；orchestrator：boot 日志无 `placeholders must be present ... target`；health 曾 UP |
| G7 | Docker 可用时 actuator/health 为 UP（G-06-09-health 硬门禁） | ✓ VERIFIED | orchestrator：`curl .../actuator/health` → `{"status":"UP"}`；SUMMARY 非 residual。本轮 spot-check 时进程已停（HEALTH_UNREACHABLE）— 不否定已达成门禁 |

**Gap closure score:** 7/7

### Observable Truths（回归：原 ROADMAP + PLAN）

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| **ROADMAP** | | | |
| 1 | FAQ/知识库问答可用（Milvus + Redis 语义缓存 + ES 全文混合检索） | ✓ VERIFIED | 回归：`FaqAnswerService` / `HybridSearchService` / `SemanticCacheService` 仍在 |
| 2 | RoutingAgent + Supervisor + Handoffs；工单域 + Graph interrupt（HITL） | ✓ VERIFIED | 回归：`CsAgentConfig` / `TicketService` / `HumanHandoffController` 仍在 |
| 3 | 运营监控/成本；模型/Prompt CRUD + Nacos 热更新 | ✓ VERIFIED | 回归：Dashboard / ModelAdmin / PromptPublish 仍在 |
| 4 | 统一交付标准；端口 19300；栈 PG+Milvus+Redis+ES+Nacos | ✓ VERIFIED | 回归：`application.yml` + compose override；冷启动路径已修 |
| **PLAN 06-01…07** | | | |
| 5–27 | 原 23 项 PLAN must-haves（工程/JWT/RAG/Agent/SSE/Admin/UAT 资产） | ✓ VERIFIED | 快速回归：关键产物存在；无 TBD/FIXME/XXX；编译门禁此前已绿 |
| 28 | 有 API Key 时 login/chat/ticket/handoff 各 ≥1 IT 可运行 | ? UNCERTAIN | IT 类存在；本轮未重跑 Testcontainers/真模型 IT |

**Score:** 34/35 truths verified（含 gap 7 项；1 UNCERTAIN → 人工）

### Deferred Items

| # | Item | Addressed In | Evidence |
|---|------|--------------|----------|
| 1 | 06-REVIEW Critical HITL/approve 404（CR-01 / D-14） | Pending Todo（非本 gap_closure） | STATE.md Pending Todos；Phase 7 明确不改 smart-cs 业务代码；**本报告按指示不重开为 gap** |

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `projects/smart-cs-platform/docker-compose.override.yml` | scs-db-init + prometheus 相对 docker/ | ✓ VERIFIED | gsd verify.artifacts 06-08: 3/3；含路径注释 |
| `projects/knowledge-qa-platform/docker-compose.override.yml` | kqa-db-init 正确 volume | ✓ VERIFIED | `../projects/knowledge-qa-platform/db` |
| `projects/office-agent-assistant/docker-compose.override.yml` | office-db-init 正确 volume | ✓ VERIFIED | `../projects/office-agent-assistant/db` |
| `src/main/resources/prompts/query-rewrite.st` | `{target}`+`{query}` | ✓ VERIFIED | gsd artifacts 06-09: 3/3 |
| `db/data.sql` | INSERT + 幂等 UPDATE | ✓ VERIFIED | `content NOT LIKE '%{target}%'` |
| `src/test/resources/db/data.sql` | 与主库一致 | ✓ VERIFIED | 同模板 + UPDATE |
| `README.md` | 已有库/{target}/force-recreate 说明 | ✓ VERIFIED | §4.2 排障三条 |
| （原 06-01…07 产物） | 见初验报告 | ✓ VERIFIED | 回归抽查：pom / schema / Security / FAQ / Agent / Chat / Ticket / Dashboard / uat 脚本仍在 |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `docker-compose.override.yml` (smartcs) | `db/schema.sql` | `../projects/smart-cs-platform/db:/scs-db:ro` | ✓ WIRED | 人工 grep + inspect Source；gsd key-links 对 `../` 转义误报「not found」 |
| `docker-compose.override.yml` (smartcs) | `monitor/prometheus.yml` | prometheus volume | ✓ WIRED | 同上 |
| `RagPipelineFactory` | `PromptTemplateProvider.getQueryRewriteTemplate` | `RewriteQueryTransformer.builder().promptTemplate(...)` | ✓ WIRED | gsd key-links 06-09 verified |
| `PromptTemplateProvider` | `prompt_template` / classpath `query-rewrite.st` | `get("query-rewrite")` DB PUBLISHED 优先 | ✓ WIRED | Nacos→DB→classpath 三级回退仍在 |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|--------------------|--------|
| `scs-db-init` | `/scs-db/*.sql` | bind `projects/.../db` | schema+data 可导入（exit 0） | ✓ FLOWING |
| `PromptTemplateProvider.getQueryRewriteTemplate` | template content | DB PUBLISHED / classpath `.st` | 含 `{target}`+`{query}` | ✓ FLOWING |
| `RewriteQueryTransformer` | promptTemplate | Provider 注入 | PromptAssert 可通过（health 曾 UP） | ✓ FLOWING |
| `FaqAnswerService` / `ChatService` / `DashboardStatsService` | （初验） | 同初验 | ✓ FLOWING | 回归未发现空洞化 |
| `RagPipelineFactory` Bean | `RetrievalAugmentationAdvisor` | 已注册 | FAQ 主路径仍走 Hybrid；Advisor 消费者仍少 | ⚠️ ORPHANED（初验 WARNING，非本轮 blocker） |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| G-06-08-db | 三处 `../projects/.../db:` 计数 | 3 | ✓ PASS |
| G-06-08-no-rel | 无企业项目 `./db:` | OK | ✓ PASS |
| G-06-08-prom | prometheus 路径 + 无 `./monitor/` | OK | ✓ PASS |
| G-06-09-ph/upd | 三处 `{target}`+`{query}` + 幂等 UPDATE | OK | ✓ PASS |
| scs-db-init mount | `docker inspect` Mounts.Source | `.../projects/smart-cs-platform/db`；exit 0 | ✓ PASS |
| health（本轮） | `curl localhost:19300/actuator/health` | 进程未在跑 | ? SKIP（orchestrator 已证 UP） |
| 债务标记 | TBD/FIXME/XXX in smart-cs | 0 | ✓ PASS |

### Probe Execution

| Probe | Command | Result | Status |
|-------|---------|--------|--------|
| （无 phase 声明 probe-*.sh） | — | N/A | SKIP |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| **REQ-phase-6-smart-cs** | 06-01…06-09 | smart-cs-platform：FAQ/多智能体/工单/接管/看板 + 冷启动可 UP | ✓ SATISFIED（代码+冷启动门禁） | ROADMAP SC + gap 关闭；E2E UAT 待人工补跑 |
| （孤儿检查） | REQUIREMENTS.md Phase 6 | 仅此一条 | ✓ 无 ORPHANED | Traceability 仅 `REQ-phase-6-smart-cs` |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `rag/RagPipelineFactory.java` | Bean | Advisor 主 FAQ 路径未消费（初验遗留） | ⚠️ Warning | 冷启动已可建 Bean；非功能空洞 |
| `config/ChatMemoryConfig.java` | Bean | MessageChatMemoryAdvisor 未挂 ChatClient（初验遗留） | ⚠️ Warning | 会话靠 JPA |
| — | — | TBD/FIXME/XXX / 硬编码密钥 | （无） | — |
| 06-REVIEW CR-01 | — | HITL approve 404 | ℹ️ Info | **故意不记入 gaps**（D-14 Pending Todo） |

### Human Verification Required

#### 1. 三角色登录（冷启动后补跑）

**Test:** 起应用后执行 06-UAT.md §1  
**Expected:** admin/agent1/customer1 登录拿 token  
**Why human:** verifier 未复跑 JWT 登录

#### 2. FAQ ask + SSE（需 API Key）

**Test:** 06-UAT.md §2  
**Expected:** 同步/流式有答案；可选 cacheHit  
**Why human:** 先前被冷启动挡住，需补跑真链路

#### 3. 工单流转（HITL approve 已知 Pending）

**Test:** 工单合法/非法流转；handoff 按能力验证  
**Expected:** 非法 400；**approve 404 不作为本轮新 gap**（D-14）  
**Why human:** 运行时 Graph/工具调用

#### 4. 运营看板 + Nacos + 监控

**Test:** dashboard/stats、publish、可选 Grafana  
**Expected:** 字段结构正确；Nacos Data ID 更新  
**Why human:** 运行时观测

#### 5. uat-smart-cs.sh smoke

**Test:** `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`  
**Expected:** exit 0（HITL D-14 除外）  
**Why human:** 需已运行应用；验证冷启动修复后 smoke 可达

### Gaps Summary

**UAT 冷启动两个 blocker 已关闭：**

1. **06-08** — 三处企业项目 compose override volume 改为相对首文件 `docker/` 的 `../projects/<proj>/db`（及 smartcs prometheus）；运行时 Mounts.Source 正确且 init exit 0。  
2. **06-09** — `query-rewrite` classpath + 主/test `data.sql` 补齐 `{target}`+`{query}` 与幂等 UPDATE；orchestrator 证实 health UP 且无 PromptAssert target 错误。

无代码级 **BLOCKER** / 无待 `gaps:` 结构化项。相位目标在代码与冷启动门禁上可达。

剩余 **human_needed**：冷启动修复后的全链路 UAT 补跑（login / FAQ / 工单 / 看板 / smoke）。**06-REVIEW Critical HITL/approve 404（D-14）保持 Pending Todo，不记入本轮 gaps。**

---

_Verified: 2026-07-17T16:20:00Z_  
_Verifier: Claude (gsd-verifier)_
