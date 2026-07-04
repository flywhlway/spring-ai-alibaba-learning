# common —— 公共模块（saa-learning-common）

全仓库基础设施 SSOT：统一响应协议、错误码契约、业务异常与全局异常处理。examples 与 projects 的所有工程均依赖本模块，禁止重复造轮子。

## 提供的能力

| 类 | 职责 |
|---|---|
| `result.Result<T>` | 统一响应体（record，不可变），`code=0` 成功；自动携带 MDC traceId |
| `result.PageResult<T>` | 统一分页结构，配合 `Result<PageResult<T>>` 使用 |
| `result.ResultCode` | 错误码契约接口，含全仓库分段规范（见 Javadoc） |
| `result.CommonResultCode` | 通用错误码（0 / 1000~1999 / 9000） |
| `exception.BizException` | 统一业务异常，可携带根因 |
| `exception.GlobalExceptionHandler` | `@RestControllerAdvice` 全局处理：业务异常 / 参数校验 / JSON 解析 / 兜底 |

## 使用方式

```xml
<dependency>
    <groupId>com.flywhl.saa</groupId>
    <artifactId>saa-learning-common</artifactId>
</dependency>
```

应用侧启用全局异常处理（组件扫描覆盖不到 common 包时）：

```java
@Import(GlobalExceptionHandler.class)
@SpringBootApplication
public class DemoApplication { }
```

## 设计说明

- `spring-boot-starter-web` / `validation` 声明为 **optional**：由最终应用决定 Web 栈，common 不向非 Web 模块传递依赖；
- 错误码按域分段（AI 2000 段 / RAG 3000 段 / Tool 4000 段 / Agent 5000 段），各领域枚举在对应章节模块中实现 `ResultCode` 扩展；
- 流式接口的 error 事件 payload 复用 `Result` 结构，保证前端错误处理逻辑唯一（约定见 docs/00-overview/03 §5.2）。

## 测试

```bash
mvn -pl common test
```
