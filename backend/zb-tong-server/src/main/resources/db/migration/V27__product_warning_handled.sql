-- V27__product_warning_handled.sql — 商品表添加预警处理时间
ALTER TABLE `product` ADD COLUMN `warning_handled_at` DATETIME NULL COMMENT '最近一次预警处理时间';
