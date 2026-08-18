-- ============================================================
-- V21__product_stock_brand_fields.sql
-- product 表增加真实库存列（此前 stock 为虚拟字段不落库）
-- product_brand 表补齐前端品牌管理表单字段
-- ============================================================

ALTER TABLE `product`
    ADD COLUMN `stock` INT NOT NULL DEFAULT 0 COMMENT '当前库存' AFTER `store_id`;
-- 注意：image_url 已在 V4 建表时定义，此处不再重复添加（避免 Duplicate column 错误）

ALTER TABLE `product_brand`
    ADD COLUMN `logo` VARCHAR(500) NULL DEFAULT NULL COMMENT '品牌LOGO' AFTER `category_name`,
    ADD COLUMN `origin` VARCHAR(100) NULL DEFAULT NULL COMMENT '品牌产地' AFTER `logo`,
    ADD COLUMN `description` VARCHAR(500) NULL DEFAULT NULL COMMENT '品牌描述' AFTER `origin`;
