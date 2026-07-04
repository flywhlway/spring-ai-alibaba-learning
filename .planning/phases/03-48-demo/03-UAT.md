---
status: complete
phase: 03-48-demo
source:
  - 03-01-SUMMARY.md
  - 03-02-SUMMARY.md
  - 03-03-SUMMARY.md
  - Claude-Code-interrupt-log (05-19 parallel agents)
started: 2026-07-04T21:47:00+08:00
updated: 2026-07-04T21:55:00+08:00
scope: batch1-extended (demos 01-19)
---

## Current Test

number: done
name: Automated acceptance for demos 01-19
expected: |
  每个 Demo 具备 pom/README/api.http/application.yml，端口 180NN，
  mvn compile 通过，无废弃 API/硬编码密钥/TODO。
awaiting: none

## Tests

### 1. Demo 01-08 结构与编译
expected: 首批已交付 Demo 均可 compile，交付物齐全
result: pass

### 2. Demo 11/14/15/16 结构与编译
expected: 限额中断残留中较完整的 Demo 可 compile
result: pass

### 3. Demo 09-advisor-demo
expected: 内置 Advisor 链（SafeGuard + SimpleLogger）可运行
result: pass
fixed: "新建完整工程"

### 4. Demo 10-custom-advisor-demo
expected: 自定义 AuditLoggingAdvisor（CallAdvisor）可运行
result: pass
fixed: "新建完整工程，使用 CallAdvisor 非废弃 API"

### 5. Demo 12-dynamic-tool-demo 交付物
expected: README + api.http + application.yml 齐全
result: pass
fixed: "补齐交付物"

### 6. Demo 13-http-tool-demo
expected: HTTP Tool 封装完整工程
result: pass
fixed: "新建 RestClient + MockStockApiController"

### 7. Demo 17-redis-memory-demo
expected: 自定义 Redis ChatMemoryRepository 完整可运行
result: pass
fixed: "补齐 Repository/Config/Controller/yml/README"

### 8. Demo 18-jdbc-memory-demo
expected: JDBC 记忆仓库完整工程（教程权威规格）
result: pass
fixed: "按教程规格新建"

### 9. Demo 19-summary-memory-demo
expected: 摘要压缩长对话 Demo
result: pass
fixed: "SummaryCompressingAdvisor + MessageChatMemoryAdvisor"

### 10. 全量 01-19 compile 门禁
expected: 19 个 Demo 全部 mvn compile 退出码 0
result: pass
evidence: "2026-07-04 agent recompile all OK; banned API scan clean"

## Summary

total: 10
passed: 10
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[none — all diagnosed gaps closed in fix loop]
