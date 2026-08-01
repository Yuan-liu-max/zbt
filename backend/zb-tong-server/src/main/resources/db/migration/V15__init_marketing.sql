-- V15__init_marketing.sql — 营销管理
DROP TABLE IF EXISTS `promotion`;
DROP TABLE IF EXISTS `marketing_activity`;

CREATE TABLE `marketing_activity` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(100) NULL,
    `type`             VARCHAR(30)  NULL COMMENT 'promotion|new_customer|theme|membership',
    `start_time`       VARCHAR(20)  NULL,
    `end_time`         VARCHAR(20)  NULL,
    `status`           VARCHAR(20)  NULL COMMENT 'ongoing|ended|not_started',
    `scope`            VARCHAR(100) NULL,
    `registered_count` INT          DEFAULT 0,
    `total_count`      INT          DEFAULT 0,
    `is_deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_activity_status` (`status`),
    INDEX `idx_activity_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `promotion` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(100) NULL,
    `type`            VARCHAR(30)  NULL COMMENT 'discount|full_reduction|gift|member_price',
    `discount_method` VARCHAR(200) NULL,
    `start_time`      VARCHAR(20)  NULL,
    `end_time`        VARCHAR(20)  NULL,
    `status`          VARCHAR(20)  NULL COMMENT 'ongoing|ended|not_started',
    `scope`           VARCHAR(100) NULL,
    `usage_count`     INT          DEFAULT 0,
    `is_deleted`      TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_promotion_status` (`status`),
    INDEX `idx_promotion_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
