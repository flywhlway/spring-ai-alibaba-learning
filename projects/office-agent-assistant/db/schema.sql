-- =====================================================================
-- 项目二 · 企业 AI Agent 办公助手 —— MySQL 业务库 DDL（SSOT）
--
-- 数据库：office_agent（由 docker-compose.override.yml 的 office-db-init 自动创建并执行本文件）
-- 约定：JPA ddl-auto=none，本文件是表结构唯一真源；实体类（后续迭代）必须与此对齐。
-- 向量数据不在此库：轻量知识检索存 pgvector（office_vector 库，表 office_knowledge，应用自动初始化）。
--
-- @author flywhl
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------
-- 用户：员工 / 管理员（后台用户管理域）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL COMMENT 'DelegatingPasswordEncoder 格式（演示数据用 {noop}）',
    display_name  VARCHAR(64)  NOT NULL,
    role          VARCHAR(32)  NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'ADMIN / EMPLOYEE',
    department    VARCHAR(64)  NULL,
    email         VARCHAR(128) NULL COMMENT '邮件起草 Tool 的默认发件人',
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '平台用户';

-- ---------------------------------------------------------------
-- 用户长期偏好：JDBC 长期记忆的结构化补充（写作风格/常用报表/时区等）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_preference (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    pref_key    VARCHAR(64) NOT NULL COMMENT '如 email-tone / report-format / working-hours',
    pref_value  TEXT        NOT NULL,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_pref (user_id, pref_key),
    CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户长期偏好（跨会话记忆的结构化部分）';

-- ---------------------------------------------------------------
-- Prompt 模板族：会议纪要 / 日报 / 邮件 / 审批意见（后台完整 CRUD）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prompt_template (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    template_key VARCHAR(128) NOT NULL COMMENT '如 meeting-summary / daily-report / email-draft / approval-opinion',
    version      INT          NOT NULL DEFAULT 1,
    content      TEXT         NOT NULL,
    description  VARCHAR(256) NULL,
    status       VARCHAR(32)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PUBLISHED / ARCHIVED',
    published_at DATETIME     NULL,
    created_by   BIGINT       NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_prompt_key_version (template_key, version),
    KEY idx_prompt_key_status (template_key, status),
    CONSTRAINT fk_prompt_user FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Prompt 模板族版本化管理';

-- ---------------------------------------------------------------
-- 审批单：审批助手（SequentialAgent/RoutingAgent 编排）的业务载体
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_request (
    id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_no   VARCHAR(64)   NOT NULL UNIQUE COMMENT '审批单号，如 AP20260705-0001',
    type         VARCHAR(32)   NOT NULL COMMENT 'EXPENSE 报销 / LEAVE 请假 / PURCHASE 采购',
    title        VARCHAR(256)  NOT NULL,
    amount       DECIMAL(12,2) NULL COMMENT '金额（报销/采购），驱动 RoutingAgent 升级链路',
    content      TEXT          NOT NULL COMMENT '申请正文（Agent 结构化抽取的原始输入）',
    ai_summary   TEXT          NULL COMMENT 'Agent 生成的要点摘要',
    ai_opinion   TEXT          NULL COMMENT 'Agent 生成的初审意见（合规性/预算核对）',
    status       VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / AI_REVIEWED / APPROVED / REJECTED',
    applicant_id BIGINT        NOT NULL,
    approver_id  BIGINT        NULL,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_approval_status (status),
    CONSTRAINT fk_approval_applicant FOREIGN KEY (applicant_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批单（审批助手 Agent 编排载体）';

-- ---------------------------------------------------------------
-- 日程：Calendar Tool 的数据底座（演示用本地日程，接真实 OA 时替换为 HTTP Tool）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS calendar_event (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(256) NOT NULL,
    location   VARCHAR(256) NULL,
    start_time DATETIME     NOT NULL,
    end_time   DATETIME     NOT NULL,
    remark     VARCHAR(512) NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_calendar_user_time (user_id, start_time),
    CONSTRAINT fk_calendar_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '日程（Calendar Tool 数据底座）';

-- ---------------------------------------------------------------
-- 报表底数：SQL Tool 白名单查询目标（演示数据；生产接数仓只读账号）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS report_sales (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_date    DATE          NOT NULL,
    region      VARCHAR(64)   NOT NULL,
    product     VARCHAR(128)  NOT NULL,
    sales_amount DECIMAL(14,2) NOT NULL,
    order_count INT           NOT NULL,
    KEY idx_sales_date (biz_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '销售日报底数（SQL Tool 白名单表）';

CREATE TABLE IF NOT EXISTS report_attendance (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_date    DATE        NOT NULL,
    department  VARCHAR(64) NOT NULL,
    headcount   INT         NOT NULL,
    present     INT         NOT NULL,
    on_leave    INT         NOT NULL,
    KEY idx_attendance_date (biz_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '考勤日报底数（SQL Tool 白名单表）';

-- ---------------------------------------------------------------
-- 产出物：会议纪要/日报/邮件草稿/Excel 的生成记录（可追溯、可重下载）
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS assistant_artifact (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(32)  NOT NULL COMMENT 'MEETING_SUMMARY / DAILY_REPORT / EMAIL_DRAFT / EXCEL',
    title       VARCHAR(256) NOT NULL,
    content     MEDIUMTEXT   NULL COMMENT '文本类产出物正文（JSON 结构化输出）',
    file_path   VARCHAR(512) NULL COMMENT 'Excel 类产出物的工作区路径',
    model       VARCHAR(64)  NULL,
    input_tokens  INT        NOT NULL DEFAULT 0,
    output_tokens INT        NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_artifact_user_type (user_id, type),
    CONSTRAINT fk_artifact_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 产出物记录（纪要/日报/邮件/Excel）';

-- ---------------------------------------------------------------
-- Spring AI JDBC ChatMemory（长期记忆）官方 MySQL 表结构
-- 与 spring-ai-starter-model-chat-memory-repository-jdbc 自带 schema 对齐；
-- application.yml 已设 initialize-schema=never，以本文件为准。
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(36)                              NOT NULL,
    content         TEXT                                     NOT NULL,
    type            ENUM ('USER','ASSISTANT','SYSTEM','TOOL') NOT NULL,
    `timestamp`     TIMESTAMP                                NOT NULL,
    KEY SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX (conversation_id, `timestamp`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Spring AI JDBC 长期会话记忆';
