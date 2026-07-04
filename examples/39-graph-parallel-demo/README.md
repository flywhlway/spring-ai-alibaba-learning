# 39-graph-parallel-demo

并行 `StateGraph`：`addEdge(START, List.of(...))` fan-out + `addEdge(List.of(...), merge)` fan-in（对应教程第 14 章并行诊断；**无** `addAggregatedEdge`）。

## 前置条件
- 中间件：无（Checkpoint 使用进程内 `MemorySaver`）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18039
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/graph/parallel?question=` | 并行检索知识库与历史工单，汇总后生成诊断建议 |

## 快速验证
```bash
curl "http://localhost:18039/graph/parallel?question=P0420故障码怎么处理"
```
预期输出（节选）：
```json
{"code":0,"message":"success","data":"...结合知识库与历史工单的诊断建议..."}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `ParallelGraphConfig` | `addEdge(START, List)` / `addEdge(List, merge)`（JAR 真 API） | §14.5 |
| `ParallelNodes` | searchKb / searchHistory（假数据）/ generateAnswer | §14 可运行 Demo |
| `ParallelController` | `invoke` → `Result` | §14 |

## 运行结果
截图存放于 `images/examples/39-graph-parallel-demo/`（真机运行后补充）。
