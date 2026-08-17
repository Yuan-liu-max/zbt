-- ============================================================
-- V22__inventory_check_fields.sql
-- product_inventory_check 表补齐盘点单管理字段（前端 InventoryCheck 页面契约）
-- ============================================================

ALTER TABLE `product_inventory_check`
    ADD COLUMN `check_code` VARCHAR(50)  NULL DEFAULT NULL COMMENT '盘点单号' AFTER `id`,
    ADD COLUMN `check_name` VARCHAR(200) NULL DEFAULT NULL COMMENT '盘点名称' AFTER `check_code`,
    ADD COLUMN `check_type` VARCHAR(50)  NULL DEFAULT NULL COMMENT '盘点类型(月度盘点/周盘点/临时盘点)' AFTER `check_name`,
    ADD COLUMN `start_date` DATE         NULL DEFAULT NULL COMMENT '盘点开始日期' AFTER `check_type`,
    ADD COLUMN `end_date`   DATE         NULL DEFAULT NULL COMMENT '盘点结束日期' AFTER `start_date`,
    ADD COLUMN `status`     VARCHAR(20)  NOT NULL DEFAULT 'planning' COMMENT 'planning/counting/completed/cancelled' AFTER `end_date`,
    ADD INDEX `idx_check_code` (`check_code`),
    ADD INDEX `idx_status` (`status`);
