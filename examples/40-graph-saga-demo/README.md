# 40-graph-saga-demo

最小 Saga：`deductInventory → chargePayment →` 条件边（成功 END / 失败 `compensateInventory`），内存 Map 模拟库存与扣款（对应教程第 14 章 §14.7）。

## 前置条件
- 中间件：无（进程内 `MemorySaver`；无外部事务中间件）
- 环境变量：`AI_DASHSCOPE_API_KEY`（DashScope starter 装配需要；本 Demo 节点不调用模型）

## 运行
```bash
mvn spring-boot:run    # 端口 18040
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/graph/saga?orderId=&forceFail=` | `forceFail=true` 触发扣款失败与库存补偿 |

## 快速验证
```bash
# 成功路径（不补偿）
curl "http://localhost:18040/graph/saga?orderId=ORD-1&forceFail=false"

# 失败路径（补偿库存）
curl "http://localhost:18040/graph/saga?orderId=ORD-2&forceFail=true"
```
预期输出（失败路径节选）：
```json
{"code":0,"message":"success","data":{"orderId":"ORD-2","paymentSuccess":false,"compensated":true,"message":"已补偿库存...","inventory":10}}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `SagaGraphConfig` | `addConditionalEdges` + 补偿节点 | §14.7 |
| `SagaNodes` | 内存库存/扣款模拟 | §14.7 |
| `SagaController` | `forceFail` 查询参数 → `SagaOutcome` | §14.7 |

## 运行结果
截图存放于 `images/examples/40-graph-saga-demo/`（真机运行后补充）。
