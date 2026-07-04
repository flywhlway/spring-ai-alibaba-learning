# 31-mcp-server-demo

Spring AI MCP Server：通过 Streamable HTTP 暴露 `@McpTool`（对应教程第 12 章 §12.3）。

## 前置条件
- 中间件：**无**
- 环境变量：无（本 Demo 不调用大模型）

## 运行
```bash
mvn spring-boot:run    # 端口 18031
```

MCP endpoint：`http://localhost:18031/mcp`（Streamable HTTP）。

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 进程健康检查 |
| * | `/mcp` | MCP Streamable HTTP 协议端点 |

## 快速验证
```bash
curl "http://localhost:18031/health"
```

预期输出（节选）：
```json
{"code":0,"message":"success","data":"mcp-server-demo ready, endpoint=/mcp"}
```

Client 侧消费见 `32-mcp-client-demo`（先启动本服务）。

## 安全提示
本 Demo **未做鉴权**，仅供本地学习。生产环境必须结合传输层鉴权（见 `33-mcp-auth-demo` 的 Bearer `TransportContextExtractor`），不能假设内网即安全。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `OrderTools` | `@McpTool` 暴露订单状态查询 | §12.3 |
| `application.yml` | `protocol: STREAMABLE`，默认 endpoint `/mcp` | §12.2 |

## 运行结果
截图存放于 `images/examples/31-mcp-server-demo/`（真机运行后补充）。
