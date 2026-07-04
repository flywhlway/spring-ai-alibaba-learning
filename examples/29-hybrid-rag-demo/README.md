# 29-hybrid-rag-demo

Hybrid RAG + Citation：`RetrievalAugmentationAdvisor` 生成答案，应用层从检索文档的
`metadata.source` 拼接 `citations`（100% 准确，不依赖模型自觉标注）。对应教程第 09 章完整示例。

## 前置条件
- 中间件：`bash scripts/infra.sh up core vector`（PostgreSQL + Milvus + MinIO）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18029
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/ingest` | 写入样例知识库（教程场景文档） |
| GET | `/ask?question=` | 返回答案 + citations + evidenceCount |

## 快速验证
```bash
curl -X POST http://localhost:18029/ingest
curl "http://localhost:18029/ask?question=OTA升级失败一般是什么原因？"
```

### 预期输出（data 字段）
```json
{
  "answer": "OTA 升级失败常见原因包括：网络中断导致包体传输不完整、签名校验失败、存储空间不足...",
  "citations": ["OTA故障排查手册.pdf", "车联网平台运维规范.docx"],
  "evidenceCount": 4
}
```

响应外层为统一 `Result`：`{"code":0,"message":"success","data":{...}}`。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `RagController` | Retriever 取证据 → Advisor 生成 → 应用层拼 citations | §9.5 + 可运行 Demo |
