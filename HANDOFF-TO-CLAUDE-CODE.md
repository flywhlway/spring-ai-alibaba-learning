# 交接文档：spring-ai-alibaba-learning → Claude Code + open-gsd

> **本文件用途**：把本仓库从"对话式生成"平滑移交给 **Claude Code**（配合用户级安装的 **open-gsd** 流程控制插件）继续开发。它同时是一份"全量上下文快照"与"逐步操作手册"。
>
> **给 Claude Code 的第一条指令**：进入本仓库后，先 `Read` 本文件与根目录 `CLAUDE.md`，再 `Read` `docs/00-overview/` 下四份总览文档，然后按第 6 节的 GSD 流程推进。
>
> **核验日期**：2026-07-04。GSD 命令族在不同版本间有细微差异，任何以 `/gsd-*` 开头的命令都以你本机 `/gsd-help` 的实际输出为准。

---

## 0. 一句话现状

`spring-ai-alibaba-learning` 是一套"教程正文 + 可运行源码 + 三个企业项目"的 Spring AI Alibaba 企业级教学仓库，按 7 个阶段交付。**Phase 1（脚手架/调研）与 Phase 2（22 章教程 + starter 模块 + 全量 QA）已完成**，下一步是 **Phase 3：40~60 个独立可运行 Demo 工程**。

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
├── CLAUDE.md                # ← Claude Code 项目记忆（本次新增，自动加载）
├── HANDOFF-TO-CLAUDE-CODE.md# ← 本文件
├── common/          [✅ 完成] saa-learning-common：Result/PageResult/ResultCode/BizException/GlobalExceptionHandler + 单测
├── starter/         [✅ 完成] saa-learning-starter：统一装配/审计Advisor/模型路由降级/成本采集（第19章落地）
├── examples/        [⏳ Phase 3] 40~60 个独立 Demo（当前仅 README 目录清单）
├── projects/        [⏳ Phase 4-6] 三个企业项目（当前仅 README 蓝图）
├── docker/          [✅ 完成] docker-compose.yml，profiles: core/vector/mq/search/cloud
├── scripts/         [✅ 完成] env-check.sh / infra.sh / setup-env.example.sh / version-audit.sh / spring-ai-2-readiness.sh
├── docs/
│   ├── 00-overview/ [✅ 完成] 01-学习路线 / 02-版本调研报告 / 03-总体架构与目录规划 / 04-技术选型ADR
│   └── tutorial/    [✅ 完成] 01~22 章教材级教程（约 25 万字）
└── images/          [占位]
```

父 POM 当前 `<modules>` 仅挂载 `common` 与 `starter`；`examples/`、`projects/` 下各工程是**独立可运行应用**，Phase 3~6 逐个新增并按需挂载。

### 1.4 全局约定（Phase 3+ 必须遵守）

- **包根**：`com.flywhl.saa`（作者标识 `@author flywhl`）。
- **端口**：Demo 工程 `examples/NN-xxx` → 端口 `180NN`（如 example 29 → 18029）。一个 Server/Client 配对时，Client 用 `+100` 偏移（如 example 34 Server 18034 / Client 18134）。已用端口见 `examples/README.md` 与各章 Demo。
- **章节骨架**（教程已固化 15 节，Phase 3 Demo 的 README 可精简）：学习目标→前置知识→核心概念→架构 Mermaid→API 解析→可运行 Demo→关键源码→企业实践建议→性能优化→安全建议→常见踩坑→版本差异→为什么这样设计→FAQ→总结→延伸阅读→下一章预告→思考题。
- **图示**：一律 Mermaid，可直接渲染。
- **代码**：零 TODO、零伪代码、零"请自行补充"；`git clone → docker compose up → mvn spring-boot:run` 直接可跑。
- **禁用**已废弃 API：`PromptChatMemoryAdvisor`（用 `MessageChatMemoryAdvisor`）、`CallAroundAdvisor/AdvisedRequest`（用 `CallAdvisor/ChatClientRequest`）、`FunctionCallback`（用 `@Tool/ToolCallback`）、可变 Options setter（一律 Builder，为 2.0 迁移提前对齐）。
- **密钥**：只经环境变量注入（`AI_DASHSCOPE_API_KEY`、DeepSeek Key 等），严禁提交。

---

## 2. 已交付成果盘点

### Phase 1（脚手架/调研）✅
父 `pom.xml`、`common` 模块（含单测）、`docker/docker-compose.yml`（5 组 profile：Redis/Postgres+pgvector/MySQL/MinIO/Milvus+etcd/Kafka/RabbitMQ/ES/Nacos）、`scripts/`、`docs/00-overview/`（4 份，含 ADR-001~006）、`examples/README.md`（Demo 目录清单）、`projects/README.md`（三项目蓝图）、根 `README.md`。

### Phase 2（教程正文 + starter + QA）✅
- **22 章教程**（`docs/tutorial/01~22`，约 25 万字），全部通过 15 项骨架小节检查、代码围栏配对、版本一致性、端口无冲突、`下一章预告` 链条 1→22 连续。
- **starter 模块完整实现**（`starter/src/main/java/com/flywhl/saa/starter/`）：
  - `autoconfigure/SaaLearningProperties`（record，前缀 `saa.learning`）+ `SaaLearningAutoConfiguration`（`@ConditionalOnMissingBean` / `@ConditionalOnBean` / `@ConditionalOnProperty` 三原则）
  - `advisor/AdvisorOrder`（顺序常量）+ `AuditLoggingAdvisor`（脱敏审计，`CallAdvisor+StreamAdvisor`）
  - `routing/ModelRouter` + `FallbackModelRouter`（无锁熔断降级，`AtomicReference`+不可变 State record）+ 4 例单测
  - `metrics/CostRecorder` + `LoggingCostRecorder` + `CostTrackingObservationHandler`（基于 `gen_ai.usage.*`）
  - `META-INF/spring/...AutoConfiguration.imports`
- **两个 QA 脚本**：`scripts/version-audit.sh`（BOM 对齐自检）、`scripts/spring-ai-2-readiness.sh`（2.0 破坏点扫描）。

> ⚠️ 沙箱内无 Maven/网络，starter 仅做过**括号配对 + 包声明 + API 对照官方文档**校验，**尚未真正 `mvn compile`**。Claude Code 落地后**第一件事应是编译验证**（见第 5.1 节）。

---

## 3. 下一阶段：Phase 3 任务定义

**目标**：把教程每一章的核心 API 落成 40~60 个可独立 `mvn spring-boot:run` 的最小 Demo，全部挂在 `examples/` 下，目录清单见 `examples/README.md`（已列 ~48 条，编号/端口/对应章已规划）。

**每个 Demo 的验收标准**：
1. 独立 `pom.xml`（`parent` 指向本仓库父 POM，继承版本管理）。
2. 端口遵循 `180NN` 约定，与既有 Demo 不冲突。
3. 依赖真实中间件的，用对应 `docker compose` profile 一键起（`bash scripts/infra.sh up <profiles>`）。
4. `application.yml` 用 `${AI_DASHSCOPE_API_KEY}` 注入密钥，绝不硬编码。
5. 至少一个 REST 入口 + 明确的 `curl` 验证命令 + 预期输出（与对应章节 Demo 一致）。
6. 涉及模型调用的集成测试用 `@EnabledIfEnvironmentVariable(named="AI_DASHSCOPE_API_KEY", matches=".+")` 跳过无 Key 环境；涉及中间件的用 Testcontainers。
7. 复用 `saa-learning-common` 与 `saa-learning-starter`，不重复造轮子。

**优先级建议**（可作为 GSD 子阶段切分）：先做 01~08 章对应的基础 Demo（quickstart / autoconfig / chatclient / prompt-nacos / advisor / tool / memory），再做 09~12 的 RAG/Embedding/VectorStore/MCP，再做 13~18 的 Agent/Graph/MultiAgent/结构化/流式/可观测，最后 19~22 的最佳实践与迁移类。

**后续阶段**（Phase 4-6 = `projects/`）：企业项目一"AI 知识库问答平台" / 项目二"AI Agent 办公助手" / 项目三"智能客服 Agent 平台"；Phase 7 = 统一测试/CI-CD/部署/调优。

---

## 4. Claude Code 环境准备

### 4.1 落地首检清单（Phase 3 动手前先跑一遍）

```bash
# 1. 工具链（目标机：MacBook M5 Pro arm64 / OrbStack）
java -version            # 需 21
mvn -version             # 或 ./mvnw
docker version           # OrbStack 提供

# 2. 密钥（复制模板后填值，勿提交）
cp scripts/setup-env.example.sh scripts/setup-env.sh   # 若尚未创建
source scripts/setup-env.sh
bash scripts/env-check.sh    # 校验 AI_DASHSCOPE_API_KEY 等是否就位

# 3. 编译既有模块（关键：验证 Phase 2 的 starter 真能编过）
mvn -q -pl common,starter -am clean install
mvn -pl starter test          # 跑 FallbackModelRouterTest 4 个用例

# 4. BOM 对齐自检
bash scripts/version-audit.sh
```

若 `starter` 编译报错，**先修 starter 再开 Phase 3**——它是三个企业项目的公共底座，不能带病进入下一阶段。

### 4.2 项目记忆
根目录 `CLAUDE.md`（本次一并生成）是 Claude Code 每次会话自动加载的"常驻上下文"，已提炼版本锁定、约定、禁用 API、当前阶段。无需手动引用。

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

把本项目的宏观 Phase 直接喂给 GSD 的相位循环：

| 本项目 Phase | GSD 相位动作 | 产出 |
|---|---|---|
| Phase 3（40~60 Demo） | 对每一批 Demo 走 discuss→research→plan→execute→verify | `examples/NN-*` 各工程 + 规划工件 |
| Phase 4/5/6（三个企业项目） | 每个项目独立走完整相位循环 | `projects/*` |
| Phase 7（测试/CI-CD/部署） | 单相位 execute + verify | 流水线与部署脚本 |

> **要点**：本仓库不是"从零新建"，而是"已完成 Phase 1-2 的存量项目"。因此 `/gsd-init` 的作用是**建立 GSD 规划工作区并登记既有路线图**（本文件 + `docs/00-overview/` 就是现成的 requirements/roadmap 输入），而不是重新做需求。

### 5.3 建议的规划工件与技能配置

- **规划目录**：`/gsd-init` 会创建 GSD 的规划工作区（视版本为 `.planning/` 或 `.claude/get-shit-done/` 下的工件）。让它 scaffold 后不要手动改目录名。
- **项目技能（强烈建议）**：在 `.claude/skills/` 下新建一个项目级 skill（如 `saa-conventions/SKILL.md`），把第 1.4 节的"全局约定 + 禁用 API + 端口规则"固化进去。GSD 的 project-skills discovery 会在后续会话自动加载它，确保每个 Demo 都遵循同一套约定，无需每次口述。
- **上下文注入**：GSD 命令用 `@path` 硬引用强制加载上下文。给 execute 相位喂入时，至少 `@HANDOFF-TO-CLAUDE-CODE.md`、`@docs/00-overview/03-总体架构与目录规划.md`、以及**对应章节**教程（如做 RAG Demo 就 `@docs/tutorial/09-RAG.md`）——每章 Demo 的源码位置、端口、预期输出都已写在章内，是最精确的实现规格。

### 5.4 逐阶段操作序列（Phase 3 示例）

```text
# 会话 A —— 初始化 + 登记路线图（一次性）
/gsd-init
  当 GSD 提问时，指向本文件与 docs/00-overview/ 作为需求与路线图输入，
  声明"Phase 1-2 已完成，从 Phase 3 起接管"。

# 会话 B —— 规划 Phase 3 第一批（01~08 章基础 Demo）
/gsd-discuss-phase   # 明确这一批要做哪几个 Demo、验收标准（引用第 3 节）
/gsd-research-phase  # 需要时核验 SAA 1.1.2.2 具体 API（教程已给出，多数无需再查）
/gsd-plan-phase      # 产出每个 Demo 的任务分解（模块/端口/接口/curl/预期输出）

# 会话 C —— 执行 + 验收
/gsd-execute-phase   # 自动建模块、写码、编译、提交；每个 Demo 完成即 mvn spring-boot:run 自测
/gsd-verify-phase    # 对照第 3 节验收标准逐条核验，记录缺口

# 后续每一批 Demo 重复 B、C。企业项目（Phase 4-6）同法，每个项目一整轮。
```

---

## 6. 可直接粘贴的 GSD Kickoff Prompt

> 在装好 open-gsd 的 Claude Code 里，`cd` 到本仓库后粘贴：

```
这是一个名为 spring-ai-alibaba-learning 的存量项目：7 阶段规划，Phase 1（脚手架）与
Phase 2（22 章教程 + starter 模块 + QA）已完成，现从 Phase 3（40~60 个独立 Demo）接管。

请先 Read 以下文件建立上下文：
  @HANDOFF-TO-CLAUDE-CODE.md
  @CLAUDE.md
  @docs/00-overview/03-总体架构与目录规划.md
  @docs/00-overview/04-技术选型ADR.md
  @examples/README.md

然后：
1. 先执行 HANDOFF 第 4.1 节的落地首检（编译 common+starter、跑 starter 单测、version-audit）。
   若 starter 编译失败，先修复再继续。
2. 用 open-gsd 接管后续开发：/gsd-init 登记既有路线图（不重做 Phase 1-2 需求），
   随后按相位循环推进 Phase 3 第一批（01~08 章对应的基础 Demo）。
3. 每个 Demo 严格遵守 HANDOFF 第 1.4 节约定（包根 com.flywhl.saa、端口 180NN、
   双 BOM、禁用已废弃 API、密钥走环境变量）与第 3 节验收标准。
4. 建议先创建项目级 skill `.claude/skills/saa-conventions/SKILL.md` 固化这些约定。

先给我一份 Phase 3 第一批（基础 Demo）的执行计划与相位切分，确认后再动手。
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

1. **starter 未真机编译**：落地第一步必须补编译验证（第 4.1 节），这是 Phase 2 沙箱限制留下的唯一"欠账"。
2. **Milvus 冷启动慢**：依赖 etcd+MinIO 健康检查，30~60 秒才可用；Demo 启动脚本要等 `service_healthy`（见 `docker/docker-compose.yml`）。
3. **Redis 向量/记忆需 Redis Stack**：普通 `redis:7.4-alpine` 缺 RedisJSON/RediSearch，涉及 Redis VectorStore 或 RedisChatMemory 的 Demo 要换 `redis/redis-stack-server`（教程第 08/11 章已标注）。
4. **Nacos 3.0.x 注册行为**：MCP/Prompt 类 Demo 注意 SAA 与 Nacos 版本对齐（版本调研报告已锁 Nacos 3.0.x）。
5. **GSD 命令名以 `/gsd-help` 为准**：本文件给的是主流版本命名，你的 open-gsd 可能有增删。
6. **Boot 3.5 已 EOL**：新 CVE 无免费补丁，长期需关注（教程第 22 章给了商业支持过渡方案），但**不因此仓促迁 2.0**（SAA 尚无对齐版）。

---

*交接人：对话式生成会话（Claude）。接管方：Claude Code + open-gsd。核验日期 2026-07-04。*
