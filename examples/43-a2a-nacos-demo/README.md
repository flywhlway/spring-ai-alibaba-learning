# 43-a2a-nacos-demo

A2A Server + Nacos 注册发现 + Client 远程调用（对应教程第 15 章 §15.9）。

双模块：
| 模块 | 端口 | 职责 |
|---|---|---|
| `inventory-a2a-server` | **18043** | 暴露库存查询 ReactAgent，注册 AgentCard 到 Nacos |
| `office-a2a-client` | **18143**（Server+100） | `A2aRemoteAgent` + `NacosAgentCardProvider` 发现并调用 |

## 前置条件
- 中间件：`bash scripts/infra.sh up cloud`（Nacos）
- 环境变量：Client 需要 `AI_DASHSCOPE_API_KEY`
- Nacos 开发凭证：`nacos` / `nacos`（**生产必须更换**，见 T-43-01）

## 启动顺序
```bash
bash scripts/infra.sh up cloud

cd examples/43-a2a-nacos-demo/inventory-a2a-server
mvn spring-boot:run

cd examples/43-a2a-nacos-demo/office-a2a-client
mvn spring-boot:run
```

登录 Nacos 控制台（<http://localhost:8080>），在 AI → Agent 服务列表中应能看到 `inventory-agent`。

## 快速验证
```bash
curl "http://localhost:18143/a2a/inventory?query=查询SKU-001库存"
```

预期输出（节选）：
```json
{"code":0,"message":"success","data":"SKU-001 当前库存 120 件..."}
```

## 手动验证清单
- [ ] Nacos 控制台可见 `inventory-agent` AgentCard
- [ ] Server 18043 启动无报错（A2A JSON-RPC 端点就绪）
- [ ] Client curl 返回非空 `data`（需 Key + Nacos + Server 均已启动）
- [ ] Client 配置使用 `spring.ai.alibaba.a2a.nacos.*`，**非** `mcp.nacos`

## 依赖说明（1.1.2.2）
- `spring-ai-alibaba-starter-a2a-nacos`：A2A Server/Client + Nacos 注册发现
- Client 使用 `A2aRemoteAgent.builder().agentCardProvider(nacosAgentCardProvider)`，**禁止** `nacosServiceName()`

## 安全提示
Nacos 默认开发凭证仅用于本地学习，生产环境必须替换为强凭证并限制注册/发现权限。禁止硬编码远程 Agent URL（T-43-02）。
