# Context (DOC)

Running notes by topic. **Current position: v1.0 Full Delivery shipped 2026-07-18**（Phase 1～7 complete）.  
SSOT for learning entry: root `README.md`；agent memory: `CLAUDE.md`；handoff constraints: `HANDOFF-TO-CLAUDE-CODE.md`.

---

## Topic: project identity and current position

- **source:** README.md; CLAUDE.md; .planning/STATE.md
- spring-ai-alibaba-learning：教程正文 + 可运行源码 + 三个企业项目，7 阶段交付。
- **v1.0 已归档交付（2026-07-18）**：Phase 1～7 全部完成。
- **下一动作**：`/gsd-new-milestone`；Deferred Items 见 STATE.md。

---

## Topic: delivered inventory (v1.0)

- **source:** HANDOFF-TO-CLAUDE-CODE.md §2; .planning/milestones/v1.0-*
- common/ [✅] saa-learning-common
- starter/ [✅] saa-learning-starter（装配/审计/路由/成本）
- examples/ [✅] 48 Demo，UAT 48/48
- projects/ [✅] knowledge-qa(19100) / office-agent(19200) / smart-cs(19300)
- docker/ [✅] profiles: core/vector/mq/search/cloud
- scripts/ [✅] env-check、infra、setup-env、quality-gate、version-audit、UAT、deploy-smoke 等
- docs/00-overview/ [✅] 01～06；docs/tutorial/ [✅] 01～22
- 父 POM `<modules>`：common + starter；examples/projects 为独立应用

---

## Topic: version lock and corrections

- **source:** docs/00-overview/02-版本调研报告.md; CLAUDE.md
- 父 POM 唯一真源：Java 21、Boot 3.5.16、SAA 1.1.2.2、Extensions 1.1.2.2、Spring AI 1.1.2
- 两 BOM 必须同时导入；勿升 Boot 4 / Spring AI 2.0（SAA 无对齐版）
- Spring AI 生产可升 1.1.8（CVE）；父 POM 默认 1.1.2 对齐 SAA

---

## Topic: seven-phase learning roadmap

- **source:** docs/00-overview/01-学习路线.md; README.md
- Phase 1 基座 → Phase 2 教程 01–22 → Phase 3 48 Demo → Phase 4–6 三企业项目 → Phase 7 生产化（**均已交付**）
- 学习顺序建议：教程对应章 → 跑 Demo → 企业项目（RAG → Tool/MCP → Multi-Agent）→ 生产化文档

---

## Topic: engineering conventions (hard rules)

- **source:** CLAUDE.md; .claude/skills/saa-conventions/SKILL.md
- 包根 `com.flywhl.saa`，`@author flywhl`
- Demo 端口 `180NN`；Server/Client 配对 Client `+100`
- Mermaid；零 TODO/伪代码；`mvn spring-boot:run` 可跑
- 密钥仅 `setup-env.local.sh` / 环境变量
- 复用 common 与 starter；禁用废弃 API

---

## Topic: known risks

- **source:** CLAUDE.md; HANDOFF §8; STATE.md Deferred
- Milvus 冷启动 30~60s；Redis 向量/记忆需 redis-stack
- tech_debt（不阻塞）：远程 Actions 首次绿、deploy-smoke、HITL 进程内 Map、kqa 上传全路径压测

---

## Topic: GSD workflow

- **source:** HANDOFF §5; CLAUDE.md
- open-gsd skill；`/gsd-*` 连字符
- v1.0 后：`/gsd-new-milestone` → discuss→plan→execute→verify
- 项目级 skill：`.claude/skills/saa-conventions/SKILL.md`

---

## Topic: quality gates

- **source:** HANDOFF §7; scripts/quality-gate.sh
- 真实编译；spring-boot:run + curl；端口无冲突；version-audit；spring-ai-2-readiness；无废弃 API/硬编码密钥/TODO；Testcontainers / EnabledIfEnvironmentVariable；复用 common/starter
