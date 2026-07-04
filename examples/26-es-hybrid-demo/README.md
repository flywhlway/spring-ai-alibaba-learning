# 26-es-hybrid-demo

Elasticsearch 后端的 `VectorStore` 演示：向量入库/检索、全文 match，以及应用层
**RRF（Reciprocal Rank Fusion）混合检索**（对应教程第 11 章 §11.7）。

## Spring AI 1.1.2 能力说明

`ElasticsearchVectorStore` 在 1.1.2 仅提供统一的 `similaritySearch`（向量近邻），
**不内置 Hybrid Search API**。本 Demo 的混合检索实现为：

1. **向量通道**：`VectorStore.similaritySearch`
2. **全文通道**：ES 原生 `match` 查询（`content` 字段，经 `RestClient` + Jackson 安全编码）
3. **融合**：RRF，`score(d) = Σ 1/(k + rank_i(d))`，`k=60`

## 前置条件
- 中间件：`bash scripts/infra.sh up search`（Elasticsearch `localhost:9200`）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18026
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/documents` | `VectorStore.add` 入库 |
| GET | `/search?q=&topK=` | 纯向量检索 |
| GET | `/search/fulltext?q=&topK=` | 纯全文 match |
| GET | `/search/hybrid?q=&topK=` | 向量 + 全文 RRF 混合检索 |
| GET | `/search/filter?q=&topK=&department=` | Metadata Filter |

## 快速验证
```bash
curl -X POST http://localhost:18026/documents \
  -H 'Content-Type: application/json' \
  -d '{"content":"P0420故障码表示三元催化效率低于阈值","metadata":{"department":"vehicle-diag"}}'

curl "http://localhost:18026/search?q=催化效率低&topK=3"
curl "http://localhost:18026/search/fulltext?q=P0420&topK=3"
curl "http://localhost:18026/search/hybrid?q=P0420催化效率&topK=3"
```

专有名词（如故障码 `P0420`）更适合全文通道；语义相近表述更适合向量通道；
`/search/hybrid` 融合两路结果。

## 源码导读
| 类 | 职责 |
|---|---|
| `EsHybridController` | REST：add / vector / fulltext / hybrid |
| `HybridSearchService` | 双通道检索 + RRF |
| `application.yml` | dimensions=1024、index `saa-hybrid` |

## 运行结果
截图存放于 `images/examples/26-es-hybrid-demo/`（真机运行后补充）。
