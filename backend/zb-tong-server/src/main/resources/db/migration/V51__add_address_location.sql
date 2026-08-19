-- ============================================================
-- V51: 收货地址增加定位坐标（新增地址定位功能，经纬度存库）
-- ============================================================

ALTER TABLE `user_address`
  ADD COLUMN `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度（定位）' AFTER `detail_address`,
  ADD COLUMN `latitude`  decimal(10,6) DEFAULT NULL COMMENT '纬度（定位）' AFTER `longitude`;
