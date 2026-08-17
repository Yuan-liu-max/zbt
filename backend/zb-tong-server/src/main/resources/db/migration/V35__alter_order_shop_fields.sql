-- ============================================================
-- V35__alter_order_shop_fields.sql
-- 订单表增加商城字段：地址/支付/物流/类型
-- ============================================================

ALTER TABLE `sales_order`
    ADD COLUMN IF NOT EXISTS `buyer_id` BIGINT COMMENT '买家用户ID' AFTER `user_id`,
    ADD COLUMN IF NOT EXISTS `address_id` BIGINT COMMENT '收货地址ID' AFTER `customer_address`,
    ADD COLUMN IF NOT EXISTS `address_snapshot` TEXT COMMENT '下单时地址快照(JSON)' AFTER `address_id`,
    ADD COLUMN IF NOT EXISTS `payment_time` DATETIME COMMENT '支付时间' AFTER `payment_method`,
    ADD COLUMN IF NOT EXISTS `payment_trade_no` VARCHAR(64) COMMENT '第三方支付流水号' AFTER `payment_time`,
    ADD COLUMN IF NOT EXISTS `delivery_company` VARCHAR(50) COMMENT '物流公司' AFTER `delivery_method`,
    ADD COLUMN IF NOT EXISTS `delivery_track_no` VARCHAR(64) COMMENT '物流单号' AFTER `delivery_company`,
    ADD COLUMN IF NOT EXISTS `delivery_time` DATETIME COMMENT '发货时间' AFTER `delivery_track_no`,
    ADD COLUMN IF NOT EXISTS `receive_time` DATETIME COMMENT '签收时间' AFTER `delivery_time`,
    ADD COLUMN IF NOT EXISTS `finish_time` DATETIME COMMENT '完成时间' AFTER `receive_time`,
    ADD COLUMN IF NOT EXISTS `order_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '订单类型: SHOP/MANUAL' AFTER `finish_time`;
