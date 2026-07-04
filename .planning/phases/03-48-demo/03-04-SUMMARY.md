---
phase: 03-48-demo
plan: 04
subsystem: examples
tags: [structured-output, entity, validateSchema, ParameterizedTypeReference, BeanOutputConverter, ChatClient]

requires:
  - phase: 03-48-demo
    provides: common Result/GlobalExceptionHandler 与 Demo 脚手架约定（01~19）
provides:
  - examples/20-structured-output-demo（Record + Schema 校验重试）
  - examples/21-json-schema-demo（嵌套泛型 + 校验容错回退）
affects: [03-05, 后续 Structured Output 消费方 Demo]

tech-stack:
  added: []
  patterns:
    - "StructuredOutputValidationAdvisor 实现 validateSchema 语义（Spring AI 1.1.2）"
    - "ParameterizedTypeReference 表达 List<T>，禁止 .entity(List.class)"
    - "Result<T> 包装结构化输出，校验失败后应用层宽松回退"

key-files:
  created:
    - examples/20-structured-output-demo/pom.xml
    - examples/20-structured-output-demo/src/main/java/com/flywhl/saa/structured/StructuredOutputController.java
    - examples/20-structured-output-demo/src/main/java/com/flywhl/saa/structured/DiagnosisResult.java
    - examples/20-structured-output-demo/src/test/java/com/flywhl/saa/structured/StructuredOutputDemoApplicationIT.java
    - examples/21-json-schema-demo/pom.xml
    - examples/21-json-schema-demo/src/main/java/com/flywhl/saa/jsonschema/JsonSchemaController.java
    - examples/21-json-schema-demo/src/main/java/com/flywhl/saa/jsonschema/model/ActorFilms.java
    - examples/21-json-schema-demo/src/main/java/com/flywhl/saa/jsonschema/model/InspectionReport.java
  modified: []

key-decisions:
  - "Spring AI 1.1.2 无 entity(Class, Consumer) 重载，用 StructuredOutputValidationAdvisor 落地 validateSchema"
  - "Demo 20 包名 com.flywhl.saa.structured（计划指定，非教程 structuredoutput）"
  - "Demo 21 /report/resilient 展示校验耗尽后的宽松 .entity() 回退"

patterns-established:
  - "validateSchema → StructuredOutputValidationAdvisor.builder().outputType(...).maxRepeatAttempts(n)"
  - "泛型集合结构化输出必须 ParameterizedTypeReference"

requirements-completed: [REQ-phase-3-demos]

duration: 4min
completed: 2026-07-04
---

# Phase 03 Plan 04: Structured Output Demos 20–21 Summary

**ChatClient `.entity(Record)` + `StructuredOutputValidationAdvisor`（validateSchema）与 `ParameterizedTypeReference` 嵌套泛型容错，交付可编译 Demo 20/21**

## Performance

- **Duration:** 4 min
- **Started:** 2026-07-04T14:35:55Z
- **Completed:** 2026-07-04T14:40:42Z
- **Tasks:** 2
- **Files modified:** 18

## Accomplishments

- 新建 `20-structured-output-demo`（端口 18020）：`DiagnosisResult` Record、`.entity()` + Schema 校验自动重试、`Result` 包装、冒烟 IT
- 新建 `21-json-schema-demo`（端口 18021）：`ParameterizedTypeReference<List<ActorFilms>>`、嵌套 `InspectionReport`、校验失败宽松回退
- 两 Demo 均 `mvn -f ... compile` 通过；密钥仅 `${AI_DASHSCOPE_API_KEY}`；README 声明无中间件

## Task Commits

Each task was committed atomically:

1. **Task 1: 新建 20-structured-output-demo** - `5f0d2cc` (feat)
2. **Task 2: 新建 21-json-schema-demo** - `50a28b0` (feat)

**Plan metadata:** （本 SUMMARY 提交）

## Files Created/Modified

- `examples/20-structured-output-demo/` — 故障码结构化诊断全工程
- `examples/21-json-schema-demo/` — 嵌套泛型与校验容错全工程

## Decisions Made

- Spring AI 1.1.2 的 `CallResponseSpec.entity` 仅有单参数重载；教程中的 `.entity(T.class, spec -> spec.validateSchema())` 映射为 `StructuredOutputValidationAdvisor`（教程正文亦写明底层由该 Advisor 实现）
- Demo 20 包根按计划使用 `com.flywhl.saa.structured`（教程示例为 `structuredoutput`）
- Demo 21 增加 `/report/resilient`：先 validateSchema，耗尽后宽松 `.entity()`，双失败抛 `BizException`

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] validateSchema fluent API 在 1.1.2 不存在**
- **Found during:** Task 1（编译 `entity(Class, Consumer)`）
- **Issue:** Spring AI 1.1.2 `CallResponseSpec` 仅有 `entity(Class)` / `entity(ParameterizedTypeReference)` / `entity(StructuredOutputConverter)`，无 `validateSchema()` Consumer 重载
- **Fix:** 使用 `StructuredOutputValidationAdvisor.builder().outputType(...).maxRepeatAttempts(3)` 挂到 `.advisors()`，再 `.entity(...)`；变量名 `validateSchema` 保留语义可读性
- **Files modified:** `StructuredOutputController.java`、`StructuredOutputDemoApplicationIT.java`、`JsonSchemaController.java`
- **Verification:** `mvn -f examples/20-structured-output-demo/pom.xml -q compile` 与 21 同命令均退出码 0
- **Committed in:** `5f0d2cc` / `50a28b0`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** 语义与教程/威胁模型一致（Schema 校验 + 自动重试），仅 API 形态适配锁死版本；无范围蔓延。

## Issues Encountered

- 教程可运行 Demo 代码片段使用尚未进入 1.1.2 的 fluent `validateSchema()`；已按锁死 BOM 改用 Advisor，并在 JavaDoc/README 标明映射关系。

## User Setup Required

真机 curl / 冒烟 IT 需环境变量 `AI_DASHSCOPE_API_KEY`；编译不依赖 Key。无中间件。

## Next Phase Readiness

- Demo 20/21 可独立 `spring-boot:run` + curl
- 后续 03-05+（Embedding/Vector/RAG 等）不依赖本计划产物，可并行

## Self-Check: PASSED

- 全部关键产物文件存在
- 提交 `5f0d2cc`、`50a28b0` 存在于 git log
- 无 TODO/FIXME/placeholder stub

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
