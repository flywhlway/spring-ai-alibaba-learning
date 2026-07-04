# 07-prompt-builder-demo

Prompt 组装器与版本化管理：按 `name@version` 两级索引注册/查询/渲染 `PromptTemplate`，
演示教程第 05 章 §5.3 提到的"版本化管理"第②级（外部化管理）向第③级（配置中心热更新，
见 `examples/08-prompt-nacos-demo`）过渡前的中间形态。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18007
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/prompts` | 注册一个 `name@version` 模板 |
| GET | `/prompts/{name}` | 列出该 name 下所有已注册版本 |
| GET | `/prompts/{name}/{version}` | 查询指定版本模板原文 |
| POST | `/prompts/{name}/{version}/render` | 仅渲染变量，不调用模型 |
| POST | `/prompts/{name}/{version}/invoke` | 渲染后调用模型返回结果 |

## 快速验证
```bash
curl -X POST http://localhost:18007/prompts \
  -H 'Content-Type: application/json' \
  -d '{"name":"greeting","version":"v1","template":"你好，我是{assistant_name}，很高兴为你服务。"}'

curl -X POST http://localhost:18007/prompts \
  -H 'Content-Type: application/json' \
  -d '{"name":"greeting","version":"v2","template":"Hi～我是{assistant_name}，有什么可以帮你？"}'

curl http://localhost:18007/prompts/greeting
# {"code":0,"message":"success","data":["v1","v2"]}

curl -X POST http://localhost:18007/prompts/greeting/v1/invoke \
  -H 'Content-Type: application/json' \
  -d '{"params":{"assistant_name":"小艾"}}'
```
预期输出（节选）：
```json
{"code":0,"message":"success","data":"你好，我是小艾的助手..."}
```
查询不存在的 name/version 返回 `code=1003`（`CommonResultCode.NOT_FOUND`）。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `PromptRegistry` | 内存版 `name -> version -> 模板原文` 注册表 + 渲染 | §5.3 |
| `PromptBuilderController` | 注册/查询/渲染/调用 REST 入口 | §5.3 |

## 运行结果
截图存放于 `images/examples/07-prompt-builder-demo/`（真机运行后补充）。
