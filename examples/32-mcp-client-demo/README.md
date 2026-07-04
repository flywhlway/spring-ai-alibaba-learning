# 32-mcp-client-demo

MCP Client 消费本机 `31-mcp-server-demo` 暴露的工具（对应教程第 12 章 §12.5）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`
- **启动顺序**：先启动 `31-mcp-server-demo`（`http://localhost:18031/mcp`），再启动本服务

> 编译不依赖 31 运行；真机 curl 验证时必须先起 31。

## 运行
```bash
# 终端 1
cd examples/31-mcp-server-demo && mvn spring-boot:run

# 终端 2
cd examples/32-mcp-client-demo && mvn spring-boot:run    # 端口 18032
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/ask?question=` | 自然语言提问，模型经 MCP 调用 31 的工具 |
| POST | `/ask?question=` | 同上（也支持 body 文本） |

## 快速验证
```bash
curl "http://localhost:18032/ask?question=帮我查一下订单SO20260704001的状态"
```

预期输出（节选）：
```json
{"code":0,"message":"success","data":"订单 SO20260704001 当前状态：配送中，预计明日送达"}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `application.yml` | 仅配置一个连接 `http://localhost:18031` + `/mcp` | §12.5 |
| `McpClientController` | `SyncMcpToolCallbackProvider` 聚合远程工具 | §12.5 |

## 运行结果
截图存放于 `images/examples/32-mcp-client-demo/`（真机运行后补充）。
