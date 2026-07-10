-- ============================================================
-- V6__init_sales.sql
-- 销售记录 / 销售明细 — 共2张表
-- ============================================================

-- 1. 销售记录表
DROP TABLE IF EXISTS `sales_record`;
CREATE TABLE `sales_record` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '销售记录ID',
    `sales_no`           VARCHAR(50)   NOT NULL COMMENT '销售单号',
    `store_id`           BIGINT        NOT NULL COMMENT '门店ID',
    `employee_id`        BIGINT        NOT NULL COMMENT '导购ID',
    `sales_date`         DATE          NOT NULL COMMENT '销售日期',
    `order_no`           VARCHAR(100)  NULL     DEFAULT NULL COMMENT '外部单据号',
    `total_amount`       DECIMAL(12,2) NOT NULL COMMENT '成交金额',
    `paid_amount`        DECIMAL(12,2) NOT NULL COMMENT '实收金额',
    `product_count`      INT           NOT NULL COMMENT '商品数量',
    `customer_type`      VARCHAR(10)   NOT NULL COMMENT 'NEW/OLD',
    `customer_gender`    VARCHAR(10)   NULL     DEFAULT NULL COMMENT 'MALE/FEMALE/UNKNOWN',
    `customer_age_range` VARCHAR(10)   NULL     DEFAULT NULL COMMENT '18-25/26-35/36-45/46+',
    `purchase_scene`     VARCHAR(20)   NOT NULL COMMENT 'WEDDING/GIFT/SELF/INVEST/HOLIDAY/OTHER',
    `customer_concern`   TEXT          NULL     DEFAULT NULL COMMENT '客户成交时在意点',
    `sales_photo_urls`   JSON          NULL     DEFAULT NULL COMMENT '销售单据照片',
    `remark`             TEXT          NULL     DEFAULT NULL COMMENT '备注',
    `audit_status`       VARCHAR(10)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/AUDITED/REJECTED',
    `auditor_id`         BIGINT        NULL     DEFAULT NULL COMMENT '审核人ID',
    `audit_comment`      TEXT          NULL     DEFAULT NULL COMMENT '审核意见',
    `audited_at`         DATETIME      NULL     DEFAULT NULL COMMENT '审核时间',
    `external_source`    VARCHAR(50)   NULL     DEFAULT NULL COMMENT 'POS系统来源(预留)',
    `external_order_id`  VARCHAR(100)  NULL     DEFAULT NULL COMMENT 'POS订单号(预留)',
    `sync_status`        VARCHAR(10)   NULL     DEFAULT NULL COMMENT '同步状态(预留)',
    `is_deleted`         TINYINT       NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_sales_no` (`sales_no`),
    INDEX `idx_sales_date` (`sales_date`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售记录表';

-- 2. 销售明细表
DROP TABLE IF EXISTS `sales_item`;
CREATE TABLE `sales_item` (
    `id`                     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `sales_record_id`        BIGINT        NOT NULL COMMENT '关联销售单ID',
    `product_id`             BIGINT        NULL     DEFAULT NULL COMMENT '商品ID(可选)',
    `product_name`           VARCHAR(200)  NOT NULL COMMENT '商品名称',
    `category`               VARCHAR(20)   NOT NULL COMMENT '品类',
    `style`                  VARCHAR(100)  NULL     DEFAULT NULL COMMENT '款式',
    `material`               VARCHAR(50)   NULL     DEFAULT NULL COMMENT '材质',
    `weight`                 VARCHAR(50)   NULL     DEFAULT NULL COMMENT '重量',
    `size`                   VARCHAR(50)   NULL     DEFAULT NULL COMMENT '手寸/圈号',
    `color`                  VARCHAR(50)   NULL     DEFAULT NULL COMMENT '颜色',
    `shape`                  VARCHAR(50)   NULL     DEFAULT NULL COMMENT '形状',
    `meaning`                VARCHAR(200)  NULL     DEFAULT NULL COMMENT '寓意',
    `price`                  DECIMAL(10,2) NOT NULL COMMENT '成交价',
    `quantity`               INT           NOT NULL DEFAULT 1  COMMENT '数量',
    `gross_margin_rate`      DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '毛利率(%)',
    `customer_favorite_point` TEXT         NULL     DEFAULT NULL COMMENT '客户喜欢点',
    `objection`              TEXT          NULL     DEFAULT NULL COMMENT '客户异议',
    `closing_reason`         TEXT          NULL     DEFAULT NULL COMMENT '成交原因',
    `product_photo_urls`     JSON          NULL     DEFAULT NULL COMMENT '货品图',
    `created_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_sales_record_id` (`sales_record_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售明细表';
