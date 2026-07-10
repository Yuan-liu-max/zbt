-- ============================================================
-- V3__init_human.sql
-- 员工档案 / 面谈 / 考核 / 培训 / 培训参与 / 月度复盘 / 分层记录 — 共7张表
-- ============================================================

-- 1. 员工档案表
DROP TABLE IF EXISTS `employee_profile`;
CREATE TABLE `employee_profile` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT      NULL     DEFAULT NULL COMMENT '关联用户ID',
    `store_id`   BIGINT      NULL     DEFAULT NULL COMMENT '所属门店ID',
    `position`   VARCHAR(50) NULL     DEFAULT NULL COMMENT '岗位',
    `entry_date` DATE        NULL     DEFAULT NULL COMMENT '入职日期',
    `level`      VARCHAR(20) NOT NULL DEFAULT 'STANDARD' COMMENT 'BENCHMARK/STANDARD/IMPROVING',
    `status`     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RESIGNED/DISABLED',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工档案表';

-- 2. 员工面谈记录表
DROP TABLE IF EXISTS `employee_interview`;
CREATE TABLE `employee_interview` (
    `id`                      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id`             BIGINT        NOT NULL COMMENT '员工ID',
    `interviewer_id`          BIGINT        NULL     DEFAULT NULL COMMENT '面谈人(店长)ID',
    `interview_date`          DATE          NOT NULL COMMENT '面谈日期',
    `current_week_sales`      DECIMAL(12,2) NULL     DEFAULT NULL COMMENT '本周业绩',
    `target_completion_rate`  DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '目标完成率(%)',
    `main_problem`            TEXT          NULL     DEFAULT NULL COMMENT '主要问题',
    `customer_follow_issue`   TEXT          NULL     DEFAULT NULL COMMENT '客户跟进难点',
    `product_knowledge_gap`   TEXT          NULL     DEFAULT NULL COMMENT '产品知识短板',
    `mindset_status`          VARCHAR(20)   NULL     DEFAULT NULL COMMENT 'POSITIVE/NORMAL/LOW/ABNORMAL',
    `next_week_goal`          DECIMAL(12,2) NULL     DEFAULT NULL COMMENT '下周目标',
    `improvement_plan`        TEXT          NULL     DEFAULT NULL COMMENT '改进计划',
    `manager_comment`         TEXT          NULL     DEFAULT NULL COMMENT '店长评语',
    `employee_feedback`       TEXT          NULL     DEFAULT NULL COMMENT '员工反馈',
    `follow_up_date`          DATE          NULL     DEFAULT NULL COMMENT '跟进日期',
    `created_at`              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`              DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_interview_date` (`interview_date`),
    INDEX `idx_interviewer_id` (`interviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工面谈记录表';

-- 3. 员工能力考核表
DROP TABLE IF EXISTS `employee_assessment`;
CREATE TABLE `employee_assessment` (
    `id`                       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id`              BIGINT       NOT NULL COMMENT '员工ID',
    `assessor_id`              BIGINT       NULL     DEFAULT NULL COMMENT '考核人ID',
    `assessment_week`          VARCHAR(20)  NOT NULL COMMENT '考核周(如2026-W01)',
    `product_knowledge_score`  DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '产品知识(满分25)',
    `matching_skill_score`     DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '搭配技巧(满分20)',
    `reception_score`          DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '接待流程(满分20)',
    `objection_handling_score` DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '异议处理(满分20)',
    `promotion_script_score`   DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '活动话术(满分15)',
    `total_score`              DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '总分',
    `improvement_advice`       TEXT         NULL     DEFAULT NULL COMMENT '改进建议',
    `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_assessment_week` (`assessment_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工能力考核表';

-- 4. 培训记录表
DROP TABLE IF EXISTS `employee_training`;
CREATE TABLE `employee_training` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `training_title`   VARCHAR(200) NOT NULL COMMENT '培训标题',
    `training_type`    VARCHAR(50)  NOT NULL COMMENT '新品知识/婚庆销售/古法黄金/钻石镶嵌/高毛利等',
    `trainer_id`       BIGINT       NULL     DEFAULT NULL COMMENT '培训人ID',
    `training_date`    DATE         NOT NULL COMMENT '培训日期',
    `exam_score`       DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '考核分',
    `training_summary` TEXT         NULL     DEFAULT NULL COMMENT '培训总结',
    `material_urls`    JSON         NULL     DEFAULT NULL COMMENT '培训资料URL',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_trainer_id` (`trainer_id`),
    INDEX `idx_training_date` (`training_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='培训记录表';

-- 5. 培训参与记录表
DROP TABLE IF EXISTS `employee_training_record`;
CREATE TABLE `employee_training_record` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `training_id`    BIGINT      NULL     DEFAULT NULL COMMENT '培训ID',
    `employee_id`    BIGINT      NULL     DEFAULT NULL COMMENT '参与员工ID',
    `sign_in_status` VARCHAR(10) NOT NULL DEFAULT 'SIGNED' COMMENT 'SIGNED/ABSENT/LATE',
    `exam_score`     DECIMAL(5,2) NULL    DEFAULT NULL COMMENT '个人考核分',
    `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_training_employee` (`training_id`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='培训参与记录表';

-- 6. 月度绩效复盘表
DROP TABLE IF EXISTS `employee_monthly_review`;
CREATE TABLE `employee_monthly_review` (
    `id`                          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id`                 BIGINT        NOT NULL COMMENT '员工ID',
    `reviewer_id`                 BIGINT        NULL     DEFAULT NULL COMMENT '复盘人ID',
    `review_month`                VARCHAR(7)    NOT NULL COMMENT '月份(2026-01)',
    `total_sales_amount`          DECIMAL(12,2) NULL     DEFAULT NULL COMMENT '总销售额',
    `sales_order_count`           INT           NULL     DEFAULT NULL COMMENT '成交单数',
    `avg_order_amount`            DECIMAL(10,2) NULL     DEFAULT NULL COMMENT '客单价',
    `new_customer_sales`          DECIMAL(12,2) NULL     DEFAULT NULL COMMENT '新客销售',
    `old_customer_repurchase_sales` DECIMAL(12,2) NULL   DEFAULT NULL COMMENT '老客复购',
    `key_category_sales`          JSON          NULL     DEFAULT NULL COMMENT '品类销售JSON',
    `service_score`               DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '服务评分',
    `task_execution_score`        DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '任务执行分',
    `reward_amount`               DECIMAL(10,2) NULL     DEFAULT NULL COMMENT '奖励金额',
    `penalty_amount`              DECIMAL(10,2) NULL     DEFAULT NULL COMMENT '处罚金额',
    `manager_review`              TEXT          NULL     DEFAULT NULL COMMENT '店长评语',
    `created_at`                  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                  DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_review_month` (`review_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月度绩效复盘表';

-- 7. 员工分层记录表
DROP TABLE IF EXISTS `employee_level_record`;
CREATE TABLE `employee_level_record` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id`       BIGINT       NOT NULL COMMENT '员工ID',
    `eval_month`        VARCHAR(7)   NOT NULL COMMENT '评估月份',
    `performance_score` DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '业绩分',
    `service_score`     DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '服务分',
    `execution_score`   DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '执行分',
    `final_level`       VARCHAR(20)  NOT NULL COMMENT 'BENCHMARK/STANDARD/IMPROVING',
    `reason`            TEXT         NULL     DEFAULT NULL COMMENT '定级原因',
    `next_month_plan`   TEXT         NULL     DEFAULT NULL COMMENT '下月计划',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_eval_month` (`eval_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工分层记录表';
