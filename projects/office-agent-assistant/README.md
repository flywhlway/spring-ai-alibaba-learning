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

完整示例见 [`http/api.http`](http/api.http)，OpenAPI 文档：`http://localhost:19200/doc.html`

## 测试

```bash
mvn -f projects/office-agent-assistant/pom.xml clean test
```

- 单元测试：AuthService、SqlQueryTool
- Testcontainers：MySQL + Redis 集成测试（需 Docker）
- 模型测试：`AI_DASHSCOPE_API_KEY` 存在时运行 ModelIntegrationTest

UAT：`bash projects/office-agent-assistant/scripts/uat-office-agent.sh`
