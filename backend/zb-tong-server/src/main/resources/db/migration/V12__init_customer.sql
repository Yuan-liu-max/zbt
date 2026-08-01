-- V12__init_customer.sql — 客户管理模块
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `member_level`;

CREATE TABLE `customer` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
    `code`                VARCHAR(32)   NULL,
    `name`                VARCHAR(50)   NULL,
    `phone`               VARCHAR(20)   NULL,
    `level`               VARCHAR(20)   NULL DEFAULT 'normal' COMMENT 'normal|vip|diamond',
    `total_consumption`   DECIMAL(12,2) NULL DEFAULT 0,
    `points`              INT           NULL DEFAULT 0,
    `registered_at`       VARCHAR(20)   NULL,
    `last_consumption_at` VARCHAR(20)   NULL,
    `status`              VARCHAR(20)   NULL DEFAULT 'normal' COMMENT 'normal|disabled',
    `is_deleted`          TINYINT(1)    NOT NULL DEFAULT 0,
    `created_at`          DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_customer_code` (`code`),
    INDEX `idx_customer_level` (`level`),
    INDEX `idx_customer_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `member_level` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `name`              VARCHAR(30)   NULL,
    `identifier`        VARCHAR(30)   NULL,
    `member_count`      INT           DEFAULT 0,
    `total_consumption` DECIMAL(12,2) DEFAULT 0,
    `points_multiplier` INT           DEFAULT 1,
    `discount`          DECIMAL(3,1)  DEFAULT 9.5,
    `benefits`          VARCHAR(500)  NULL,
    `status`            VARCHAR(20)   DEFAULT 'enabled' COMMENT 'enabled|disabled',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
