# 41-multi-agent-demo

`SequentialAgent` / `ParallelAgent` / `LlmRoutingAgent` / `LoopAgent` 四模式演示（对应教程第 15 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18041
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/multi/sequential?query=` | 顺序：改写 → 回答 |
| GET | `/multi/parallel?query=` | 并行检索知识库 + 工单历史 |
| GET | `/multi/routing?query=` | LLM 路由到售前/售后/技术支持 |
| GET | `/multi/loop?query=` | `CountLoopStrategy(2)` 有限次循环改进 |

## 快速验证
```bash
curl "http://localhost:18041/multi/sequential?query=P0420故障码怎么处理？"
curl "http://localhost:18041/multi/parallel?query=P0420故障码"
curl "http://localhost:18041/multi/routing?query=我想咨询退换货流程"
curl "http://localhost:18041/multi/loop?query=用三句话介绍Spring AI Alibaba"
```

预期：`{"code":0,"message":"success","data":"..."}` 且 `data` 非空。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `MultiAgentConfig` | 四模式 FlowAgent + 子 ReactAgent | §15.3~15.6 |
| `MultiAgentController` | `invoke` → `OverAllState` → `Result` | §15 |
| `FlowStateExtractor` | 从 state.messages 提取助手文本 | — |

## API 纠偏说明
- `LlmRoutingAgent` 使用 `.model().systemPrompt().subAgents()`，**非**教程 `routes(Map).routingPrompt()`
- `LoopAgent` 使用 `.subAgent().loopStrategy(new CountLoopStrategy(n))`，**非** `bodyAgent` / `exitCondition` / `.maxIterations`

## 冒烟 IT
```bash
mvn -f examples/41-multi-agent-demo/pom.xml test
```
