-- V19__init_category_brand_config.sql — 商品分类、品牌、系统配置表
DROP TABLE IF EXISTS `product_category`;
DROP TABLE IF EXISTS `product_brand`;
DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `product_category` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(100) NOT NULL COMMENT '分类名称',
    `sort_order`   INT          DEFAULT 0,
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product_brand` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(100) NOT NULL COMMENT '品牌名称',
    `category_name` VARCHAR(100) NULL COMMENT '所属分类',
    `sort_order`    INT          DEFAULT 0,
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
    `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT         NULL COMMENT '配置值',
    `config_group` VARCHAR(50)  NULL COMMENT '配置分组',
    `description`  VARCHAR(500) NULL COMMENT '说明',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED|DISABLED',
    `sort_order`   INT          DEFAULT 0,
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_config_key` (`config_key`),
    INDEX `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
