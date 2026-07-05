-- =====================================================================
-- 项目一 · AI 企业知识库问答平台 —— 演示数据
--
-- 密码采用 DelegatingPasswordEncoder 的 {noop} 前缀，仅限本机演示；
-- 生产环境一律 BCrypt（后续迭代的 UserAdminService 创建用户时强制加密）。
--
-- 演示账号：admin / admin123（管理员）、zhangsan / zhangsan123（员工）
-- =====================================================================

INSERT INTO sys_user (username, password_hash, display_name, role, department)
VALUES ('admin',    '{noop}admin123',    '平台管理员', 'ADMIN',    '信息技术部'),
       ('zhangsan', '{noop}zhangsan123', '张三',       'EMPLOYEE', '市场部'),
       ('lisi',     '{noop}lisi123',     '李四',       'EMPLOYEE', '研发部')
ON CONFLICT (username) DO NOTHING;

-- Prompt 模板（qa-system 已发布，其余为草稿演示版本流转）
INSERT INTO prompt_template (template_key, version, content, description, status, published_at, created_by)
VALUES ('qa-system', 1,
        E'你是企业知识库问答助手。仅依据提供的知识片段作答：\n1. 答案必须来自上下文，不得编造；\n2. 上下文不足时明确回答"知识库中未找到相关内容"；\n3. 回答末尾按 [1][2] 形式标注引用片段编号。\n\n知识片段：\n{context}',
        '问答系统提示词（带引用约束）', 'PUBLISHED', now(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('query-rewrite', 1,
        '将用户问题改写为适合向量检索的独立查询语句，保留关键实体与限定词，输出改写后的查询本身，不要解释。原问题：{query}',
        '检索前置的查询改写模板', 'PUBLISHED', now(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('qa-system', 2,
        E'你是企业知识库问答助手（V2 草稿：新增部门权限提示）。仅依据提供的知识片段作答……\n\n知识片段：\n{context}',
        '问答系统提示词 V2（灰度草稿）', 'DRAFT', NULL,
        (SELECT id FROM sys_user WHERE username = 'admin'))
ON CONFLICT (template_key, version) DO NOTHING;

-- 知识文档元数据示例（对应 MinIO 中的演示文件；INDEXED 表示已完成向量化）
INSERT INTO kb_document (title, category, file_name, content_type, file_size, minio_object, status, chunk_count, uploaded_by)
SELECT '员工差旅费报销制度（2026 版）', '公司制度', 'travel-expense-policy-2026.pdf',
       'application/pdf', 245760, 'policy/travel-expense-policy-2026.pdf', 'INDEXED', 18,
       (SELECT id FROM sys_user WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM kb_document WHERE file_name = 'travel-expense-policy-2026.pdf');

INSERT INTO kb_document (title, category, file_name, content_type, file_size, minio_object, status, chunk_count, uploaded_by)
SELECT '智能网关产品手册 V3.2', '产品手册', 'gateway-manual-v3.2.docx',
       'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 1048576,
       'manual/gateway-manual-v3.2.docx', 'INDEXED', 42,
       (SELECT id FROM sys_user WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM kb_document WHERE file_name = 'gateway-manual-v3.2.docx');

INSERT INTO kb_document (title, category, file_name, content_type, file_size, minio_object, status, chunk_count, uploaded_by)
SELECT '微服务接入规范', '技术文档', 'microservice-onboarding.md',
       'text/markdown', 65536, 'tech/microservice-onboarding.md', 'UPLOADED', 0,
       (SELECT id FROM sys_user WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM kb_document WHERE file_name = 'microservice-onboarding.md');

-- 审计日志示例
INSERT INTO audit_log (user_id, username, action, target, detail, client_ip, success)
SELECT u.id, u.username, 'UPLOAD_DOC', 'travel-expense-policy-2026.pdf',
       '{"category": "公司制度", "size": 245760}'::jsonb, '127.0.0.1', TRUE
FROM sys_user u WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM audit_log WHERE action = 'UPLOAD_DOC' AND target = 'travel-expense-policy-2026.pdf');
