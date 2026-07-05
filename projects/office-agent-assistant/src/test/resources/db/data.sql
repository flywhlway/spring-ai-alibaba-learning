-- =====================================================================
-- 项目二 · 企业 AI Agent 办公助手 —— 演示数据（MySQL）
--
-- 演示账号：admin / admin123（管理员）、zhangsan / zhangsan123（员工）
-- 口令 {noop} 前缀仅限本机演示，生产一律 BCrypt。
-- =====================================================================

INSERT IGNORE INTO sys_user (username, password_hash, display_name, role, department, email)
VALUES ('admin',    '{noop}admin123',    '平台管理员', 'ADMIN',    '信息技术部', 'admin@corp.example.com'),
       ('zhangsan', '{noop}zhangsan123', '张三',       'EMPLOYEE', '市场部',     'zhangsan@corp.example.com'),
       ('lisi',     '{noop}lisi123',     '李四',       'EMPLOYEE', '销售部',     'lisi@corp.example.com');

-- 用户长期偏好（结构化长期记忆演示）
INSERT IGNORE INTO user_preference (user_id, pref_key, pref_value)
SELECT id, 'email-tone', '正式、简洁，落款"市场部 张三"' FROM sys_user WHERE username = 'zhangsan';
INSERT IGNORE INTO user_preference (user_id, pref_key, pref_value)
SELECT id, 'report-format', '三段式：今日完成 / 明日计划 / 风险与求助' FROM sys_user WHERE username = 'zhangsan';

-- Prompt 模板族（全部 PUBLISHED，供四类生成场景直接使用）
INSERT IGNORE INTO prompt_template (template_key, version, content, description, status, published_at, created_by)
VALUES ('meeting-summary', 1,
        '你是会议纪要助手。基于会议记录输出结构化 JSON：{"topic":主题,"decisions":[决议],"actions":[{"owner":负责人,"task":事项,"due":期限}],"risks":[风险]}。会议记录：\n{transcript}',
        '会议纪要结构化模板', 'PUBLISHED', NOW(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('daily-report', 1,
        '你是日报助手。根据用户口述与系统查询结果，按用户偏好格式（{format}）生成当日日报，语言精炼、量化优先。素材：\n{material}',
        '日报生成模板', 'PUBLISHED', NOW(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('email-draft', 1,
        '你是邮件起草助手。按语气偏好（{tone}）为用户起草中文商务邮件，输出 JSON：{"subject":主题,"body":正文}。需求：\n{request}',
        '邮件起草模板', 'PUBLISHED', NOW(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('approval-opinion', 1,
        '你是审批初审助手。对审批单进行合规性检查（预算、票据要求、审批链），输出 JSON：{"summary":要点,"compliance":合规结论,"suggestion":"APPROVE|REJECT|ESCALATE","reason":理由}。审批单：\n{request}',
        '审批初审意见模板', 'PUBLISHED', NOW(),
        (SELECT id FROM sys_user WHERE username = 'admin'));

-- 审批单演示（一单待 AI 初审、一单已完成流转）
INSERT IGNORE INTO approval_request (request_no, type, title, amount, content, status, applicant_id)
SELECT 'AP20260705-0001', 'EXPENSE', '华东客户拜访差旅报销', 3860.00,
       '7月1-3日拜访杭州/上海两地客户，高铁 1560 元，住宿两晚 1800 元，市内交通 500 元，发票齐全。',
       'PENDING', id
FROM sys_user WHERE username = 'zhangsan';

INSERT IGNORE INTO approval_request (request_no, type, title, amount, content, ai_summary, ai_opinion, status, applicant_id, approver_id)
SELECT 'AP20260703-0002', 'PURCHASE', '市场部宣传物料采购', 12800.00,
       '采购 Q3 展会宣传物料一批：易拉宝 20 个、宣传册 2000 份，三家比价后选定报价最低供应商。',
       '展会物料采购，金额 12800 元，已完成三家比价。',
       '金额超过 5000 元升级阈值，已按采购制度校验比价记录，建议 ESCALATE 至部门总监审批。',
       'APPROVED',
       (SELECT id FROM sys_user WHERE username = 'lisi'),
       (SELECT id FROM sys_user WHERE username = 'admin');

-- 日程演示（Calendar Tool 查询目标）
INSERT IGNORE INTO calendar_event (user_id, title, location, start_time, end_time, remark)
SELECT id, 'Q3 营销策略评审会', '3F 会议室 A', '2026-07-06 10:00:00', '2026-07-06 11:30:00', '携带 Q2 数据复盘'
FROM sys_user WHERE username = 'zhangsan';
INSERT IGNORE INTO calendar_event (user_id, title, location, start_time, end_time, remark)
SELECT id, '与供应商视频会议', '线上（腾讯会议）', '2026-07-07 14:00:00', '2026-07-07 15:00:00', NULL
FROM sys_user WHERE username = 'zhangsan';

-- 报表底数演示（SQL Tool 白名单查询目标）
INSERT IGNORE INTO report_sales (biz_date, region, product, sales_amount, order_count)
VALUES ('2026-07-03', '华东', '智能网关 Pro', 356000.00, 89),
       ('2026-07-03', '华南', '智能网关 Pro', 198000.00, 52),
       ('2026-07-04', '华东', '智能网关 Pro', 412000.00, 103),
       ('2026-07-04', '华南', '边缘计算盒子', 275000.00, 61),
       ('2026-07-04', '华北', '智能网关 Pro', 158000.00, 37);

INSERT IGNORE INTO report_attendance (biz_date, department, headcount, present, on_leave)
VALUES ('2026-07-04', '市场部', 18, 16, 2),
       ('2026-07-04', '销售部', 32, 30, 2),
       ('2026-07-04', '研发部', 45, 44, 1);
