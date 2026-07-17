# Phase 06 · smart-cs-platform UAT 验收清单

> 端口 **19300**；演示账号 `admin/admin123`（ADMIN）、`agent1/agent123`（AGENT）、
> `customer1/customer123` / `customer2/customer123`（CUSTOMER）。
> 自动化脚本：`bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`
> （无 Key 仅 health+login+RBAC；有 Key 全量 ask/stream/handoff/dashboard）。

## 前置条件

```bash
# 1. 环境变量（DashScope Key 可选，无 Key 时跳过 ask/stream/handoff）
source scripts/setup-env.sh

# 2. 中间件（Milvus 冷启动 30~60s）
docker compose -f docker/docker-compose.yml \
  -f projects/smart-cs-platform/docker-compose.override.yml \
  --profile core --profile vector --profile search --profile cloud --profile smartcs up -d

# 3. 编译并启动
mvn -pl common,starter -am clean install -DskipTests
mvn -f projects/smart-cs-platform/pom.xml spring-boot:run
```

全接口对照：`projects/smart-cs-platform/http/api.http`。

---

## 0. 健康检查（无需鉴权）

```bash
curl -s http://localhost:19300/actuator/health | jq .
```

**预期：** `status` 为 `UP`。

```bash
curl -s http://localhost:19300/actuator/prometheus | head
```

**预期：** 含 `jvm_` / `http_server_` 等指标行；有流量后可见 `gen_ai.*`。

---

## 1. 认证域

### 1.1 管理员登录

```bash
curl -s -X POST http://localhost:19300/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq .
```

**预期：** `code=0`，`data.accessToken` 非空，`data.user.role=ADMIN`。

### 1.2 坐席登录

```bash
curl -s -X POST http://localhost:19300/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"agent1","password":"agent123"}' | jq .
```

**预期：** `code=0`，`data.user.role=AGENT`。

### 1.3 客户登录

```bash
curl -s -X POST http://localhost:19300/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer1","password":"customer123"}' | jq .
```

**预期：** `code=0`，`data.user.role=CUSTOMER`。

```bash
export ADMIN_TOKEN="<admin accessToken>"
export AGENT_TOKEN="<agent accessToken>"
export CUSTOMER_TOKEN="<customer accessToken>"
```

### 1.4 当前用户

```bash
curl -s http://localhost:19300/api/auth/me \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" | jq .
```

**预期：** `code=0`，`data.username=customer1`。

---

## 2. 会话域（需 `AI_DASHSCOPE_API_KEY`）

演示 FAQ 高频问：`收到商品后多久可以申请退货？`（对应 `db/data.sql` 退货政策种子）。

### 2.1 同步问答

```bash
curl -s -X POST http://localhost:19300/api/chat/ask \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"demo-conv-001","question":"收到商品后多久可以申请退货？"}' | jq .
```

**预期：** `code=0`，`data.answer` 非空，含 `routeAgent` / `cacheHit` / `usage` 字段。

### 2.2 SSE 流式对话

```bash
curl -sN -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H 'Accept: text/event-stream' \
  'http://localhost:19300/api/chat/stream?conversationId=demo-conv-001&question=如何查询我的订单物流信息'
```

**预期：** 出现 `event:message` 增量、`event:meta`（路由/usage）、`event:done`。

---

## 3. 工单域

### 3.1 客户创建工单

```bash
curl -s -X POST http://localhost:19300/api/tickets \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
    "conversationId": "demo-conv-001",
    "summary": "支付成功但订单状态一直显示待支付，请核实",
    "priority": "HIGH"
  }' | jq .
```

**预期：** `code=0`，`data.status=OPEN`，`data.ticketNo` 形如 `SCS-YYYYMMDD-####`。

### 3.2 按工单号查询

```bash
curl -s "http://localhost:19300/api/tickets/${TICKET_NO}" \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" | jq .
```

**预期：** `code=0`，返回对应工单。

### 3.3 坐席合法状态转移

```bash
curl -s -X PATCH "http://localhost:19300/api/tickets/${TICKET_ID}/transition" \
  -H "Authorization: Bearer ${AGENT_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"to":"AI_PROCESSING","reason":"坐席开始处理"}' | jq .
```

**预期：** `code=0`，`data.status=AI_PROCESSING`。非法边（如 `OPEN→CLOSED`）返回业务错误。

---

## 4. 人工接管（HITL）

### 4.1 触发人工升级

```bash
curl -s -X POST http://localhost:19300/api/handoff/start \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"demo-conv-001","query":"我要投诉，请转人工处理"}' | jq .
```

**预期：** `code=0`，工单进入 `PENDING_HUMAN` 或返回待审批引导。

### 4.2 坐席审批恢复

```bash
curl -s -X POST http://localhost:19300/api/handoff/approve \
  -H "Authorization: Bearer ${AGENT_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"threadId":"demo-conv-001"}' | jq .
```

**预期：** `code=0`（`threadId` 与 `conversationId` 一致）。

---

## 5. 后台-模型 / Prompt / FAQ（ADMIN）

### 5.1 模型配置列表

```bash
curl -s 'http://localhost:19300/api/admin/model-profiles?scene=FAQ' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`，含 `dashscope-qwen-plus-faq`。

### 5.2 Prompt 模板列表

```bash
curl -s 'http://localhost:19300/api/admin/prompts?templateKey=cs-router-system' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`，含已发布路由系统提示词。

### 5.3 FAQ 列表

```bash
curl -s 'http://localhost:19300/api/admin/faq?category=售后&status=INDEXED&page=1&size=10' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`，含「退货政策」等种子 FAQ。

---

## 6. 后台-看板（ADMIN）

```bash
curl -s 'http://localhost:19300/api/admin/dashboard/stats?days=7' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`，含会话量 / 成本 / 缓存命中 / 工单分布等统计字段。

### 6.1 RBAC：客户访问 admin 403

```bash
curl -s -o /dev/null -w '%{http_code}\n' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  'http://localhost:19300/api/admin/dashboard/stats?days=7'
```

**预期：** HTTP `403`。

---

## 7. HANDOFF §7 质量门禁（无 Key CI）

```bash
mvn -f projects/smart-cs-platform/pom.xml clean install
bash scripts/version-audit.sh
bash scripts/spring-ai-2-readiness.sh .
# 无废弃 API / 硬编码密钥 / TODO
rg -v '^#' projects/smart-cs-platform/src/main/java | rg -ci 'TODO|FIXME|SupervisorAgent|PromptChatMemoryAdvisor'
# 期望输出 0
```

---

## 备注

- Milvus 依赖 etcd + MinIO，冷启动约 30~60s，UAT 前确认 `19530` 健康。
- Redis 语义缓存需 Redis Stack（`6380`）；会话记忆用普通 Redis `6379`。
- 完整 HTTP 用例见 `projects/smart-cs-platform/http/api.http`。
