# 28-advanced-rag-demo

Advanced / Modular RAG：`RetrievalAugmentationAdvisor` 组装查询改写（`RewriteQueryTransformer`）
与分数重排序（`DocumentPostProcessor`）。对应教程第 09 章 §9.4。

## 相对 Naive RAG（27）的差异
| 能力 | 27 QuestionAnswerAdvisor | 28 RetrievalAugmentationAdvisor |
|---|---|---|
| 上手成本 | 一行挂载 | 需组装组件 |
| 查询改写 | 无 | `RewriteQueryTransformer`（口语 → 检索友好表述） |
| 重排序 | 无 | `DocumentPostProcessor` 按 score 截断 Top-K |
| 空上下文 | 靠 system 提示 | 默认 `allowEmptyContext(false)`，拒绝无证据作答 |

## 前置条件
- 中间件：`bash scripts/infra.sh up vector`
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18028
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/ingest` | 写入样例知识库文档 |
| GET | `/ask?question=` | Advanced RAG 问答 |

## 快速验证
```bash
curl -X POST http://localhost:18028/ingest
curl "http://localhost:18028/ask?question=车机升级挂了咋办？"
```

口语化问题会先经查询改写再检索，答案应围绕 OTA 失败原因。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `AdvancedRagConfig` | 组装 Rewrite + Retriever + Rerank | §9.4 |
| `AdvancedRagController` | `/ingest`、`/ask` | §9.4 |
