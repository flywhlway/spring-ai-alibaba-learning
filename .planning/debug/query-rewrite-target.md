# DEBUG: query-rewrite missing {target}

## Symptoms
- BeanCreationException on `retrievalAugmentationAdvisor`
- `IllegalArgumentException: The following placeholders must be present in the prompt template: target`
- Stack: `RewriteQueryTransformer.<init>` → `PromptAssert.templateHasRequiredPlaceholders`

## Root Cause
Spring AI 1.1.2 `RewriteQueryTransformer` 构造时强制要求模板同时含 `{target}` 与 `{query}`。  
当前 `query-rewrite`（classpath `prompts/query-rewrite.st` + `db/data.sql` PUBLISHED 种子）仅有 `{query}`。  
DB 种子优先于 classpath，故仅改 `.st` 不够，必须同步改 `db/data.sql`（及 test resources 若有副本）。

## Fix Direction
对齐 Spring AI 默认模板语义，例如：

```
Given a user query, rewrite it to provide better results when querying a {target}.
...
Original query:
{query}
```

或中文版保留 `{target}` + `{query}`。同步更新：
- `src/main/resources/prompts/query-rewrite.st`
- `db/data.sql`（及 `src/test/resources/db/data.sql` 若存在）
- 可选：对已存在库提供幂等 UPDATE 种子 / 文档说明需重跑 init
