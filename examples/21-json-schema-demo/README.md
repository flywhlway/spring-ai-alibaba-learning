# 21-json-schema-demo

JSON Schema 约束、嵌套泛型集合与 Schema 校验容错演示（对应教程第 16 章 §16.4 / §16.7）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18021
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/filmography?actors=` | `ParameterizedTypeReference<List<ActorFilms>>`（禁止 `List.class`） |
| GET | `/report/nested?topic=` | 嵌套 Record：`InspectionReport` → `List<InspectionFinding>` |
| GET | `/report/resilient?topic=` | validateSchema 失败后宽松 `.entity()` 回退 |

## 快速验证
```bash
curl "http://localhost:18021/filmography?actors=汤姆·汉克斯,比尔·默瑞"
curl "http://localhost:18021/report/nested?topic=电动汽车电池热管理"
curl "http://localhost:18021/report/resilient?topic=数据中心机房巡检"
```

预期 `/filmography`（`data` 结构，文案因模型而异）：
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {"actor": "汤姆·汉克斯", "movies": ["阿甘正传", "拯救大兵瑞恩", "荒岛余生"]},
    {"actor": "比尔·默瑞", "movies": ["土拨鼠之日", "迷失东京", "鬼戏人"]}
  ]
}
```

预期 `/report/resilient`（正常路径 `usedFallback=false`）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "report": {
      "topic": "数据中心机房巡检",
      "summary": "整体运行正常，存在少量可改进项",
      "findings": [
        {"item": "UPS 电池", "severity": "MEDIUM", "detail": "部分电池内阻偏高"}
      ]
    },
    "usedFallback": false,
    "path": "validateSchema"
  }
}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `ActorFilms` | 嵌套 `List<String>` 字段 | §16.4 |
| `InspectionReport` / `InspectionFinding` | 嵌套 Record 类型树 | §16.4 |
| `JsonSchemaController` | `ParameterizedTypeReference` + `StructuredOutputValidationAdvisor` + 容错回退 | §16.4 / §16.7 |

## 运行结果
截图存放于 `images/examples/21-json-schema-demo/`（真机运行后补充）。
