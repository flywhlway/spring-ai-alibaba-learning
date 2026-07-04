# 30-rag-eval-demo

RAG 评测最小实现：对固定问答集计算**忠实度（faithfulness）**与**相关性（relevance）**，
并演示内存响应缓存以降本。对应教程第 09 章评测/缓存叙述。

## 指标含义与局限
| 指标 | 计算方式 | 局限 |
|---|---|---|
| 忠实度 faithfulness | 答案命中「证据关键词」的比例；无关问题若正确拒绝编造则记 1.0 | 规则启发式，非语义级；关键词表需人工维护 |
| 相关性 relevance | 答案命中「问题相关关键词」的比例 | 无法识别同义改写；生产应考虑 LLM-as-judge 或专用评测集 |

本 Demo 优先可编译、可回归的最小实现，不引入额外评测框架。

## 前置条件
- 中间件：`bash scripts/infra.sh up vector`
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18030
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/eval/run` | 运行内置（或自定义）问答集，返回评分列表 |
| POST | `/eval/cache/clear` | 清空响应缓存 |

## 快速验证
```bash
# 首次运行：调用模型，cached=false
curl -X POST http://localhost:18030/eval/run -H 'Content-Type: application/json'

# 再次运行：相同问题命中缓存，cached=true
curl -X POST http://localhost:18030/eval/run -H 'Content-Type: application/json'
```

预期：`scores` 中含 `faithfulness` / `relevance`；第二次请求对应条目 `cached=true`。

## 源码导读
| 类 | 职责 |
|---|---|
| `RagEvalService` | 内置用例、规则评分、内存缓存 |
| `RagEvalController` | `/eval/run`、`/eval/cache/clear` |
