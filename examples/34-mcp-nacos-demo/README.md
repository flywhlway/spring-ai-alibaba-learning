# 34-mcp-nacos-demo

MCP Server + Nacos MCP Registry 注册发现（对应教程第 12 章 §12.7–12.9）。

双模块：
| 模块 | 端口 | 职责 |
|---|---|---|
| `order-mcp-server` | **18034** | 暴露 `getOrderStatus`，注册服务名 `order-service-mcp` |
| `office-assistant-client` | **18134**（Server+100） | 按服务名发现并调用，不硬编码 IP |

## 前置条件
- 中间件：`bash scripts/infra.sh up cloud`（Nacos）
- 环境变量：Client 需要 `AI_DASHSCOPE_API_KEY`
- Nacos 开发凭证：`nacos` / `nacos`（**生产必须更换**，见 T-34-01）

## 启动顺序
```bash
bash scripts/infra.sh up cloud

cd examples/34-mcp-nacos-demo/order-mcp-server
mvn spring-boot:run

cd examples/34-mcp-nacos-demo/office-assistant-client
mvn spring-boot:run
```

登录 Nacos 控制台（<http://localhost:8080>），在 AI → MCP 服务列表中应能看到 `order-service-mcp`。

## 快速验证
```bash
curl "http://localhost:18134/ask?question=帮我查一下订单SO20260704001的状态"
```

预期输出（节选）：
```json
{"code":0,"message":"success","data":"订单 SO20260704001 当前状态：配送中，预计明日送达"}
```

## 依赖说明（1.1.2.2）
教程中的 `spring-ai-alibaba-starter-nacos-mcp-server/client` 在 SAA 1.1.2.2 已更名为：
- Server：`spring-ai-alibaba-starter-mcp-registry`
- Client：`spring-ai-alibaba-starter-mcp-distributed`

配置键仍为 `spring.ai.alibaba.mcp.nacos.*`。

## 安全提示
Nacos 默认开发凭证仅用于本地学习，生产环境必须替换为强凭证并限制注册/发现权限。
