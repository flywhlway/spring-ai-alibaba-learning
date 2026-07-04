# order-mcp-server

订单 MCP Server，注册到 Nacos 服务名 `order-service-mcp`，端口 **18034**。

## 前置条件
- `bash scripts/infra.sh up cloud`
- 无模型密钥

## 运行
```bash
mvn spring-boot:run
```

## 验证
```bash
curl "http://localhost:18034/health"
```

Nacos 控制台应出现 `order-service-mcp`（含 `getOrderStatus` 工具描述）。
