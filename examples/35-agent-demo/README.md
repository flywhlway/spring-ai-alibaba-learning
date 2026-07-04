# 35-agent-demo

`ReactAgent` + `methodTools` + `ModelCallLimitHook` + `MemorySaver` 车辆故障诊断（对应教程第 13 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18035
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/agent/diagnose?query=` | ReactAgent ReAct 循环：先查故障码工具，再生成诊断建议 |

## 快速验证
```bash
curl "http://localhost:18035/agent/diagnose?query=车辆报P0420故障码，是什么问题？"
```
预期输出（节选）：
```json
{"code":0,"message":"success","data":"根据查询，P0420 故障码表示\"三元催化转化器效率低于阈值（1号库）\"..."}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `DtcLookupTools` | `@Tool` 故障码知识库 | §13 可运行 Demo |
| `VehicleDiagnosisAgentConfig` | `methodTools` + `ModelCallLimitHook`（替代伪 API `.maxIterations`） | §13.2 |
| `AgentController` | `agent.call` → `Result` | §13 |

## 冒烟 IT
```bash
# 需 AI_DASHSCOPE_API_KEY
mvn -f examples/35-agent-demo/pom.xml test
```

## 运行结果
截图存放于 `images/examples/35-agent-demo/`（真机运行后补充）。
