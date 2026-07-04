# 24-milvus-demo

Milvus 后端的 `VectorStore` 演示：文档入库、相似度检索（响应含 `score`）与
Metadata Filter（对应教程第 11 章 §11.3）。

## 前置条件
- 中间件：`bash scripts/infra.sh up vector`
  - **冷启动约 30~60s**（依赖 etcd + MinIO 健康检查通过后 Milvus 才可用）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18024
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/documents` | `VectorStore.add` 入库 |
| GET | `/search?q=&topK=` | 相似度检索（含 score） |
| GET | `/search/filter?q=&topK=&department=` | Metadata Filter |

## 快速验证
```bash
curl -X POST http://localhost:18024/documents \
  -H 'Content-Type: application/json' \
  -d '{"content":"P0420故障码表示三元催化效率低于阈值","metadata":{"department":"vehicle-diag"}}'

curl "http://localhost:18024/search?q=催化效率低&topK=3"
```

## 源码导读
| 类 | 职责 |
|---|---|
| `MilvusController` | add / search，命中结果带 `score` |
| `application.yml` | embedding-dimension=1024、IVF_FLAT、COSINE |

## 运行结果
截图存放于 `images/examples/24-milvus-demo/`（真机运行后补充）。
