# 27-rag-demo

Naive RAG：`QuestionAnswerAdvisor` 一行挂载完成「检索 → 拼 Prompt → 生成」。
对应教程第 09 章 §9.3。默认向量后端为 Milvus，Embedding 使用 DashScope `text-embedding-v4`（1024 维）。

## 前置条件
- 中间件：`bash scripts/infra.sh up vector`（Milvus + etcd + MinIO）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18027
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/ingest` | 写入样例知识库文档（含 `metadata.source`） |
| GET | `/ask?question=` | Naive RAG 问答，返回答案字符串 |

## 快速验证
```bash
curl -X POST http://localhost:18027/ingest
curl "http://localhost:18027/ask?question=OTA升级失败一般是什么原因？"
```

预期：答案围绕网络中断、签名校验、存储空间等知识库内容；若问题与知识库无关，应回复「知识库中未找到相关信息」一类表述（T-27-01 空上下文兜底，不编造）。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `RagConfig` | `QuestionAnswerAdvisor` + system 提示仅依据知识库 | §9.3 |
| `RagController` | `/ingest` 入库、`/ask` 问答 | §9.3 |

## 与 Advanced RAG（28）的差异
本 Demo 使用黑盒式 `QuestionAnswerAdvisor`，无法插拔查询改写/重排序。需要精细控制时见 `28-advanced-rag-demo` 的 `RetrievalAugmentationAdvisor`。
