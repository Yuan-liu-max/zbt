-- V18__init_purchase.sql — 采购模块
DROP TABLE IF EXISTS `purchase_item`;
DROP TABLE IF EXISTS `purchase_order`;

CREATE TABLE `purchase_order` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`     VARCHAR(32)   NOT NULL,
    `store_id`     BIGINT        NULL,
    `supplier_id`  BIGINT        NULL,
    `total_amount` DECIMAL(12,2) NULL,
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT|SUBMITTED|APPROVED|REJECTED',
    `applicant_id` BIGINT        NULL,
    `approver_id`  BIGINT        NULL,
    `remark`       VARCHAR(500)  NULL,
    `is_deleted`   TINYINT(1)    NOT NULL DEFAULT 0,
    `created_at`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `purchase_item` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`     BIGINT        NOT NULL,
    `product_id`   BIGINT        NULL,
    `product_name` VARCHAR(100)  NULL,
    `quantity`     INT           DEFAULT 1,
    `price`        DECIMAL(10,2) NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
