# 38-workflow-demo

线性 `StateGraph`：`START → rewrite → retrieve → generate → END`，进程内 `MemorySaver`（对应教程第 14 章基础图）。

## 前置条件
- 中间件：无（Checkpoint 使用进程内 `MemorySaver`）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18038
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/workflow/run?question=` | 线性图：改写查询 → 内存检索 → 模型生成 |

## 快速验证
```bash
curl "http://localhost:18038/workflow/run?question=P0420故障码是什么问题？"
```
预期输出（节选）：
```json
{"code":0,"message":"success","data":"...结合知识库证据的诊断说明..."}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `WorkflowGraphConfig` | `KeyStrategy.REPLACE` + 线性边 + `SaverConfig`/`MemorySaver` | §14.2~14.6 |
| `WorkflowNodes` | rewrite / retrieve（假数据）/ generate（ChatClient） | §14.3 |
| `WorkflowController` | `compiledGraph.invoke` → `Result` | §14 |

## 冒烟 IT
```bash
# 需 AI_DASHSCOPE_API_KEY
mvn -f examples/38-workflow-demo/pom.xml test
```

## 运行结果
截图存放于 `images/examples/38-workflow-demo/`（真机运行后补充）。
