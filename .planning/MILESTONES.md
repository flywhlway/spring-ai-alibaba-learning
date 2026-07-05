# Milestones: spring-ai-alibaba-learning

## v1.0 — 全栈教学与企业项目（进行中）

**Core value:** 48 Demo + 3 企业项目可 `mvn spring-boot:run`，通过 HANDOFF §7 质量门禁

### Phase 3: 48 个独立 Demo ✅ SHIPPED 2026-07-05

**Scope:** examples/01~48 全量可运行 Demo  
**Plans:** 14/14 complete  
**UAT:** 48/48 pass（`scripts/uat-phase3.sh` v3）

**Key accomplishments:**

1. **48 个独立 Demo 工程齐备** — 三批交付（01~19 / 20~34 / 35~48），compile gate 全绿
2. **starter 企业能力落地** — 44~48 强制复用 ModelRouter、AuditLoggingAdvisor、CostTracking
3. **Agent/Graph/Multi-Agent 全覆盖** — ReactAgent、StateGraph、Supervisor、A2A+Nacos
4. **Nacos 集成闭环** — 34 MCP 分布式发现 + 43 A2A 远程 Agent 真机验收通过
5. **自动化 UAT 脚本** — `scripts/uat-phase3.sh` 一键 curl 验收 48 Demo
6. **UAT gap 修复** — lazy MCP Client、A2A AgentCard 对齐、validation 依赖、ModelRouter 时序

**Evidence:**

- `.planning/phases/03-48-demo/VERIFICATION.md`
- `.planning/phases/03-48-demo/03-UAT.md`
- `.planning/milestones/phase-3-48-demos-ROADMAP.md`

**Known minor items (non-blocking):**

- Demo 37 HITL 模型偶发不触发 tool，UAT 降级接受 COMPLETED
- starter 真机单测欠账（Phase 2 遗留，Phase 4 前补齐）

**Next:** Phase 4 — knowledge-qa-platform（端口 19100）

---

### Phase 1–2: 基座 + 教程（brownfield validated）

- Phase 1: 脚手架 — 2026-07-03
- Phase 2: 22 章教程 + starter + QA — 2026-07-03

### Phase 4–7: 待交付

- Phase 4: 知识库问答平台
- Phase 5: 办公 Agent 助手
- Phase 6: 智能客服平台
- Phase 7: 生产化

---
*Last updated: 2026-07-05 after Phase 3 milestone closure*
