-- V42__add_customer_user_id.sql — customer 表与 sys_user 表关联
ALTER TABLE `customer`
    ADD COLUMN `user_id` BIGINT NULL AFTER `id`,
    ADD INDEX `idx_customer_user_id` (`user_id`);
