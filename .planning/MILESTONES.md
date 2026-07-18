# Milestones: spring-ai-alibaba-learning

## v1.0 Full Delivery (Shipped: 2026-07-18)

**Phases completed:** 7 phases（含 brownfield 1–2），约 36 plans / 90 tasks  
**Archives:** [ROADMAP](milestones/v1.0-ROADMAP.md) · [REQUIREMENTS](milestones/v1.0-REQUIREMENTS.md) · [AUDIT](milestones/v1.0-MILESTONE-AUDIT.md)  
**Audit status:** tech_debt（7/7 REQ satisfied，无关键阻塞）  
**Known deferred at close:** 6 items（见 STATE.md Deferred Items）

**Key accomplishments:**

1. **基座 + 教程** — 父 POM / common / starter / 22 章教程 / docker profiles / ADR-001~006
2. **48 独立 Demo** — examples/01~48，UAT 48/48，端口 180NN，复用 common/starter
3. **知识库问答** — knowledge-qa-platform:19100，RAG+Citation+SSE，脚本 UAT 8/0
4. **办公 Agent** — office-agent-assistant:19200，Tools+审批编排，脚本 UAT 12/0
5. **智能客服** — smart-cs-platform:19300，多智能体+HITL+看板；gap 08–10 + REVIEW-FIX 8/8
6. **生产化** — quality-gate.sh、ci.yml / model-it.yml、Compose 部署文档与 UAT 债务索引

**Evidence:** `.planning/phases/**` · `scripts/quality-gate.sh` · `docs/00-overview/`

**Next:** `/gsd-new-milestone`

---

## Historical: Phase 3 sub-archive (2026-07-05)

Phase 3 曾单独归档：[`milestones/phase-3-48-demos-ROADMAP.md`](milestones/phase-3-48-demos-ROADMAP.md)。全量 v1.0 归档后以本文件顶部条目为准。
