---
status: resolved
resolved_at: 2026-07-18T02:30:00Z
source: .planning/debug/admin-rbac-403-vs-500.md
fix: 06-10 AccessDeniedExceptionHandler → HTTP 403
---

# DEBUG RESOLVED: admin RBAC 403 vs 500

**Root cause:** AuthorizationDeniedException 被 GlobalExceptionHandler 兜底为 500。  
**Fix:** 06-10 sibling RestControllerAdvice 映射 403；uat-smart-cs.sh RBAC 硬断言通过。  
**Closed at:** milestone Path A cleanup 2026-07-18
