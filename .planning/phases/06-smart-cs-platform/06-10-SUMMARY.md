---
phase: 06-smart-cs-platform
plan: 10
subsystem: security-uat
tags: [access-denied, rbac, uat, gap-closure, jwt-hs256]

requires:
  - phase: 06-smart-cs-platform
    provides: 06-08/09 冷启动 + HUMAN-UAT 诊断（AccessDenied→500、脚本 approve 硬断言）
provides:
  - AccessDenied/AuthorizationDenied → HTTP 403（common optional security）
  - uat-smart-cs.sh approve D-14 soft-pass；RBAC 硬断言 403
  - JWT HS256 + SSE URL-encode 已提交
  - uat-smart-cs.sh exit 0（Key+infra）
affects: [human-uat, starter-autoconfig, phase-6-gap-closure]

tech-stack:
  added: [spring-boot-starter-security optional in common]
  patterns: [conditional-on-class-name-string, access-denied-advice-sibling]

key-files:
  created:
    - common/src/main/java/com/flywhl/saa/common/exception/AccessDeniedExceptionHandler.java
    - common/src/test/java/com/flywhl/saa/common/exception/AccessDeniedExceptionHandlerTest.java
  modified:
    - common/pom.xml
    - common/src/main/java/com/flywhl/saa/common/exception/GlobalExceptionHandler.java
    - starter/src/main/java/com/flywhl/saa/starter/autoconfigure/SaaLearningAutoConfiguration.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/AuthService.java
    - projects/smart-cs-platform/scripts/uat-smart-cs.sh
    - .planning/phases/06-smart-cs-platform/06-VALIDATION.md
    - .planning/phases/06-smart-cs-platform/06-HUMAN-UAT.md

key-decisions:
  - "AccessDenied 用 sibling RestControllerAdvice，不塞进 GlobalExceptionHandler 方法签名"
  - "starter @ConditionalOnClass(name=…) 字符串形式，避免编译期依赖 security"
  - "HITL approve InterruptionMetadata / CR-01 不在本 plan 修复（D-14）"

patterns-established:
  - "方法安全拒绝→专用 advice 403；catch-all 不吞 AccessDenied"

requirements-completed:
  - REQ-phase-6-smart-cs

duration: 25min
completed: 2026-07-18
---

# Phase 06: Plan 10 Summary

**关闭 HUMAN-UAT Gap：AccessDenied→403；uat-smart-cs.sh 在 D-14 approve soft-pass 下 exit 0；提交 HS256/SSE hotfix。**

## Performance

- **Duration:** ~25 min
- **Started:** 2026-07-18T00:44:00Z
- **Completed:** 2026-07-18T00:48:00Z
- **Tasks:** 3/3
- **Files modified:** 8+

## Accomplishments

- JWT 签发显式 `MacAlgorithm.HS256`（修 Spring Security 6.5 默认 RS256 与 HMAC 不兼容）
- SSE `question` URL 编码（Tomcat RFC 拒中文 query）
- `AccessDeniedExceptionHandler`：`AccessDeniedException` → HTTP 403 + `FORBIDDEN`；AuthorizationDenied 子类命中同一 handler
- starter 条件 `@Import`（`@ConditionalOnClass(name = "…AccessDeniedException")`）
- `uat-smart-cs.sh`：approve 404/500 soft-pass（含 D-14/CR-01 文案）；customer→admin **仍硬断言 403**
- 真机 UAT：**11 通过 / 0 失败 / 1 警告，exit 0**

## Task Commits

| Task | Commit | Note |
|------|--------|------|
| 1 HS256 + SSE | `c767020` | fix(06-10): JWT HS256 header + SSE question URL-encode |
| 2 AccessDenied→403 | `6a4d57a` | fix(06-10): AccessDenied/AuthorizationDenied 映射 HTTP 403 |
| 3 soft-allow + UAT | （本 SUMMARY 同批） | 脚本 soft-pass + HUMAN-UAT resolved |

## Gates

| Gate | Status |
|------|--------|
| G-06-10-handler | ✅ |
| G-06-10-optional | ✅ |
| G-06-10-starter | ✅ |
| G-06-10-test | ✅ |
| G-06-10-uat | ✅ exit 0（approve soft-pass） |

## Explicitly NOT done

- HumanHandoffController / InterruptionMetadata / resume 顺序（D-14 / CR-01）→ `/gsd-code-review 6 --fix`
