# 06-prompt-demo

`PromptTemplate` 模板渲染、Few-shot、CoT（Chain of Thought）、JSON 格式化输出（`BeanOutputConverter`）
四种经典 Prompt 范式演示（对应教程第 06 章 §5.1-5.2）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18006
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/prompt/template` | `PromptTemplate.render(Map)` 变量渲染后调用模型 |
| GET | `/prompt/few-shot?dtcCode=` | User/Assistant 示例对引导模型模仿输出风格 |
| GET | `/prompt/cot?question=` | System Prompt 引导模型分步推理 |
| GET | `/prompt/json?dtcCode=` | `BeanOutputConverter` 约束并解析为强类型 `FaultDiagnosis` |

## 快速验证
```bash
curl -X POST http://localhost:18006/prompt/template \
  -H 'Content-Type: application/json' \
  -d '{"domain":"车联网 OTA","log":"升级包下载超时，重试3次后失败"}'

curl "http://localhost:18006/prompt/few-shot?dtcCode=P0420"

curl "http://localhost:18006/prompt/cot?question=车辆频繁出现P0420故障码，可能是什么原因？"

curl "http://localhost:18006/prompt/json?dtcCode=P0420"
```
预期输出（`/prompt/json`，节选）：
```json
{"code":0,"message":"success","data":{"dtcCode":"P0420","possibleCauses":["三元催化器老化","氧传感器故障","排气系统泄漏"],"severity":"中","suggestedAction":"检测氧传感器与三元催化器"}}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `PromptController` | 四种 Prompt 范式的 REST 入口 | §5.1-5.2 |
| `FaultDiagnosis` | JSON 结构化输出目标类型 | §5.2 |
| `TemplateRequest` | 模板渲染请求体 + 校验 | §5.1 |

## 运行结果
截图存放于 `images/examples/06-prompt-demo/`（真机运行后补充）。
