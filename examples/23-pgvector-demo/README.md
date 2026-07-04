# 23-pgvector-demo

PostgreSQL + pgvector 后端的 `VectorStore` 演示：文档入库、相似度检索与
`FilterExpressionBuilder` Metadata Filter（对应教程第 11 章 §11.4）。

## 前置条件
- 中间件：`bash scripts/infra.sh up core`（PostgreSQL / pgvector）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18023
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/documents` | `VectorStore.add` 入库 |
| GET | `/search?q=&topK=` | 相似度检索 |
| GET | `/search/filter?q=&topK=&department=` | Metadata Filter（FilterExpressionBuilder） |

## 快速验证
```bash
curl -X POST http://localhost:18023/documents \
  -H 'Content-Type: application/json' \
  -d '{"content":"OTA升级失败常见原因包括网络中断、签名校验失败","metadata":{"department":"vehicle-diag"}}'

curl "http://localhost:18023/search?q=升级中断怎么办&topK=3"

curl "http://localhost:18023/search/filter?q=升级中断&topK=3&department=vehicle-diag"
```

## 源码导读
| 类 | 职责 |
|---|---|
| `PgVectorController` | add / similaritySearch / FilterExpressionBuilder |
| `application.yml` | dimensions=1024、HNSW、initialize-schema |

## 运行结果
截图存放于 `images/examples/23-pgvector-demo/`（真机运行后补充）。
