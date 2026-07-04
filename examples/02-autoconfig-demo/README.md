# 02-autoconfig-demo

手写一个规范的 Spring Boot 自动装配模块，复现 `DashScopeApi → DashScopeChatModel → ChatClient.Builder`
装配链路的三要素：属性绑定、`@ConditionalOnProperty` 开关、`@ConditionalOnMissingBean` 谦让（对应教程第 03 章）。
不调用任何模型，是第 19 章仓库自建 starter 的原型。

## 前置条件
- 中间件：无
- 环境变量：无

## 运行
```bash
mvn spring-boot:run    # 端口 18002
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/greet?name=` | 返回按配置前缀拼装的问候语 |

## 快速验证
```bash
curl "http://localhost:18002/greet?name=flywhl"
```
预期输出：
```text
你好, flywhl! (locale=zh-CN)
```

验证条件装配报告（负面匹配定位）：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--debug" 2>&1 | grep -A 3 "GreetingAutoConfiguration"
```
反向验证：将 `application.yml` 的 `enabled: false`，重启后 Controller 会因 `GreetingService` 缺失而启动期报错——
这正是排查"Bean 未装配"的黄金线索。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `GreetingProperties` | record 属性类 + 紧凑构造器兜底默认值 | §3.5 |
| `GreetingAutoConfiguration` | 装配类本体，三大条件注解 | §可运行 Demo |
| `AutoConfiguration.imports` | Boot 3 装配钩子（取代 spring.factories） | §3.1 |

## 运行结果
截图存放于 `images/examples/02-autoconfig-demo/`（真机运行后补充）。
