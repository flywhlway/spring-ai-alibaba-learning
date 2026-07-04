# office-assistant-client

办公助手 MCP Client，按服务名 `order-service-mcp` 发现工具，端口 **18134**。

## 前置条件
- Nacos 已启动，且 `order-mcp-server` 已注册
- `AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run
```

## 验证
```bash
curl "http://localhost:18134/ask?question=帮我查一下订单SO20260704001的状态"
```
