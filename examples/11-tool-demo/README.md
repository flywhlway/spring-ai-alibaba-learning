# 11-tool-demo

`@Tool` 声明式工具定义、`ToolContext` 身份注入、`returnDirect` 结果控制（对应教程第 07 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18011
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tool/context?question=&userId=` | 身份由 `ToolContext` 服务端注入，模型不感知/不可伪造 |
| GET | `/tool/direct?city=` | 触发 `returnDirect=true` 工具，结果不经模型二次转述 |

## 快速验证
```bash
curl "http://localhost:18011/tool/context?question=帮我查一下我的会员等级&userId=u-1001"

curl "http://localhost:18011/tool/direct?city=上海"
```
预期输出（节选）：
```json
{"code":0,"message":"success","data":"您当前是钻石会员"}
{"code":0,"message":"success","data":"{\"city\":\"上海\",\"time\":\"2026-07-04T20:00:00\",\"timezone\":\"Asia/Shanghai\"}"}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `MemberTools` | `@Tool` 声明、`ToolContext` 读取、`returnDirect` 标记 | §7.2-7.5 |
| `ToolController` | `defaultTools()` 注册、`toolContext()` 传参 | §7.4 |

## 运行结果
截图存放于 `images/examples/11-tool-demo/`（真机运行后补充）。
