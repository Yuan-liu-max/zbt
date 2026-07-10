-- ============================================================
-- V7__init_ai_report.sql
-- AI结果 / 提示词模板 / 门店评分 / 报表快照 / 通知 / 文件 / 表单 / 操作日志 — 共8张表
-- ============================================================

-- 1. 提示词模板表
DROP TABLE IF EXISTS `prompt_template`;
CREATE TABLE `prompt_template` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name`   VARCHAR(200) NOT NULL COMMENT '模板名称',
    `business_type`   VARCHAR(20)  NOT NULL COMMENT 'EMPLOYEE/PRODUCT/SCENE/TASK',
    `prompt_content`  TEXT         NOT NULL COMMENT '提示词内容(支持变量占位符)',
    `input_schema`    JSON         NULL     DEFAULT NULL COMMENT '输入变量Schema',
    `output_schema`   JSON         NULL     DEFAULT NULL COMMENT '输出格式Schema',
    `model_name`      VARCHAR(50)  NOT NULL DEFAULT 'gpt-4' COMMENT '模型名称',
    `status`          VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `created_by`      BIGINT       NULL     DEFAULT NULL COMMENT '创建人ID',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';

-- 2. AI结果表
DROP TABLE IF EXISTS `ai_result`;
CREATE TABLE `ai_result` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `business_type`      VARCHAR(20)  NOT NULL COMMENT 'EMPLOYEE/PRODUCT/SCENE/TASK',
    `related_id`         BIGINT       NOT NULL COMMENT '关联对象ID',
    `prompt_template_id` BIGINT       NULL     DEFAULT NULL COMMENT '提示词模板ID',
    `input_snapshot`     JSON         NULL     DEFAULT NULL COMMENT '输入快照',
    `output_text`        TEXT         NULL     DEFAULT NULL COMMENT '输出结果',
    `output_json`        JSON         NULL     DEFAULT NULL COMMENT '结构化结果',
    `score`              DECIMAL(5,2) NULL     DEFAULT NULL COMMENT 'AI分数',
    `model_name`         VARCHAR(50)  NULL     DEFAULT NULL COMMENT '模型名称',
    `token_usage`        JSON         NULL     DEFAULT NULL COMMENT 'Token消耗',
    `status`             VARCHAR(10)  NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_business_type_related` (`business_type`, `related_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI结果表';

-- 3. 门店月度评分表
DROP TABLE IF EXISTS `store_monthly_score`;
CREATE TABLE `store_monthly_score` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `store_id`         BIGINT       NOT NULL COMMENT '门店ID',
    `score_month`      VARCHAR(7)   NOT NULL COMMENT '评分月份',
    `total_score`      DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '总分(100)',
    `human_score`      DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '人效分(35%)',
    `product_score`    DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '货品分(30%)',
    `scene_score`      DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '场景分(25%)',
    `discipline_score` DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '纪律分(10%)',
    `overdue_count`    INT          NOT NULL DEFAULT 0  COMMENT '超时次数',
    `rejected_count`   INT          NOT NULL DEFAULT 0  COMMENT '被驳回次数',
    `ranking`          INT          NULL     DEFAULT NULL COMMENT '排名',
    `detail_json`      JSON         NULL     DEFAULT NULL COMMENT '详细评分JSON',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_store_month` (`store_id`, `score_month`),
    INDEX `idx_ranking` (`ranking`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店月度评分表';

-- 4. 报表快照表
DROP TABLE IF EXISTS `report_snapshot`;
CREATE TABLE `report_snapshot` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_type`   VARCHAR(50) NOT NULL COMMENT '报表类型',
    `report_period` VARCHAR(50) NOT NULL COMMENT '报表周期(2026-01/2026-Q1)',
    `store_id`      BIGINT      NULL     DEFAULT NULL COMMENT '门店ID(NULL=全系统)',
    `report_json`   JSON        NOT NULL COMMENT '报表数据JSON',
    `generated_at`  DATETIME    NOT NULL COMMENT '生成时间',
    PRIMARY KEY (`id`),
    INDEX `idx_report_type_period` (`report_type`, `report_period`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表快照表';

-- 5. 消息通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `receiver_id`       BIGINT       NOT NULL COMMENT '接收人ID',
    `title`             VARCHAR(200) NOT NULL COMMENT '标题',
    `content`           TEXT         NULL     DEFAULT NULL COMMENT '内容',
    `notification_type` VARCHAR(20)  NOT NULL COMMENT 'TASK_REMIND/OVERDUE/AUDIT/REJECT/ABNORMAL/AI_ADVICE/HQ_NOTICE',
    `business_type`     VARCHAR(50)  NULL     DEFAULT NULL COMMENT '业务类型',
    `business_id`       BIGINT       NULL     DEFAULT NULL COMMENT '业务ID',
    `is_read`           TINYINT      NOT NULL DEFAULT 0  COMMENT '是否已读 0=否 1=是',
    `read_at`           DATETIME     NULL     DEFAULT NULL COMMENT '阅读时间',
    `channel`           VARCHAR(20)  NOT NULL DEFAULT 'STATION' COMMENT 'STATION/WECOM/SMS/PUSH',
    `send_status`       VARCHAR(10)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_notification_type` (`notification_type`),
    INDEX `idx_send_status` (`send_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 6. 文件资源表
DROP TABLE IF EXISTS `file_resource`;
CREATE TABLE `file_resource` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name`     VARCHAR(500) NOT NULL COMMENT '原始文件名',
    `file_key`      VARCHAR(500) NOT NULL COMMENT '存储Key',
    `file_url`      VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_type`     VARCHAR(20)  NOT NULL COMMENT 'IMAGE/DOCUMENT/VIDEO/OTHER',
    `file_size`     BIGINT       NOT NULL COMMENT '文件大小(字节)',
    `mime_type`     VARCHAR(100) NULL     DEFAULT NULL COMMENT 'MIME类型',
    `storage_type`  VARCHAR(20)  NOT NULL DEFAULT 'MINIO' COMMENT 'MINIO/OSS/COS',
    `uploader_id`   BIGINT       NULL     DEFAULT NULL COMMENT '上传人ID',
    `business_type` VARCHAR(50)  NULL     DEFAULT NULL COMMENT '关联业务类型',
    `business_id`   BIGINT       NULL     DEFAULT NULL COMMENT '关联业务ID',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_file_key` (`file_key`),
    INDEX `idx_uploader_id` (`uploader_id`),
    INDEX `idx_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件资源表';

-- 7. 表单模板表
DROP TABLE IF EXISTS `form_schema`;
CREATE TABLE `form_schema` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `form_name`        VARCHAR(200) NOT NULL COMMENT '表单名称',
    `form_json_schema` JSON         NOT NULL COMMENT 'JSON Schema定义',
    `status`           VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `created_by`       BIGINT       NULL     DEFAULT NULL COMMENT '创建人ID',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单模板表';

-- 8. 操作日志表
DROP TABLE IF EXISTS `operate_log`;
CREATE TABLE `operate_log` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `operator_id`    BIGINT      NULL     DEFAULT NULL COMMENT '操作人ID',
    `module`         VARCHAR(50) NOT NULL COMMENT '操作模块',
    `action`         VARCHAR(50) NOT NULL COMMENT '操作动作 CREATE/UPDATE/DELETE/SUBMIT/AUDIT',
    `target_type`    VARCHAR(50) NULL     DEFAULT NULL COMMENT '操作对象类型',
    `target_id`      BIGINT      NULL     DEFAULT NULL COMMENT '操作对象ID',
    `request_ip`     VARCHAR(50) NULL     DEFAULT NULL COMMENT '请求IP',
    `request_params` JSON        NULL     DEFAULT NULL COMMENT '请求参数(脱敏)',
    `old_data`       JSON        NULL     DEFAULT NULL COMMENT '修改前数据',
    `new_data`       JSON        NULL     DEFAULT NULL COMMENT '修改后数据',
    `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_target_type_id` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
