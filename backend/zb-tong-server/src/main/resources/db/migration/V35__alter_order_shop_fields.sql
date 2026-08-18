-- ============================================================
-- V35__alter_order_shop_fields.sql
-- 订单表增加商城字段：地址/支付/物流/类型
-- ============================================================

ALTER TABLE `sales_order`
    ADD COLUMN `buyer_id` BIGINT COMMENT '买家用户ID' AFTER `user_id`,
    ADD COLUMN `address_id` BIGINT COMMENT '收货地址ID' AFTER `customer_address`,
    ADD COLUMN `address_snapshot` TEXT COMMENT '下单时地址快照(JSON)' AFTER `address_id`,
    ADD COLUMN `payment_time` DATETIME COMMENT '支付时间' AFTER `payment_method`,
    ADD COLUMN `payment_trade_no` VARCHAR(64) COMMENT '第三方支付流水号' AFTER `payment_time`,
    ADD COLUMN `delivery_company` VARCHAR(50) COMMENT '物流公司' AFTER `delivery_method`,
    ADD COLUMN `delivery_track_no` VARCHAR(64) COMMENT '物流单号' AFTER `delivery_company`,
    ADD COLUMN `delivery_time` DATETIME COMMENT '发货时间' AFTER `delivery_track_no`,
    ADD COLUMN `receive_time` DATETIME COMMENT '签收时间' AFTER `delivery_time`,
    ADD COLUMN `finish_time` DATETIME COMMENT '完成时间' AFTER `receive_time`,
    ADD COLUMN `order_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '订单类型: SHOP/MANUAL' AFTER `finish_time`;
