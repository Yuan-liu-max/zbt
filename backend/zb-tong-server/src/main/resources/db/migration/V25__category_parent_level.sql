-- ============================================================
-- V25__category_parent_level.sql
-- product_category 表增加父子层级字段，支持二级子分类
-- ============================================================

ALTER TABLE `product_category`
    ADD COLUMN `parent_id` BIGINT NULL DEFAULT NULL COMMENT '父分类ID(NULL=一级)' AFTER `name`,
    ADD COLUMN `level`     INT    NOT NULL DEFAULT 1 COMMENT '层级 1=一级 2=二级' AFTER `parent_id`,
    ADD INDEX `idx_parent_id` (`parent_id`);
