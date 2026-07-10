-- ============================================================
-- V2__init_task.sql
-- 动作库 / 任务模板 / 任务实例 / 提交 / 审核 / 提醒 — 共6张表
-- ============================================================

-- 1. 动作库表
DROP TABLE IF EXISTS `action_template`;
CREATE TABLE `action_template` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '动作ID',
    `action_name`            VARCHAR(200) NOT NULL COMMENT '动作名称',
    `dimension`              VARCHAR(20)  NOT NULL COMMENT 'HUMAN/PRODUCT/SCENE/COMPREHENSIVE',
    `category`               VARCHAR(50)  NOT NULL COMMENT '分类(晨会/面谈/盘点等)',
    `description`            TEXT         NULL     DEFAULT NULL COMMENT '动作说明',
    `execution_standard`     TEXT         NULL     DEFAULT NULL COMMENT '执行标准',
    `frequency_type`         VARCHAR(20)  NOT NULL COMMENT 'DAILY/WEEKLY/MONTHLY/QUARTERLY/MANUAL/ABNORMAL',
    `cron_expression`        VARCHAR(100) NULL     DEFAULT NULL COMMENT '周期表达式',
    `due_time_rule`          VARCHAR(100) NULL     DEFAULT NULL COMMENT '截止时间规则(如当日10:00)',
    `required_photos`        INT          NOT NULL DEFAULT 1  COMMENT '最少上传照片数',
    `required_text`          TINYINT      NOT NULL DEFAULT 1  COMMENT '是否必填文字 0=否 1=是',
    `required_form`          TINYINT      NOT NULL DEFAULT 0  COMMENT '是否绑定表单 0=否 1=是',
    `form_schema_id`         BIGINT       NULL     DEFAULT NULL COMMENT '表单模板ID',
    `require_audit`          TINYINT      NOT NULL DEFAULT 1  COMMENT '是否需要审核 0=否 1=是',
    `default_auditor_role`   VARCHAR(50)  NULL     DEFAULT NULL COMMENT '默认审核角色',
    `score_weight`           DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '分值权重',
    `is_default`             TINYINT      NOT NULL DEFAULT 1  COMMENT '是否默认动作库 0=否 1=是',
    `is_force`               TINYINT      NOT NULL DEFAULT 0  COMMENT '是否强制任务 0=否 1=是',
    `applicable_store_types` VARCHAR(200) NULL     DEFAULT NULL COMMENT '适用门店类型(逗号分隔)',
    `status`                 VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `created_by`             BIGINT       NULL     DEFAULT NULL COMMENT '创建人ID',
    `is_deleted`             TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`             DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_dimension` (`dimension`),
    INDEX `idx_frequency_type` (`frequency_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动作库表';

-- 2. 任务模板表
DROP TABLE IF EXISTS `task_template`;
CREATE TABLE `task_template` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_name`         VARCHAR(200) NOT NULL COMMENT '模板名称',
    `action_id`             BIGINT       NULL     DEFAULT NULL COMMENT '来源动作ID',
    `dimension`             VARCHAR(20)  NOT NULL COMMENT '人/货/场/综合',
    `category`              VARCHAR(50)  NOT NULL COMMENT '任务分类',
    `description`           TEXT         NULL     DEFAULT NULL COMMENT '任务说明',
    `execution_standard`    TEXT         NULL     DEFAULT NULL COMMENT '执行标准',
    `required_photos`       INT          NOT NULL DEFAULT 1  COMMENT '最少照片数',
    `required_text`         TINYINT      NOT NULL DEFAULT 1  COMMENT '是否必填文字',
    `required_form`         TINYINT      NOT NULL DEFAULT 0  COMMENT '是否绑定表单',
    `form_schema_id`        BIGINT       NULL     DEFAULT NULL COMMENT '表单模板ID',
    `require_audit`         TINYINT      NOT NULL DEFAULT 1  COMMENT '是否需要审核',
    `default_auditor_role`  VARCHAR(50)  NULL     DEFAULT NULL COMMENT '默认审核角色',
    `frequency_type`        VARCHAR(20)  NOT NULL COMMENT '周期类型 DAILY/WEEKLY/MONTHLY/QUARTERLY/MANUAL',
    `cron_expression`       VARCHAR(100) NULL     DEFAULT NULL COMMENT 'Cron表达式',
    `due_time_rule`         VARCHAR(100) NULL     DEFAULT NULL COMMENT '截止时间规则',
    `reminder_rule`         JSON         NULL     DEFAULT NULL COMMENT '提醒规则JSON',
    `score_weight`          DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '分值权重',
    `is_default`            TINYINT      NOT NULL DEFAULT 0  COMMENT '是否默认动作库',
    `is_force`              TINYINT      NOT NULL DEFAULT 0  COMMENT '是否强制任务',
    `applicable_store_ids`  TEXT         NULL     DEFAULT NULL COMMENT '适用门店ID(逗号分隔)',
    `applicable_region_ids` TEXT         NULL     DEFAULT NULL COMMENT '适用区域ID(逗号分隔)',
    `status`                VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `created_by`            BIGINT       NULL     DEFAULT NULL COMMENT '创建人ID',
    `is_deleted`            TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除',
    `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_action_id` (`action_id`),
    INDEX `idx_frequency_type` (`frequency_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务模板表';

-- 3. 任务实例表 (核心)
DROP TABLE IF EXISTS `task_instance`;
CREATE TABLE `task_instance` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_no`             VARCHAR(50)  NOT NULL COMMENT '任务编号 TK+日期+序号',
    `template_id`         BIGINT       NULL     DEFAULT NULL COMMENT '来源模板ID',
    `task_title`          VARCHAR(200) NOT NULL COMMENT '任务标题',
    `dimension`           VARCHAR(20)  NOT NULL COMMENT 'HUMAN/PRODUCT/SCENE/COMPREHENSIVE',
    `category`            VARCHAR(50)  NOT NULL COMMENT '分类',
    `store_id`            BIGINT       NOT NULL COMMENT '所属门店ID',
    `assignee_id`         BIGINT       NOT NULL COMMENT '执行人ID',
    `auditor_id`          BIGINT       NULL     DEFAULT NULL COMMENT '审核人ID',
    `start_time`          DATETIME     NOT NULL COMMENT '开始时间',
    `due_time`            DATETIME     NOT NULL COMMENT '截止时间',
    `completed_time`      DATETIME     NULL     DEFAULT NULL COMMENT '完成时间',
    `status`              VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/READY/IN_PROGRESS/SUBMITTED/AUDITING/APPROVED/COMPLETED/REJECTED/RECTIFYING/OVERDUE/CANCELLED/VOIDED/EXEMPTED',
    `priority`            VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/URGENT',
    `source_type`         VARCHAR(20)  NOT NULL COMMENT 'CYCLE/MANUAL/HQ/ABNORMAL/HOLIDAY/AI',
    `related_object_type` VARCHAR(50)  NULL     DEFAULT NULL COMMENT '关联对象类型 EMPLOYEE/PRODUCT/STORE',
    `related_object_id`   BIGINT       NULL     DEFAULT NULL COMMENT '关联对象ID',
    `is_overdue`          TINYINT      NOT NULL DEFAULT 0  COMMENT '是否超时 0=否 1=是',
    `overdue_minutes`     INT          NOT NULL DEFAULT 0  COMMENT '超时分钟数',
    `quality_score`       DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '执行质量分',
    `ai_score`            DECIMAL(5,2) NULL     DEFAULT NULL COMMENT 'AI评分',
    `manual_score`        DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '人工评分',
    `final_score`         DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '最终得分',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_task_no` (`task_no`),
    INDEX `idx_assignee_id` (`assignee_id`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_due_time` (`due_time`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_source_type` (`source_type`),
    INDEX `idx_is_overdue` (`is_overdue`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务实例表';

-- 4. 任务提交表
DROP TABLE IF EXISTS `task_submission`;
CREATE TABLE `task_submission` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '提交ID',
    `task_id`         BIGINT   NOT NULL COMMENT '任务ID',
    `submitter_id`    BIGINT   NOT NULL COMMENT '提交人ID',
    `text_content`    TEXT     NULL     DEFAULT NULL COMMENT '文字说明',
    `form_data`       JSON     NULL     DEFAULT NULL COMMENT '表单数据JSON',
    `photo_urls`      JSON     NULL     DEFAULT NULL COMMENT '图片URL列表',
    `attachment_urls` JSON     NULL     DEFAULT NULL COMMENT '附件URL列表',
    `location`        JSON     NULL     DEFAULT NULL COMMENT 'GPS定位 {lat, lng}',
    `submitted_at`    DATETIME NOT NULL COMMENT '提交时间',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_submitter_id` (`submitter_id`),
    INDEX `idx_submitted_at` (`submitted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务提交表';

-- 5. 任务审核表
DROP TABLE IF EXISTS `task_audit`;
CREATE TABLE `task_audit` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审核ID',
    `task_id`       BIGINT       NOT NULL COMMENT '任务ID',
    `submission_id` BIGINT       NULL     DEFAULT NULL COMMENT '提交ID',
    `auditor_id`    BIGINT       NOT NULL COMMENT '审核人ID',
    `audit_result`  VARCHAR(20)  NOT NULL COMMENT 'APPROVED/REJECTED/RECTIFY',
    `audit_comment` TEXT         NULL     DEFAULT NULL COMMENT '审核意见',
    `score`         DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '人工评分(0-100)',
    `audited_at`    DATETIME     NOT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务审核表';

-- 6. 任务提醒日志表
DROP TABLE IF EXISTS `task_reminder_log`;
CREATE TABLE `task_reminder_log` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id`       BIGINT      NULL     DEFAULT NULL COMMENT '任务ID',
    `reminder_type` VARCHAR(20) NOT NULL COMMENT 'START/DEADLINE_2H/DEADLINE_30M/OVERDUE/OVERDUE_24H',
    `channel`       VARCHAR(20) NOT NULL COMMENT 'STATION/WECOM/SMS/PUSH',
    `receiver_id`   BIGINT      NULL     DEFAULT NULL COMMENT '接收人ID',
    `send_status`   VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    `sent_at`       DATETIME    NULL     DEFAULT NULL COMMENT '发送时间',
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务提醒日志表';
