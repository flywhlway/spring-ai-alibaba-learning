# Context (DOC)

Running notes by topic. Phase 1–2 marked validated/delivered per brownfield sources; current position = Phase 3.
Cross-ref cycle HANDOFF ↔ CLAUDE recorded as blocker; entries below extracted independently (no follow-edge merge).

---

## Topic: project identity and current position

- **source:** HANDOFF-TO-CLAUDE-CODE.md (§0); CLAUDE.md
- spring-ai-alibaba-learning：教程正文 + 可运行源码 + 三个企业项目，7 阶段交付。
- **Phase 1（脚手架/调研）与 Phase 2（22 章教程 + starter + QA）已完成。**
- **当前阶段：Phase 3（40~60 / 清单锁定 48 个独立 Demo）。**
- 存量项目登记路线图，不重做 Phase 1–2 需求。

---

## Topic: delivered inventory (Phase 1–2)

- **source:** HANDOFF-TO-CLAUDE-CODE.md (§1.3, §2)
- common/ [完成] saa-learning-common：Result/PageResult/ResultCode/BizException/GlobalExceptionHandler + 单测
- starter/ [完成] saa-learning-starter：统一装配/审计 Advisor/模型路由降级/成本采集
- docker/ [完成] profiles: core/vector/mq/search/cloud
- scripts/ [完成] env-check、infra、setup-env.example、version-audit、spring-ai-2-readiness
- docs/00-overview/ [完成] 01–04；docs/tutorial/ [完成] 01–22（约 25 万字）
- examples/ [Phase 3] 清单已规划；projects/ [Phase 4–6] 蓝图已规划
- 父 POM `<modules>` 当前仅 common + starter；examples/projects 为独立应用，按阶段挂载

---

## Topic: version lock and corrections

- **source:** docs/00-overview/02-版本调研报告.md; HANDOFF-TO-CLAUDE-CODE.md (§1.1); CLAUDE.md
- 父 POM 唯一真源：Java 21、Boot 3.5.16、SAA 1.1.2.2、Extensions 1.1.2.2、Spring AI 1.1.2
- 三处已核验更正（勿回退）：
  1. SAA 最新稳定版是 1.1.2.2（非 1.1.2.0）；1.1.2.1 被官方撤回
  2. 主 BOM 单独不能管理 starter-dashscope → 必须两个 BOM 一起导入
  3. Spring AI 2.0.0 已于 2026-06-12 GA，但 SAA 无对齐 2.0/Boot 4 版本 → 选 SAA 即停留 Boot 3.5.x
- Boot 3.5 OSS 支持已于 2026-06-30 到期；生产可评估商业支持，但不因此仓促迁 2.0
- Spring AI 生产建议可升 1.1.8（CVE 补丁，向后兼容）；父 POM 默认 1.1.2 对齐 SAA

---

## Topic: seven-phase learning roadmap

- **source:** docs/00-overview/01-学习路线.md
- Phase 1 基座 → Phase 2 教程 01–22 → Phase 3 40~60 Demo → Phase 4–6 三企业项目 → Phase 7 CI/CD·部署·调优·排障
- 面向对象：有 Spring Boot/Cloud、Docker、LangGraph、RAG、MCP、LLM 经验的高级开发者
- 目标体系：SAA 1.1.2.2 + Spring AI 1.1.x + Boot 3.5.16
- 企业项目顺序建议：项目一 RAG 主线 → 项目二 Tool/MCP 主线 → 项目三 Multi-Agent 主线

---

## Topic: engineering conventions (hard rules)

- **source:** CLAUDE.md; HANDOFF-TO-CLAUDE-CODE.md (§1.4); docs/00-overview/03-总体架构与目录规划.md (§5)
- 包根 `com.flywhl.saa`，`@author flywhl`
- Demo 端口 `180NN`；Server/Client 配对 Client `+100`
- Mermaid 图示；零 TODO/伪代码；`mvn spring-boot:run` 可跑
- 密钥仅环境变量，严禁提交
- 复用 common 与 starter，不重复造轮子
- 禁用废弃 API（Memory/Advisor/Tool/Options Builder 对齐）

---

## Topic: known risks and first actions

- **source:** HANDOFF-TO-CLAUDE-CODE.md (§4.1, §8); CLAUDE.md
- starter 尚未真机编译 → 落地第一件事：`mvn -pl common,starter -am clean install` 与 starter 单测
- Milvus 冷启动 30~60s（etcd+MinIO 健康检查）
- Redis 向量/记忆需 `redis/redis-stack-server`（非普通 redis）
- Nacos 3.0.x 与 SAA MCP/Prompt Demo 版本对齐
- GSD 命令名以本机 `/gsd-help` 为准

---

## Topic: GSD workflow for this brownfield repo

- **source:** HANDOFF-TO-CLAUDE-CODE.md (§5); CLAUDE.md
- open-gsd skill 形态；命令 `/gsd-*`（连字符）
- `/gsd-init` 登记既有路线图（不重做 Phase 1–2），再 discuss→research→plan→execute→verify 推进 Phase 3+
- 每章 Demo 实现规格在 `docs/tutorial/NN-*.md`，execute 前 `@` 引用该章
- 项目级 skill：`.claude/skills/saa-conventions/SKILL.md`

---

## Topic: quality gates

- **source:** HANDOFF-TO-CLAUDE-CODE.md (§7)
- 真实编译通过；`mvn spring-boot:run` + curl 预期输出；端口无冲突；version-audit 全绿；spring-ai-2-readiness 低位；无废弃 API/硬编码密钥/TODO；Testcontainers / EnabledIfEnvironmentVariable；复用 common/starter
