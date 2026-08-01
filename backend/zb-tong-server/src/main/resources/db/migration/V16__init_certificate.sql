-- V16__init_certificate.sql — 证书管理
DROP TABLE IF EXISTS `certificate`;

CREATE TABLE `certificate` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NULL,
    `type`        VARCHAR(20)  NULL COMMENT 'gia|ngtc|gic|other',
    `product_name` VARCHAR(100) NULL,
    `issuer`      VARCHAR(100) NULL,
    `issue_date`  VARCHAR(20)  NULL,
    `expiry_date` VARCHAR(20)  NULL,
    `status`      VARCHAR(20)  NULL COMMENT 'valid|expiring|expired',
    `file_url`    VARCHAR(500) NULL,
    `remark`      VARCHAR(500) NULL,
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_cert_code` (`code`),
    INDEX `idx_cert_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
