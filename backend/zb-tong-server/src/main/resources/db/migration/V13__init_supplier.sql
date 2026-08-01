-- V13__init_supplier.sql — 供应商管理
DROP TABLE IF EXISTS `supplier`;

CREATE TABLE `supplier` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(100) NULL,
    `logo`           VARCHAR(500) NULL,
    `type`           VARCHAR(30)  NULL DEFAULT 'raw_material' COMMENT 'raw_material|gemstone|pearl|processing|packaging|consumable',
    `contact_person` VARCHAR(50)  NULL,
    `contact_phone`  VARCHAR(20)  NULL,
    `email`          VARCHAR(100) NULL,
    `status`         VARCHAR(20)  NULL DEFAULT 'cooperating' COMMENT 'cooperating|suspended|terminated',
    `address`        VARCHAR(300) NULL,
    `remark`         VARCHAR(500) NULL,
    `is_deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_supplier_type` (`type`),
    INDEX `idx_supplier_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
