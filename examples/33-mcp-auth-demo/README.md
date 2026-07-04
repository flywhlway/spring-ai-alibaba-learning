# 33-mcp-auth-demo

MCP Server Bearer 鉴权：`McpTransportContextExtractor` 从 HTTP `Authorization` 头提取 Token，
`@McpTool` 内校验（对应教程第 12 章 §12.4）。Token **仅来自传输层**，不接受模型参数伪造。

## 前置条件
- 中间件：无
- 环境变量：无

## 运行
```bash
mvn spring-boot:run    # 端口 18033
```

MCP endpoint：`http://localhost:18033/mcp`

演示密钥：`demo-secret`（请求头 `Authorization: Bearer demo-secret`）。

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查 |
| * | `/mcp` | MCP Streamable HTTP（需 Bearer） |

## 快速验证

带正确 Token（MCP Client / Inspector 配置 Header）：
```bash
# 健康检查无需鉴权
curl "http://localhost:18033/health"

# MCP 协议调用需携带：
# Authorization: Bearer demo-secret
curl -H "Authorization: Bearer demo-secret" "http://localhost:18033/health"
```

工具 `accessProtectedResource` 行为：
| Authorization | 工具返回 |
|---|---|
| 缺失 | `鉴权失败：缺少 Authorization 头...` |
| `Bearer wrong` | `鉴权失败：Token 无效...` |
| `Bearer demo-secret` | `Successfully accessed protected resource.` |

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `McpAuthConfig` | `McpTransportContextExtractor` + 自定义 Transport Provider | §12.4 |
| `ProtectedTools` | 从 `McpSyncRequestContext.transportContext()` 读 Token | §12.4 |

## 运行结果
截图存放于 `images/examples/33-mcp-auth-demo/`（真机运行后补充）。
