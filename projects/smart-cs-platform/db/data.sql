-- =====================================================================
-- 项目三 · 智能客服平台 —— 演示数据
--
-- 密码采用 DelegatingPasswordEncoder 的 {noop} 前缀，仅限本机演示；
-- 生产环境一律 BCrypt（后续迭代的 UserAdminService 创建用户时强制加密）。
--
-- 演示账号：admin/admin123（管理员）、agent1/agent123（坐席）、
--           customer1/customer123、customer2/customer123（客户）
-- =====================================================================

INSERT INTO sys_user (username, password_hash, display_name, role)
VALUES ('admin',     '{noop}admin123',    '平台管理员', 'ADMIN'),
       ('agent1',    '{noop}agent123',    '坐席-王芳', 'AGENT'),
       ('customer1', '{noop}customer123', '客户-陈明', 'CUSTOMER'),
       ('customer2', '{noop}customer123', '客户-刘颖', 'CUSTOMER')
ON CONFLICT (username) DO NOTHING;

-- ---------------------------------------------------------------
-- Prompt 模板（路由/FAQ 系统提示词已发布，查询改写模板已发布）
-- ---------------------------------------------------------------
INSERT INTO prompt_template (template_key, version, content, description, status, published_at, created_by)
VALUES ('cs-router-system', 1,
        E'根据用户问题选择唯一子智能体：\n- faq-agent：标准 FAQ、政策、流程类可检索问题\n- business-supervisor：订单/物流/售后/技术复杂问题\n- ticket-agent：明确要求建单、查单、催单\n- human-escalation-agent：用户要求人工、投诉升级',
        '客服意图路由系统提示词', 'PUBLISHED', now(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('faq-answer-system', 1,
        E'你是智能客服 FAQ 助手。仅依据提供的知识片段作答：\n1. 答案必须来自上下文，不得编造；\n2. 上下文不足时明确回答"暂未找到相关内容，建议转人工"；\n3. 语气友好简洁。\n\n知识片段：\n{context}',
        'FAQ 问答系统提示词', 'PUBLISHED', now(),
        (SELECT id FROM sys_user WHERE username = 'admin')),
       ('query-rewrite', 1,
        '将用户问题改写为适合向量检索的独立查询语句，保留关键实体与限定词，输出改写后的查询本身，不要解释。原问题：{query}',
        '检索前置的查询改写模板', 'PUBLISHED', now(),
        (SELECT id FROM sys_user WHERE username = 'admin'))
ON CONFLICT (template_key, version) DO NOTHING;

-- ---------------------------------------------------------------
-- 模型配置：FAQ 场景优先 qwen-plus，BUSINESS/TICKET 场景同款，DeepSeek 作为降级备份
-- ---------------------------------------------------------------
INSERT INTO model_profile (profile_key, provider, model_name, scene, priority, enabled, options_json)
VALUES ('dashscope-qwen-plus-faq', 'DASHSCOPE', 'qwen-plus', 'FAQ', 10, TRUE, '{"temperature": 0.3}'::jsonb),
       ('dashscope-qwen-plus-business', 'DASHSCOPE', 'qwen-plus', 'BUSINESS', 10, TRUE, '{"temperature": 0.5}'::jsonb),
       ('dashscope-qwen-plus-ticket', 'DASHSCOPE', 'qwen-plus', 'TICKET', 10, TRUE, '{"temperature": 0.2}'::jsonb),
       ('deepseek-chat-fallback', 'DEEPSEEK', 'deepseek-chat', 'BUSINESS', 5, TRUE, '{"temperature": 0.5}'::jsonb)
ON CONFLICT (profile_key) DO NOTHING;

-- ---------------------------------------------------------------
-- FAQ 种子数据（10+ 条，覆盖售前/售后/物流/账号/支付高频问题，含可缓存高频问）
-- ---------------------------------------------------------------
INSERT INTO faq_article (title, category, question, answer, status, chunk_count, created_by)
SELECT v.title, v.category, v.question, v.answer, 'INDEXED', 1, (SELECT id FROM sys_user WHERE username = 'admin')
FROM (VALUES
    ('退货政策', '售后', '收到商品后多久可以申请退货？',
     '自签收之日起 7 天内，商品保持未拆封、不影响二次销售的状态下均可申请无理由退货。请在「我的订单」中选择对应订单发起退货申请。'),
    ('退款到账时间', '售后', '退款审核通过后多久到账？',
     '退款审核通过后，原路退回一般 1~3 个工作日到账，具体以发卡行处理时效为准；如超过 5 个工作日未到账，请联系人工客服核实。'),
    ('物流查询', '物流', '如何查询我的订单物流信息？',
     '在「我的订单」中点击对应订单的「查看物流」即可实时查看物流轨迹；也可以直接告诉客服订单号，我们帮您查询。'),
    ('发货时效', '物流', '下单后多久发货？',
     '正常情况下，付款成功后 24 小时内发货（预售商品以商品详情页标注时效为准），偏远地区可能延长 1~2 天。'),
    ('修改收货地址', '物流', '订单已提交，能否修改收货地址？',
     '订单在「待发货」状态下可自助修改收货地址；若已发货，请联系人工客服协助与快递公司沟通改址，不保证一定能修改成功。'),
    ('账号密码重置', '账号', '忘记登录密码怎么办？',
     '在登录页点击「忘记密码」，通过绑定手机号或邮箱验证身份后即可重置密码；如手机号已停用，请联系人工客服协助找回。'),
    ('账号注销', '账号', '如何注销我的账号？',
     '在「设置-账号安全-注销账号」中发起申请，系统会核实账户下无未完成订单和纠纷后在 7 个工作日内完成注销。'),
    ('支付方式', '支付', '支持哪些支付方式？',
     '支持微信支付、支付宝、银行卡快捷支付以及平台余额支付；部分活动商品支持分期付款，具体以结算页展示为准。'),
    ('发票开具', '支付', '购买商品可以开发票吗？',
     '支持电子发票，可在订单完成后 30 天内于「我的订单-申请发票」中自助开具，纸质发票需联系人工客服评估是否支持。'),
    ('优惠券使用规则', '售前', '优惠券可以叠加使用吗？',
     '同一订单通常只能使用一张优惠券，具体是否可与其他活动叠加以优惠券详情页说明为准；满减券需订单金额达到门槛才能使用。'),
    ('商品保修政策', '售后', '商品质量问题保修期是多久？',
     '大部分电子类商品享受 1 年质保，具体保修期以商品详情页「售后保障」标注为准；非人为损坏在保修期内可免费维修或换新。'),
    ('人工客服接入', '售前', '怎样联系人工客服？',
     '在对话中直接说"转人工"或"我要投诉"，系统会自动为您创建工单并转接坐席，坐席上线后会尽快回复您。')
) AS v(title, category, question, answer)
WHERE NOT EXISTS (SELECT 1 FROM faq_article WHERE title = v.title);

-- ---------------------------------------------------------------
-- 历史会话与工单（2 条，覆盖 RESOLVED / PENDING_HUMAN 两种终态演示）
-- ---------------------------------------------------------------
INSERT INTO cs_conversation (conversation_id, customer_id, assigned_agent_id, channel, title, message_count)
SELECT 'demo-conv-history-001',
       (SELECT id FROM sys_user WHERE username = 'customer1'),
       NULL, 'WEB', '退货申请咨询', 2
WHERE NOT EXISTS (SELECT 1 FROM cs_conversation WHERE conversation_id = 'demo-conv-history-001');

INSERT INTO cs_conversation (conversation_id, customer_id, assigned_agent_id, channel, title, message_count)
SELECT 'demo-conv-history-002',
       (SELECT id FROM sys_user WHERE username = 'customer2'),
       (SELECT id FROM sys_user WHERE username = 'agent1'),
       'WEB', '订单异常需人工处理', 4
WHERE NOT EXISTS (SELECT 1 FROM cs_conversation WHERE conversation_id = 'demo-conv-history-002');

INSERT INTO cs_ticket (ticket_no, conversation_id, customer_id, assigned_agent_id, status, priority, summary)
SELECT 'TKT-20260701-0001', 'demo-conv-history-001',
       (SELECT id FROM sys_user WHERE username = 'customer1'),
       NULL, 'RESOLVED', 'NORMAL', 'AI 已解答退货政策，客户确认无需人工介入'
WHERE NOT EXISTS (SELECT 1 FROM cs_ticket WHERE ticket_no = 'TKT-20260701-0001');

INSERT INTO cs_ticket (ticket_no, conversation_id, customer_id, assigned_agent_id, status, priority, summary)
SELECT 'TKT-20260702-0002', 'demo-conv-history-002',
       (SELECT id FROM sys_user WHERE username = 'customer2'),
       (SELECT id FROM sys_user WHERE username = 'agent1'),
       'PENDING_HUMAN', 'HIGH', '订单支付成功但库存系统未同步，AI 无法核实，已升级人工'
WHERE NOT EXISTS (SELECT 1 FROM cs_ticket WHERE ticket_no = 'TKT-20260702-0002');

INSERT INTO cs_ticket_event (ticket_id, from_status, to_status, actor, reason)
SELECT t.id, 'OPEN', 'AI_PROCESSING', 'SYSTEM', '客户发起对话，AI 自动接管'
FROM cs_ticket t WHERE t.ticket_no = 'TKT-20260701-0001'
  AND NOT EXISTS (SELECT 1 FROM cs_ticket_event e WHERE e.ticket_id = t.id AND e.to_status = 'AI_PROCESSING');

INSERT INTO cs_ticket_event (ticket_id, from_status, to_status, actor, reason)
SELECT t.id, 'AI_PROCESSING', 'RESOLVED', 'SYSTEM', 'FAQ 命中退货政策，客户确认满意'
FROM cs_ticket t WHERE t.ticket_no = 'TKT-20260701-0001'
  AND NOT EXISTS (SELECT 1 FROM cs_ticket_event e WHERE e.ticket_id = t.id AND e.to_status = 'RESOLVED');

INSERT INTO cs_ticket_event (ticket_id, from_status, to_status, actor, reason)
SELECT t.id, 'AI_PROCESSING', 'PENDING_HUMAN', 'customer2', '请求人工接管：订单支付成功但库存系统未同步'
FROM cs_ticket t WHERE t.ticket_no = 'TKT-20260702-0002'
  AND NOT EXISTS (SELECT 1 FROM cs_ticket_event e WHERE e.ticket_id = t.id AND e.to_status = 'PENDING_HUMAN');

-- ---------------------------------------------------------------
-- 消息归档示例（历史会话 001）
-- ---------------------------------------------------------------
INSERT INTO cs_message (conversation_id, role, content, route_agent, cache_hit, input_tokens, output_tokens, latency_ms)
SELECT 'demo-conv-history-001', 'USER', '收到商品后多久可以申请退货？', NULL, FALSE, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM cs_message WHERE conversation_id = 'demo-conv-history-001' AND role = 'USER');

INSERT INTO cs_message (conversation_id, role, content, route_agent, cache_hit, input_tokens, output_tokens, latency_ms)
SELECT 'demo-conv-history-001', 'ASSISTANT',
       '自签收之日起 7 天内，商品保持未拆封、不影响二次销售的状态下均可申请无理由退货。',
       'faq-agent', TRUE, 42, 58, 320
WHERE NOT EXISTS (SELECT 1 FROM cs_message WHERE conversation_id = 'demo-conv-history-001' AND role = 'ASSISTANT');

-- ---------------------------------------------------------------
-- 审计日志示例
-- ---------------------------------------------------------------
INSERT INTO audit_log (user_id, username, action, target, detail, client_ip, success)
SELECT u.id, u.username, 'CREATE_TICKET', 'TKT-20260702-0002',
       '{"priority": "HIGH", "reason": "库存系统未同步"}'::jsonb, '127.0.0.1', TRUE
FROM sys_user u WHERE u.username = 'customer2'
  AND NOT EXISTS (SELECT 1 FROM audit_log WHERE action = 'CREATE_TICKET' AND target = 'TKT-20260702-0002');
