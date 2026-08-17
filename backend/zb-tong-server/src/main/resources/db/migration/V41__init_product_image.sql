-- ============================================================
-- V41__init_product_image.sql
-- 商品图片表 — 数据库化管理，支持多图、排序、主图
-- ============================================================

DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`   BIGINT       NOT NULL COMMENT '商品ID',
    `image_url`    VARCHAR(500) NOT NULL COMMENT '图片URL（/files/static/... 或 https://...）',
    `sort_order`   INT          NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',
    `is_primary`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否主图 0=否 1=是',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';
