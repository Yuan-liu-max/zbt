-- ============================================================
-- V5__init_scene.sql
-- 卫生巡检 / 陈列检查 / 物料更新 / 设备检查 / 客户体验复盘 — 共5张表
-- ============================================================

-- 1. 卫生巡检表
DROP TABLE IF EXISTS `scene_health_inspection`;
CREATE TABLE `scene_health_inspection` (
    `id`                     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `inspection_time`        VARCHAR(10) NOT NULL COMMENT 'MORNING/MIDDAY/EVENING',
    `inspection_date`        DATE        NOT NULL COMMENT '巡检日期',
    `inspector_id`           BIGINT      NULL     DEFAULT NULL COMMENT '巡检人ID',
    `store_id`               BIGINT      NULL     DEFAULT NULL COMMENT '门店ID',
    `area_results`           JSON        NULL     DEFAULT NULL COMMENT '各区域检查结果JSON',
    `issue_description`      TEXT        NULL     DEFAULT NULL COMMENT '问题描述',
    `photo_urls`             JSON        NULL     DEFAULT NULL COMMENT '照片',
    `rectification_required` TINYINT     NOT NULL DEFAULT 0  COMMENT '是否需要整改 0=否 1=是',
    `created_at`             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_inspection_date` (`inspection_date`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_inspector_id` (`inspector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卫生巡检表';

-- 2. 陈列检查表
DROP TABLE IF EXISTS `scene_display_inspection`;
CREATE TABLE `scene_display_inspection` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `inspection_date`     DATE         NOT NULL COMMENT '检查日期',
    `store_id`            BIGINT       NULL     DEFAULT NULL COMMENT '门店ID',
    `inspector_id`        BIGINT       NULL     DEFAULT NULL COMMENT '检查人ID',
    `display_area`        VARCHAR(50)  NOT NULL COMMENT '区域(黄金区/钻石区/K金区/古法区/银饰区/C位)',
    `standard_score`      DECIMAL(5,2) NULL     DEFAULT NULL COMMENT '标准分',
    `issue_description`   TEXT         NULL     DEFAULT NULL COMMENT '问题描述',
    `before_photos`       JSON         NULL     DEFAULT NULL COMMENT '整改前照片',
    `after_photos`        JSON         NULL     DEFAULT NULL COMMENT '整改后照片',
    `rectification_plan`  TEXT         NULL     DEFAULT NULL COMMENT '整改方案',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_inspection_date` (`inspection_date`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_display_area` (`display_area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陈列检查表';

-- 3. 物料更新表
DROP TABLE IF EXISTS `scene_material_update`;
CREATE TABLE `scene_material_update` (
    `id`                     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `store_id`               BIGINT      NULL     DEFAULT NULL COMMENT '门店ID',
    `checker_id`             BIGINT      NULL     DEFAULT NULL COMMENT '检查人ID',
    `check_date`             DATE        NOT NULL COMMENT '检查日期',
    `material_type`          VARCHAR(20) NOT NULL COMMENT 'POSTER/FLAG/STAND/CARD',
    `current_status`         VARCHAR(10) NOT NULL COMMENT 'NORMAL/EXPIRED/DAMAGED/MISSING',
    `updated_photos`         JSON        NULL     DEFAULT NULL COMMENT '更新后照片',
    `issue_description`      TEXT        NULL     DEFAULT NULL COMMENT '问题说明',
    `replacement_required`   TINYINT     NOT NULL DEFAULT 0  COMMENT '是否需要更换 0=否 1=是',
    `created_at`             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_check_date` (`check_date`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料更新表';

-- 4. 设备检查表
DROP TABLE IF EXISTS `scene_equipment_check`;
CREATE TABLE `scene_equipment_check` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `store_id`          BIGINT      NULL     DEFAULT NULL COMMENT '门店ID',
    `checker_id`        BIGINT      NULL     DEFAULT NULL COMMENT '检查人ID',
    `check_date`        DATE        NOT NULL COMMENT '检查日期',
    `equipment_type`    VARCHAR(30) NOT NULL COMMENT 'LIGHT/AC/CAMERA/AUDIO/POS/CABINET_LIGHT/SAFE/NETWORK',
    `status`            VARCHAR(10) NOT NULL COMMENT 'NORMAL/ABNORMAL',
    `issue_description` TEXT        NULL     DEFAULT NULL COMMENT '问题描述',
    `repair_required`   TINYINT     NOT NULL DEFAULT 0  COMMENT '是否报修 0=否 1=是',
    `photo_urls`        JSON        NULL     DEFAULT NULL COMMENT '照片',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_check_date` (`check_date`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备检查表';

-- 5. 客户体验复盘表
DROP TABLE IF EXISTS `scene_customer_experience_review`;
CREATE TABLE `scene_customer_experience_review` (
    `id`                    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `store_id`              BIGINT      NULL     DEFAULT NULL COMMENT '门店ID',
    `reviewer_id`           BIGINT      NULL     DEFAULT NULL COMMENT '复盘人ID',
    `review_week`           VARCHAR(20) NOT NULL COMMENT '周标识',
    `feedback_count`        INT         NOT NULL DEFAULT 0  COMMENT '反馈数',
    `complaint_count`       INT         NOT NULL DEFAULT 0  COMMENT '投诉数',
    `common_feedback`       TEXT        NULL     DEFAULT NULL COMMENT '常见反馈',
    `improvement_plan`      TEXT        NULL     DEFAULT NULL COMMENT '改进计划',
    `responsible_person_id` BIGINT      NULL     DEFAULT NULL COMMENT '负责人ID',
    `created_at`            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_review_week` (`review_week`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户体验复盘表';
