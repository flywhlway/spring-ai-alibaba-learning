# 项目二 · 企业 AI Agent 办公助手（office-agent-assistant）

> Phase 5 企业项目 · 端口 **19200** · 蓝图 SSOT：[`projects/README.md`](../README.md)「项目二」

面向员工的智能办公入口：会议纪要、日报、邮件起草、报表查询、日程与审批协助。

## 技术栈

| 能力 | 落点 |
|---|---|
| 多轮对话 | ReactAgent + methodTools |
| 任务生成 | PromptTemplateProvider + ChatClient |
| 审批编排 | SequentialAgent + LlmRoutingAgent |
| 工具族 | SqlQueryTool / HttpInternalTool / ExcelTool / CalendarTool |
| MCP | 内嵌 Server（OfficeMcpTools）+ Client（application.yml） |
| 记忆 | Redis 短期 + JDBC 长期（SPRING_AI_CHAT_MEMORY） |
| 向量 | pgvector 独立 DataSource（office.vector-datasource） |
| 安全 | JWT + ADMIN/EMPLOYEE RBAC |

## 快速开始

```bash
# 1. 密钥
source scripts/setup-env.sh && bash scripts/env-check.sh

# 2. 中间件（MySQL + pgvector + Redis）
docker compose -f docker/docker-compose.yml \
  -f projects/office-agent-assistant/docker-compose.override.yml \
  --profile core --profile office up -d

# 3. 编译运行
mvn -pl common,starter -am clean install -DskipTests
mvn -f projects/office-agent-assistant/pom.xml spring-boot:run
```

## 演示账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | ADMIN |
| zhangsan | zhangsan123 | EMPLOYEE |

## 主要接口

- `POST /api/auth/login` — 登录签发 JWT
- `POST /api/chat` — ReactAgent 多轮对话
- `POST /api/tasks/meeting-summary|daily-report|email-draft` — 结构化任务
- `POST /api/approvals/review` — 审批 AI 初审
- `GET/POST /api/admin/users` — 用户管理
- `GET/POST /api/admin/prompts` — Prompt CRUD/发布

完整示例见 [`http/api.http`](http/api.http) 与 [Postman Collection](http/office-agent-assistant.postman_collection.json)，OpenAPI 文档：`http://localhost:19200/doc.html`

## 安全

模型（LLM）产出的工具入参一律视为**不可信输入**，工具层做纵深防御：

- **`SqlQueryTool`（报表查询）**：只允许单条 `SELECT`；先剥离字符串字面量再校验，杜绝把表名藏进字面量绕过；拒绝注释/分号/反引号/双引号/反斜杠等危险字符与写操作、`UNION`/`INTO OUTFILE`/`LOAD_FILE`/`information_schema`/`SLEEP` 等关键字；词法级提取 `FROM`/`JOIN` 表名并与 `office.tool.sql.allowed-tables` **精确相等**比对，拒绝子查询/派生表与 schema 限定名。
  - **生产纵深防御建议**：为本 Tool 单独配置一个只读数据库账号，仅 `GRANT SELECT` 白名单表/列，即便应用层校验被绕过也无法触达 `sys_user` 等敏感表。
- **`HttpInternalTool`（内部系统调用）**：目标主机受 `office.tool.http.allowed-hosts` 白名单约束，防 SSRF。
- **工具鉴权**：所有工具经 `ToolSecuritySupport` 基于 `ToolContext`（服务端注入的 userId/role，模型不可伪造）做 RBAC 校验。
- **接口鉴权**：JWT + ADMIN/EMPLOYEE RBAC；演示口令 `{noop}` 前缀仅限本机，生产一律 BCrypt。

## 测试

```bash
mvn -f projects/office-agent-assistant/pom.xml clean test
```

- 单元测试：AuthService、SqlQueryTool
- Testcontainers：MySQL + Redis 集成测试（需 Docker）
- 模型测试：`AI_DASHSCOPE_API_KEY` 存在时运行 ModelIntegrationTest

UAT（自启应用 + curl + `mvn test`）：`bash projects/office-agent-assistant/scripts/uat-office-agent.sh`  
前置：`docker compose -f docker/docker-compose.yml -f projects/office-agent-assistant/docker-compose.override.yml --profile core --profile office up -d`；有 `AI_DASHSCOPE_API_KEY` 时覆盖 chat/tasks/approvals。
