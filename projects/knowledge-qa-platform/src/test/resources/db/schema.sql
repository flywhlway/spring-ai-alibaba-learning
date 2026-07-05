-- =====================================================================
-- 项目一 · AI 企业知识库问答平台 —— PostgreSQL 业务库 DDL（SSOT）
--
-- 数据库：kqa_platform（由 docker-compose.override.yml 的 kqa-db-init 自动创建并执行本文件）
-- 约定：JPA ddl-auto=none，本文件是表结构唯一真源；实体类（后续迭代）必须与此对齐。
-- 向量数据不在此库：Chunk 向量存 Milvus（collection: kqa_knowledge），此处仅存元数据。
--
-- @author flywhl
-- =====================================================================

-- ---------------------------------------------------------------
-- 用户与权限：演示用最小 RBAC（ADMIN 管理知识与后台 / EMPLOYEE 提问）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,                 -- DelegatingPasswordEncoder 格式（演示数据用 {noop}）
    display_name  VARCHAR(64)  NOT NULL,
    role          VARCHAR(32)  NOT NULL DEFAULT 'EMPLOYEE',  -- ADMIN / EMPLOYEE
    department    VARCHAR(64),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
COMMENT ON TABLE sys_user IS '平台用户（管理员/员工）';

-- ---------------------------------------------------------------
-- 知识文档：原始文件存 MinIO（bucket: kqa-documents），此表记录元数据与解析状态
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS kb_document (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(256) NOT NULL,
    category       VARCHAR(64)  NOT NULL DEFAULT 'GENERAL',   -- 制度 / 产品手册 / 技术文档等
    file_name      VARCHAR(256) NOT NULL,
    content_type   VARCHAR(128) NOT NULL,
    file_size      BIGINT       NOT NULL DEFAULT 0,
    minio_object   VARCHAR(512) NOT NULL,                     -- MinIO 对象键
    status         VARCHAR(32)  NOT NULL DEFAULT 'UPLOADED',  -- UPLOADED / PARSING / INDEXED / FAILED
    chunk_count    INT          NOT NULL DEFAULT 0,
    fail_reason    VARCHAR(512),
    uploaded_by    BIGINT       NOT NULL REFERENCES sys_user (id),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_kb_document_status   ON kb_document (status);
CREATE INDEX IF NOT EXISTS idx_kb_document_category ON kb_document (category);
COMMENT ON TABLE kb_document IS '知识文档元数据（原始文件在 MinIO，向量在 Milvus）';

-- ---------------------------------------------------------------
-- Chunk 元数据：与 Milvus 中的向量一一对应（milvus_pk 关联），供引用溯源回查
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS kb_chunk (
    id           BIGSERIAL PRIMARY KEY,
    document_id  BIGINT      NOT NULL REFERENCES kb_document (id) ON DELETE CASCADE,
    milvus_pk    VARCHAR(64) NOT NULL UNIQUE,     -- Milvus 主键（Document.id）
    seq_no       INT         NOT NULL,            -- 在原文档中的顺序
    text_preview VARCHAR(512) NOT NULL,           -- 前 512 字预览（完整文本存 Milvus payload）
    token_count  INT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_kb_chunk_document ON kb_chunk (document_id);
COMMENT ON TABLE kb_chunk IS 'Chunk 元数据（引用溯源：答案 citation → chunk → 原文档）';

-- ---------------------------------------------------------------
-- Prompt 模板：数据库版本化（发布后经 Nacos 推送热更新，见 README「Prompt 管理」）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prompt_template (
    id          BIGSERIAL PRIMARY KEY,
    template_key VARCHAR(128) NOT NULL,            -- 如 qa-system / qa-citation / query-rewrite
    version     INT          NOT NULL DEFAULT 1,
    content     TEXT         NOT NULL,
    description VARCHAR(256),
    status      VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',  -- DRAFT / PUBLISHED / ARCHIVED
    published_at TIMESTAMPTZ,
    created_by  BIGINT       NOT NULL REFERENCES sys_user (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (template_key, version)
);
CREATE INDEX IF NOT EXISTS idx_prompt_key_status ON prompt_template (template_key, status);
COMMENT ON TABLE prompt_template IS 'Prompt 模板版本化管理（PUBLISHED 版本推送 Nacos 热更新）';

-- ---------------------------------------------------------------
-- 会话与消息：完整消息滚动窗口在 Redis；此处落库归档供审计/统计
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS qa_conversation (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL UNIQUE,   -- 业务会话 ID（Redis Memory conversationId）
    user_id         BIGINT      NOT NULL REFERENCES sys_user (id),
    title           VARCHAR(256),
    message_count   INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_qa_conversation_user ON qa_conversation (user_id);

CREATE TABLE IF NOT EXISTS qa_message (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64)  NOT NULL,
    role            VARCHAR(16)  NOT NULL,          -- USER / ASSISTANT
    content         TEXT         NOT NULL,
    citations       JSONB,                          -- 引用溯源快照 [{documentId,chunkId,score,preview}]
    model           VARCHAR(64),                    -- 实际应答模型（路由结果）
    input_tokens    INT          NOT NULL DEFAULT 0,
    output_tokens   INT          NOT NULL DEFAULT 0,
    latency_ms      BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_qa_message_conversation ON qa_message (conversation_id, created_at);
COMMENT ON TABLE qa_message IS '问答消息归档（含 citation 快照与 token 用量）';

-- ---------------------------------------------------------------
-- 审计日志：后台操作与 AI 调用双轨审计（starter AuditLoggingAdvisor 落库端）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(64),
    action      VARCHAR(64)  NOT NULL,          -- LOGIN / UPLOAD_DOC / DELETE_DOC / PUBLISH_PROMPT / AI_CALL ...
    target      VARCHAR(256),
    detail      JSONB,
    client_ip   VARCHAR(64),
    success     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_log_action_time ON audit_log (action, created_at);
COMMENT ON TABLE audit_log IS '审计日志（后台操作 + AI 调用）';

-- ---------------------------------------------------------------
-- 答案反馈：employee 对答案点赞/点踩，供 RAG 质量运营
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS qa_feedback (
    id          BIGSERIAL PRIMARY KEY,
    message_id  BIGINT      NOT NULL REFERENCES qa_message (id),
    user_id     BIGINT      NOT NULL REFERENCES sys_user (id),
    rating      SMALLINT    NOT NULL,            -- 1 = 有帮助 / -1 = 无帮助
    comment     VARCHAR(512),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (message_id, user_id)
);
COMMENT ON TABLE qa_feedback IS '答案反馈（RAG 质量运营数据源）';
