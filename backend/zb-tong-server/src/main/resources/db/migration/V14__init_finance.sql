-- V14__init_finance.sql — 财务管理
DROP TABLE IF EXISTS `transaction`;

CREATE TABLE `transaction` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT,
    `code`             VARCHAR(32)   NULL,
    `type`             VARCHAR(10)   NULL COMMENT 'income|expense',
    `account`          VARCHAR(100)  NULL,
    `related_object`   VARCHAR(200)  NULL,
    `amount`           DECIMAL(12,2) NULL,
    `transaction_date` VARCHAR(20)   NULL,
    `remark`           VARCHAR(500)  NULL,
    `is_deleted`       TINYINT(1)    NOT NULL DEFAULT 0,
    `created_at`       DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_transaction_type` (`type`),
    INDEX `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
