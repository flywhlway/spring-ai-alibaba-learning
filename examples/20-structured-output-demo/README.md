# 20-structured-output-demo

ChatClient `.entity(Record)` + `validateSchema()` 结构化输出演示（对应教程第 16 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18020
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/diagnose/structured?dtcCode=` | 故障码 → `DiagnosisResult`（Schema 校验失败自动重试） |

## 快速验证
```bash
curl "http://localhost:18020/diagnose/structured?dtcCode=P0420"
```

预期输出（`data` 字段结构，具体文案因模型而异）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "rootCause": "三元催化转化器效率低于阈值，通常由催化器老化或氧传感器误报引起",
    "confidenceScore": 75,
    "suggestedActions": [
      "使用诊断仪读取氧传感器实时数据，排除传感器误报",
      "检查是否存在伴随的失火故障码",
      "若确认催化器老化，更换三元催化转化器"
    ]
  }
}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `DiagnosisResult` | Record + `@JsonPropertyDescription` / `@JsonPropertyOrder` | §16.3 / §16.5 |
| `StructuredOutputController` | `.entity(DiagnosisResult.class)` + `StructuredOutputValidationAdvisor`（validateSchema 语义） | §16.7 / 可运行 Demo |

## 运行结果
截图存放于 `images/examples/20-structured-output-demo/`（真机运行后补充）。
