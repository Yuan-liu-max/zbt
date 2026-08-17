-- V21__add_warehouse_column.sql — 盘点表添加 warehouse 字段
ALTER TABLE `product_inventory_check` ADD COLUMN `warehouse` VARCHAR(200) NULL COMMENT '仓库' AFTER `photos`;
