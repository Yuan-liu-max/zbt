-- V11__init_order.sql — 订单模块（sales_order / order_item / order_log / order_return）
DROP TABLE IF EXISTS `order_return`;
DROP TABLE IF EXISTS `order_log`;
DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `sales_order`;

CREATE TABLE `sales_order` (
    `id`               BIGINT         NOT NULL AUTO_INCREMENT,
    `order_code`       VARCHAR(32)    NOT NULL,
    `customer_name`    VARCHAR(50)    NULL,
    `customer_phone`   VARCHAR(20)    NULL,
    `customer_address` VARCHAR(200)   NULL,
    `total_amount`     DECIMAL(12,2)  NULL,
    `freight`          DECIMAL(8,2)   DEFAULT 0,
    `coupon_discount`  DECIMAL(8,2)   DEFAULT 0,
    `order_amount`     DECIMAL(12,2)  NULL,
    `order_status`     VARCHAR(20)    NULL,
    `payment_status`   VARCHAR(20)    NULL,
    `payment_method`   VARCHAR(50)    NULL,
    `delivery_method`  VARCHAR(50)    NULL,
    `remark`           VARCHAR(500)   NULL,
    `is_deleted`       TINYINT(1)     NOT NULL DEFAULT 0,
    `created_at`       DATETIME       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME       NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_order_code` (`order_code`),
    INDEX `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`      BIGINT        NOT NULL,
    `product_code`  VARCHAR(32)   NULL,
    `product_name`  VARCHAR(100)  NULL,
    `image_url`     VARCHAR(500)  NULL,
    `spec`          VARCHAR(100)  NULL,
    `quantity`      INT           DEFAULT 1,
    `price`         DECIMAL(10,2) NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_log` (
    `id`       BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT       NOT NULL,
    `time`     VARCHAR(20)  NULL,
    `content`  VARCHAR(500) NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_order_log_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_return` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `return_code`   VARCHAR(32)   NULL,
    `order_code`    VARCHAR(32)   NULL,
    `return_type`   VARCHAR(20)   NULL,
    `reason`        VARCHAR(500)  NULL,
    `apply_time`    VARCHAR(20)   NULL,
    `status`        VARCHAR(20)   NULL,
    `order_amount`  DECIMAL(12,2) NULL,
    `product_name`  VARCHAR(100)  NULL,
    `product_spec`  VARCHAR(100)  NULL,
    `image_url`     VARCHAR(500)  NULL,
    `quantity`      INT           NULL,
    `created_at`    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_return_code` (`return_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
