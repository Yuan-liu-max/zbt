-- V31__add_customer_support.sql — 顾客端支持：订单归属 + 退款完善 + 顾客角色
-- （V30 重命名，修复 Flyway 校验失败问题）

-- 1. 订单表加 user_id 外键（如果字段已存在则跳过）
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sales_order' AND COLUMN_NAME = 'user_id');
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `sales_order` ADD COLUMN `user_id` BIGINT NULL AFTER `id`, ADD INDEX `idx_user_id` (`user_id`)',
  'SELECT ''column user_id already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 退换货表加 order_id（如果字段已存在则跳过）
SET @col_exists2 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_return' AND COLUMN_NAME = 'order_id');
SET @sql2 = IF(@col_exists2 = 0,
  'ALTER TABLE `order_return` ADD COLUMN `order_id` BIGINT NULL AFTER `id`, ADD INDEX `idx_return_order_id` (`order_id`)',
  'SELECT ''column order_id already exists''');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- 3. 退换货表加 refund_amount（如果字段已存在则跳过）
SET @col_exists3 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_return' AND COLUMN_NAME = 'refund_amount');
SET @sql3 = IF(@col_exists3 = 0,
  'ALTER TABLE `order_return` ADD COLUMN `refund_amount` DECIMAL(12,2) NULL AFTER `order_amount`',
  'SELECT ''column refund_amount already exists''');
PREPARE stmt3 FROM @sql3; EXECUTE stmt3; DEALLOCATE PREPARE stmt3;

-- 4. 新增 ROLE_CUSTOMER 顾客角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `remark`, `data_scope`, `status`, `created_at`)
VALUES (6, 'ROLE_CUSTOMER', '顾客', '商城注册用户，仅可查看和操作自己的订单', 'NONE', 'ENABLED', NOW());
