# 交接文档：spring-ai-alibaba-learning → Claude Code + open-gsd

> **本文件用途**：全量上下文快照与操作手册（最初用于从对话式生成移交 Claude Code + open-gsd）。
>
> **状态（2026-07-18）**：✅ **v1.0 Full Delivery 已归档**——Phase 1～7 全部完成。日常入口优先读根 [`README.md`](README.md) 与 [`CLAUDE.md`](CLAUDE.md)；规划状态见 [`.planning/STATE.md`](.planning/STATE.md) / [`.planning/ROADMAP.md`](.planning/ROADMAP.md)。下一里程碑用 `/gsd-new-milestone`。
>
> **给 Claude Code 的第一条指令**：进入本仓库后，先 `Read` 根 `README.md`、`CLAUDE.md` 与本文件 §0～§1，再按需深入 `docs/00-overview/`。GSD 命令族以本机 `/gsd-help` 为准。
>
> **核验日期**：正文技术约束仍以 2026-07-04 调研为准；交付状态已于 2026-07-18 对齐 v1.0。

---

## 0. 一句话现状

`spring-ai-alibaba-learning` 是一套"教程正文 + 可运行源码 + 三个企业项目"的 Spring AI Alibaba 企业级教学仓库。  
**v1.0 已交付**：Phase 1 基座 · Phase 2（22 章 + starter）· Phase 3（48 Demo，UAT 48/48）· Phase 4～6（三企业项目）· Phase 7（生产化）。  
**下一步**：`/gsd-new-milestone` 定义 v1.1/v2.0；已知 tech_debt 见 `.planning/STATE.md` Deferred Items。

---

## 1. 全量技术上下文（SSOT）

### 1.1 版本锁定（父 POM 唯一真源）

| 组件 | 版本 | 备注 |
|---|---|---|
| Java | 21 | `java.version=21` |
| Spring Boot | **3.5.16** | 3.x 最终版；OSS 支持已于 2026-06-30 到期（见 ADR-002） |
| Spring AI Alibaba | **1.1.2.2** | 2026-03-10；1.1.2.1 被官方撤回推荐 |
| SAA Extensions | **1.1.2.2** | 与主线同版本 |
| Spring AI（主线） | **1.1.2** | 可按 CVE 需要升到 1.1.8 |
| Lombok | 1.18.36 | |
| MapStruct | 1.6.3 | |
| springdoc | 2.8.9 | |
| knife4j | 4.5.0 | |

**两个 BOM 必须同时导入**（父 POM 已正确配置）：`spring-ai-alibaba-bom` **与** `spring-ai-alibaba-extensions-bom`。只导入前者会导致 `starter-dashscope` 版本无法被管理（GitHub Discussion #4030）。

**三处已核验的版本更正（务必保留，勿回退）**：
1. SAA 最新稳定版是 **1.1.2.2**（不是早期记录的 1.1.2.0）；1.1.2.1 因缺陷被官方撤回。
2. 主 BOM 单独并不能管理 `starter-dashscope` 版本 → 必须**两个 BOM 一起导入**。
3. **Spring AI 2.0.0 已于 2026-06-12 正式 GA**（非 Milestone），但 **SAA 至今无对齐 2.0 / Boot 4 的版本** → 选 SAA 即当前必须停留在 Boot 3.5.x 线。此结论固化在 `docs/00-overview/02-版本调研报告.md`、`04-技术选型ADR.md`（ADR-002）与教程第 22 章。

### 1.2 关键决策（ADR 摘要，全文见 `docs/00-overview/04-技术选型ADR.md`）

- **ADR-001**：选 SAA 而非裸 Spring AI —— 换取 Agent Framework / Graph / Nacos 系企业能力。
- **ADR-002**：锁 Boot 3.5.16 而非 4.x —— 因 SAA 无 2.0 对齐版（见上）。
- **ADR-003**：模型策略 = DashScope 主通道 + DeepSeek 直连副通道。
- **ADR-004**：向量库 Milvus 主力 / PGVector 轻量 / Redis 缓存 / ES 混检。
- **ADR-005/006**：构建与文档规范。

### 1.3 目录结构与模块状态

```
spring-ai-alibaba-learning/
├── pom.xml                  # 父工程，多模块，SSOT 版本属性，双 BOM
├── README.md                # 学习大纲与仓库入口（v1.0）
├── CLAUDE.md                # Claude Code 项目记忆（自动加载）
├── HANDOFF-TO-CLAUDE-CODE.md # 本文件（历史交接 + 约束 SSOT）
├── common/          [✅] saa-learning-common：Result/异常/全局处理器 + 单测
├── starter/         [✅] saa-learning-starter：装配/审计Advisor/路由降级/成本采集
├── examples/        [✅] 48 个独立 Demo（UAT 48/48，清单见 examples/README.md）
├── projects/        [✅] 三企业项目：knowledge-qa(19100) / office-agent(19200) / smart-cs(19300)
├── docker/          [✅] docker-compose.yml，profiles: core/vector/mq/search/cloud
├── scripts/         [✅] env-check / infra / setup-env / quality-gate / version-audit / UAT / deploy-smoke 等
├── docs/
│   ├── 00-overview/ [✅] 01~06（含生产化、UAT 债务索引）
│   └── tutorial/    [✅] 01~22 章教材级教程（约 25 万字）
└── images/          [✅] 截图资源约定目录
```

父 POM `<modules>` 挂载 `common` 与 `starter`；`examples/*`、`projects/*` 为**独立可运行应用**（以本仓库为 parent，不强制挂入父 modules）。

### 1.4 全局约定（全仓必须遵守）

- **包根**：`com.flywhl.saa`（作者标识 `@author flywhl`）。
- **端口**：Demo 工程 `examples/NN-xxx` → 端口 `180NN`（如 example 29 → 18029）。一个 Server/Client 配对时，Client 用 `+100` 偏移（如 example 34 Server 18034 / Client 18134）。已用端口见 `examples/README.md` 与各章 Demo。
- **章节骨架**（教程已固化 15 节，Phase 3 Demo 的 README 可精简）：学习目标→前置知识→核心概念→架构 Mermaid→API 解析→可运行 Demo→关键源码→企业实践建议→性能优化→安全建议→常见踩坑→版本差异→为什么这样设计→FAQ→总结→延伸阅读→下一章预告→思考题。
- **图示**：一律 Mermaid，可直接渲染。
- **代码**：零 TODO、零伪代码、零"请自行补充"；`git clone → docker compose up → mvn spring-boot:run` 直接可跑。
- **禁用**已废弃 API：`PromptChatMemoryAdvisor`（用 `MessageChatMemoryAdvisor`）、`CallAroundAdvisor/AdvisedRequest`（用 `CallAdvisor/ChatClientRequest`）、`FunctionCallback`（用 `@Tool/ToolCallback`）、可变 Options setter（一律 Builder，为 2.0 迁移提前对齐）。
- **密钥**：只经环境变量注入（`AI_DASHSCOPE_API_KEY`、DeepSeek Key 等），严禁提交。

---

## 2. 已交付成果盘点（v1.0）

### Phase 1（脚手架/调研）✅
父 `pom.xml`、`common`、`docker/docker-compose.yml`（profiles）、`scripts/`、`docs/00-overview/`（含 ADR）、Demo/项目清单与根 `README.md`。

### Phase 2（教程正文 + starter + QA）✅
- **22 章教程**（`docs/tutorial/01~22`）；**starter** 完整实现（装配 / 审计 Advisor / `FallbackModelRouter` / 成本采集）并参与构建。
- QA 脚本：`version-audit.sh`、`spring-ai-2-readiness.sh`。

### Phase 3（48 Demo）✅
`examples/01`～`48` 全部可独立运行；UAT 48/48（`scripts/uat-phase3.sh`）。清单 SSOT：`examples/README.md`。

### Phase 4～6（三企业项目）✅
| 项目 | 目录 | 端口 | 验收 |
|---|---|---|---|
| 知识库问答 | `projects/knowledge-qa-platform` | 19100 | `uat-knowledge-qa.sh` |
| 办公 Agent | `projects/office-agent-assistant` | 19200 | `uat-office-agent.sh` |
| 智能客服 | `projects/smart-cs-platform` | 19300 | HUMAN-UAT 5/5 + REVIEW-FIX |

### Phase 7（生产化）✅
`quality-gate.sh`、CI 工作流、Compose 部署路径、`docs/00-overview/05`～`06`。归档：`.planning/milestones/v1.0-*.md`。

---

## 3. 维护约定与 Demo 验收标准（仍有效）

新增/修改 Demo 或企业项目时仍须满足：

1. 独立 `pom.xml`（`parent` 指向本仓库父 POM）。
2. 端口：Demo `180NN`；企业项目 19100/19200/19300，不冲突。
3. 中间件用 `bash scripts/infra.sh up <profiles>`（或项目 compose override）。
4. 密钥仅环境变量，绝不硬编码。
5. REST + `curl` / `.http` + 预期输出与对应教程章一致。
6. 模型 IT：`@EnabledIfEnvironmentVariable`；中间件：Testcontainers。
7. 复用 `saa-learning-common` 与 `saa-learning-starter`。

**下一里程碑候选**（未锁定，见 `.planning/PROJECT.md`）：远程 CI 首次绿、deploy-smoke 全量、HITL 持久化、SAA/Spring AI 2.0 就绪跟踪。

---

## 4. Claude Code 环境准备

### 4.1 环境首检清单（改代码 / 开新里程碑前建议跑一遍）

```bash
# 1. 工具链（目标机：MacBook arm64 / OrbStack）
java -version            # 需 21
mvn -version
docker version

# 2. 密钥（复制模板后填值，勿提交）
cp scripts/setup-env.example.sh scripts/setup-env.local.sh   # 若尚未创建
source scripts/setup-env.local.sh
bash scripts/env-check.sh

# 3. 编译公共底座
mvn -q -pl common,starter -am clean install
mvn -pl starter test

# 4. BOM / 质量门禁
bash scripts/version-audit.sh
bash scripts/quality-gate.sh    # 按需；耗时更长
```

`common` / `starter` 是三企业项目公共底座，编译失败时优先修复再改业务模块。

### 4.2 项目记忆
根目录 `CLAUDE.md` 为 Claude Code 常驻上下文（版本锁定、约定、禁用 API、v1.0 状态）。学习路径见根 `README.md`。

---

## 5. GSD 工作流接入（open-gsd，`/gsd-*` 形态）

### 5.1 版本与形态确认（第一步）

- 你的 open-gsd 以 **skill** 形式安装在用户级 `~/.claude/skills/gsd-*/SKILL.md`（Claude Code v2.1.88 起弃用 `~/.claude/commands/`，GSD 已迁移到 skills）。
- Claude Code 用 **连字符** 形态 `/gsd-命令`（`/gsd:命令` 冒号形态仅 Gemini CLI 用）。
- **先跑 `/gsd-help`** 打印你本机实际命令族，以其为准；下文命令名以主流版本为例，个别可能叫法略异。

### 5.2 命令流与项目阶段的映射

GSD 的标准相位闭环（每相位建议开新上下文窗口）：

```
/gsd-init  →  /gsd-discuss-phase  →  /gsd-research-phase  →  /gsd-plan-phase  →  /gsd-execute-phase  →  /gsd-verify-phase
（若你的版本有 /gsd-next 与 /gsd-ship，用它们在相位间推进与收口）
```

v1.0 相位（1～7）已全部走完并归档。后续新里程碑仍用同一闭环：

```
/gsd-new-milestone → discuss → research → plan → execute → verify → ship
```

> **要点**：本仓库是 **v1.0 已交付的存量项目**。不要重做 Phase 1～7 需求；新工作从 `/gsd-new-milestone` 登记范围开始。历史相位工件仍在 `.planning/phases/`（可用 `/gsd-cleanup` 后续归档）。

### 5.3 建议的规划工件与技能配置

- **规划目录**：`/gsd-init` 会创建 GSD 的规划工作区（视版本为 `.planning/` 或 `.claude/get-shit-done/` 下的工件）。让它 scaffold 后不要手动改目录名。
- **项目技能（已落地）**：`.claude/skills/saa-conventions/SKILL.md` 固化第 1.4 节约定；GSD project-skills discovery 会自动加载。
- **上下文注入**：GSD 命令用 `@path` 硬引用强制加载上下文。给 execute 相位喂入时，至少 `@HANDOFF-TO-CLAUDE-CODE.md`、`@docs/00-overview/03-总体架构与目录规划.md`、以及**对应章节**教程（如做 RAG Demo 就 `@docs/tutorial/09-RAG.md`）——每章 Demo 的源码位置、端口、预期输出都已写在章内，是最精确的实现规格。

### 5.4 新里程碑操作序列（示例）

```text
# 会话 A —— 开启下一里程碑
/gsd-new-milestone
  参考 .planning/PROJECT.md「Next Milestone Goals」与 STATE.md Deferred Items，
  确认范围后生成新 ROADMAP / REQUIREMENTS。

# 会话 B —— 相位闭环
/gsd-discuss-phase → /gsd-research-phase → /gsd-plan-phase
/gsd-execute-phase → /gsd-verify-phase
# 需要时 /gsd-ship
```

---

## 6. 可直接粘贴的 GSD Kickoff Prompt（v1.0 之后）

> 在装好 open-gsd 的 Claude Code 里，`cd` 到本仓库后粘贴：

```
这是 spring-ai-alibaba-learning：v1.0 Full Delivery 已于 2026-07-18 归档
（Phase 1～7：22 章教程 + 48 Demo + 3 企业项目 + 生产化）。

请先 Read：
  @README.md
  @CLAUDE.md
  @HANDOFF-TO-CLAUDE-CODE.md
  @.planning/STATE.md
  @.planning/PROJECT.md

然后：
1. 跑 HANDOFF §4.1 环境首检（common+starter 编译、starter 单测、version-audit）。
2. 用 /gsd-new-milestone 开启下一里程碑；不要重做 v1.0 已交付范围。
3. 遵守 HANDOFF §1.4 约定与 §3 验收标准；工程约定见
   @.claude/skills/saa-conventions/SKILL.md
4. 候选方向见 PROJECT.md Next Milestone Goals（CI 绿、deploy-smoke、HITL 持久化、2.0 跟踪等）。

先给出下一里程碑范围建议与相位切分，确认后再动手。
```

---

## 7. 质量门禁（每批 Demo / 每阶段收口前必过）

- [ ] `mvn -pl <新模块> -am clean install` 通过（真实编译，非静态检查）。
- [ ] `mvn spring-boot:run` 起得来，`curl` 验证命令返回预期输出。
- [ ] 端口无冲突（对照 `examples/README.md`）。
- [ ] 依赖树版本唯一：`bash scripts/version-audit.sh` 全绿。
- [ ] 未引入 2.0 破坏点：`bash scripts/spring-ai-2-readiness.sh .` 数字维持低位。
- [ ] 无已废弃 API、无硬编码密钥、无 TODO/伪代码。
- [ ] 集成测试用 Testcontainers / `@EnabledIfEnvironmentVariable`，CI 环境可跳过无 Key/无 Docker 情形。
- [ ] 复用 `common` 与 `starter`，未重复实现异常处理/审计/路由/成本采集。

---

## 8. 风险与注意事项

1. **Milvus 冷启动慢**：依赖 etcd+MinIO 健康检查，30~60 秒才可用；等 `service_healthy`（见 `docker/docker-compose.yml`）。
2. **Redis 向量/记忆需 Redis Stack**：普通 redis 缺 RedisJSON/RediSearch（教程第 08/11 章已标注）。
3. **Nacos 3.0.x**：MCP/Prompt Demo 注意与 SAA 版本对齐。
4. **GSD 命令名以 `/gsd-help` 为准**。
5. **Boot 3.5 已 EOL**：不因此仓促迁 2.0（SAA 尚无对齐版）；见教程第 22 章。
6. **v1.0 tech_debt**（不阻塞）：远程 Actions 首次绿、deploy-smoke 全量、HITL pending 进程内 Map、kqa 上传全路径压测——见 `.planning/STATE.md`。

---

*初版交接：2026-07-04。交付状态对齐：2026-07-18（v1.0 Full Delivery）。*
