-- =====================================================================
-- 项目三 · 智能客服平台 —— PostgreSQL 业务库 DDL（SSOT）
--
-- 数据库：scs_platform（由 docker-compose.override.yml 的 scs-db-init 自动创建并执行本文件）
-- 约定：JPA ddl-auto=none，本文件是表结构唯一真源；实体类（后续迭代）必须与此对齐。
-- 向量数据不在此库：FAQ Chunk 向量存 Milvus（collection: scs_faq）+ Elasticsearch（index: scs-faq），
-- 语义缓存向量存 Redis Stack（6380），此处仅存元数据/业务实体。
--
-- @author flywhl
-- =====================================================================

-- ---------------------------------------------------------------
-- 用户与权限：客户/坐席/管理员三分 RBAC
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,                 -- DelegatingPasswordEncoder 格式（演示数据用 {noop}）
    display_name  VARCHAR(64)  NOT NULL,
    role          VARCHAR(32)  NOT NULL DEFAULT 'CUSTOMER',  -- CUSTOMER / AGENT / ADMIN
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
COMMENT ON TABLE sys_user IS '平台用户（客户/坐席/管理员）';

-- ---------------------------------------------------------------
-- FAQ 文档元数据：向量在 Milvus/ES，此表记录来源与索引状态
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS faq_article (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(256) NOT NULL,
    category     VARCHAR(64)  NOT NULL DEFAULT 'GENERAL',   -- 售前 / 售后 / 物流 / 账号 等
    question     TEXT         NOT NULL,
    answer       TEXT         NOT NULL,
    status       VARCHAR(32)  NOT NULL DEFAULT 'PENDING',   -- PENDING / INDEXED / FAILED
    chunk_count  INT          NOT NULL DEFAULT 0,
    fail_reason  VARCHAR(512),
    created_by   BIGINT       NOT NULL REFERENCES sys_user (id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_faq_article_status   ON faq_article (status);
CREATE INDEX IF NOT EXISTS idx_faq_article_category ON faq_article (category);
COMMENT ON TABLE faq_article IS 'FAQ 文档元数据（向量在 Milvus scs_faq / ES scs-faq）';

-- ---------------------------------------------------------------
-- FAQ Chunk 溯源：与 Milvus/ES 中的向量对应，供命中回查
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS faq_chunk (
    id           BIGSERIAL PRIMARY KEY,
    article_id   BIGINT      NOT NULL REFERENCES faq_article (id) ON DELETE CASCADE,
    milvus_pk    VARCHAR(64) NOT NULL UNIQUE,     -- Milvus 主键（Document.id）
    es_doc_id    VARCHAR(64),                     -- Elasticsearch 文档 ID
    seq_no       INT         NOT NULL,            -- 在原文档中的顺序
    text_preview VARCHAR(512) NOT NULL,           -- 前 512 字预览（完整文本存向量库 payload）
    token_count  INT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_faq_chunk_article ON faq_chunk (article_id);
COMMENT ON TABLE faq_chunk IS 'FAQ Chunk 溯源元数据（milvus_pk / es_doc_id 关联双向量库）';

-- ---------------------------------------------------------------
-- 会话：客户/坐席多轮对话入口，完整消息滚动窗口在 Redis，此处落库归档
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cs_conversation (
    id                 BIGSERIAL PRIMARY KEY,
    conversation_id    VARCHAR(64) NOT NULL UNIQUE,   -- 业务会话 ID（UUID，Redis Memory / Agent threadId 共用）
    customer_id        BIGINT      NOT NULL REFERENCES sys_user (id),
    assigned_agent_id  BIGINT      REFERENCES sys_user (id),
    channel            VARCHAR(32) NOT NULL DEFAULT 'WEB',
    title              VARCHAR(256),
    message_count      INT         NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cs_conversation_customer ON cs_conversation (customer_id);
CREATE INDEX IF NOT EXISTS idx_cs_conversation_agent     ON cs_conversation (assigned_agent_id);
COMMENT ON TABLE cs_conversation IS '客服会话（conversation_id 与 Agent threadId 一致，全链路 UUID）';

-- ---------------------------------------------------------------
-- 消息归档：记录路由 Agent、缓存命中、token 用量，供看板统计
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cs_message (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64)  NOT NULL,
    role            VARCHAR(16)  NOT NULL,          -- USER / ASSISTANT
    content         TEXT         NOT NULL,
    route_agent     VARCHAR(64),                    -- faq-agent / business-supervisor / ticket-agent / human-escalation-agent
    cache_hit       BOOLEAN      NOT NULL DEFAULT FALSE,
    input_tokens    INT          NOT NULL DEFAULT 0,
    output_tokens   INT          NOT NULL DEFAULT 0,
    latency_ms      BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cs_message_conversation ON cs_message (conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_cs_message_route_agent   ON cs_message (route_agent);
COMMENT ON TABLE cs_message IS '客服消息归档（含路由 Agent、语义缓存命中、token 用量）';

-- ---------------------------------------------------------------
-- 工单：状态机 OPEN → AI_PROCESSING → PENDING_HUMAN → HUMAN_HANDLING → RESOLVED → CLOSED
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cs_ticket (
    id                 BIGSERIAL PRIMARY KEY,
    ticket_no          VARCHAR(32) NOT NULL UNIQUE,     -- 业务工单号，如 TKT-20260706-0001
    conversation_id    VARCHAR(64) NOT NULL,
    customer_id        BIGINT      NOT NULL REFERENCES sys_user (id),
    assigned_agent_id  BIGINT      REFERENCES sys_user (id),
    status             VARCHAR(32) NOT NULL DEFAULT 'OPEN',    -- OPEN/AI_PROCESSING/PENDING_HUMAN/HUMAN_HANDLING/RESOLVED/CLOSED
    priority           VARCHAR(16) NOT NULL DEFAULT 'NORMAL',  -- LOW/NORMAL/HIGH/URGENT
    summary            VARCHAR(512) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cs_ticket_conversation ON cs_ticket (conversation_id);
CREATE INDEX IF NOT EXISTS idx_cs_ticket_status        ON cs_ticket (status);
CREATE INDEX IF NOT EXISTS idx_cs_ticket_customer       ON cs_ticket (customer_id);
COMMENT ON TABLE cs_ticket IS '工单（服务端状态机校验，禁止客户端直写 status）';

-- ---------------------------------------------------------------
-- 工单状态机审计轨迹
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cs_ticket_event (
    id           BIGSERIAL PRIMARY KEY,
    ticket_id    BIGINT      NOT NULL REFERENCES cs_ticket (id) ON DELETE CASCADE,
    from_status  VARCHAR(32),
    to_status    VARCHAR(32) NOT NULL,
    actor        VARCHAR(64) NOT NULL,          -- 用户名 或 SYSTEM/AGENT_TOOL
    reason       VARCHAR(512),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cs_ticket_event_ticket ON cs_ticket_event (ticket_id, created_at);
COMMENT ON TABLE cs_ticket_event IS '工单状态转移审计（谁在何时把工单从什么状态转到什么状态）';

-- ---------------------------------------------------------------
-- Prompt 模板：数据库版本化（发布后经 Nacos 推送热更新）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prompt_template (
    id           BIGSERIAL PRIMARY KEY,
    template_key VARCHAR(128) NOT NULL,            -- 如 cs-router-system / faq-answer / query-rewrite
    version      INT          NOT NULL DEFAULT 1,
    content      TEXT         NOT NULL,
    description  VARCHAR(256),
    status       VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',  -- DRAFT / PUBLISHED / ARCHIVED
    published_at TIMESTAMPTZ,
    created_by   BIGINT       NOT NULL REFERENCES sys_user (id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (template_key, version)
);
CREATE INDEX IF NOT EXISTS idx_prompt_key_status ON prompt_template (template_key, status);
COMMENT ON TABLE prompt_template IS 'Prompt 模板版本化管理（PUBLISHED 版本推送 Nacos 热更新）';

-- ---------------------------------------------------------------
-- 模型配置：场景化路由（FAQ/BUSINESS/TICKET），发布时同步推 Nacos scs.model.profiles
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS model_profile (
    id           BIGSERIAL PRIMARY KEY,
    profile_key  VARCHAR(128) NOT NULL UNIQUE,     -- 如 dashscope-qwen-plus-faq
    provider     VARCHAR(32)  NOT NULL,             -- DASHSCOPE / DEEPSEEK
    model_name   VARCHAR(64)  NOT NULL,             -- qwen-plus / deepseek-chat 等
    scene        VARCHAR(32)  NOT NULL,             -- FAQ / BUSINESS / TICKET
    priority     INT          NOT NULL DEFAULT 0,   -- 同 scene 下越大越优先
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    options_json JSONB,                             -- temperature / topP 等模型参数覆盖
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_model_profile_scene ON model_profile (scene, enabled, priority);
COMMENT ON TABLE model_profile IS '模型配置 CRUD（scene 驱动运行时路由，发布可推 Nacos scs.model.profiles）';

-- ---------------------------------------------------------------
-- 审计日志：后台操作与 AI 调用双轨审计（starter AuditLoggingAdvisor 落库端）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(64),
    action      VARCHAR(64)  NOT NULL,          -- LOGIN / CREATE_TICKET / HANDOFF_APPROVE / PUBLISH_PROMPT / AI_CALL ...
    target      VARCHAR(256),
    detail      JSONB,
    client_ip   VARCHAR(64),
    success     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_log_action_time ON audit_log (action, created_at);
COMMENT ON TABLE audit_log IS '审计日志（后台操作 + AI 调用 + 工单/HITL 关键事件）';

-- ---------------------------------------------------------------
-- 满意度反馈：客户对答案/工单处理点赞/点踩
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cs_feedback (
    id          BIGSERIAL PRIMARY KEY,
    message_id  BIGINT      NOT NULL REFERENCES cs_message (id),
    user_id     BIGINT      NOT NULL REFERENCES sys_user (id),
    rating      SMALLINT    NOT NULL,            -- 1 = 有帮助 / -1 = 无帮助
    comment     VARCHAR(512),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (message_id, user_id)
);
COMMENT ON TABLE cs_feedback IS '满意度反馈（FAQ/Agent 质量运营数据源）';
