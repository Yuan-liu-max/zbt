-- V23__add_user_settings.sql
-- 个人设置扩展：邮箱、时区、语言、日期格式、通知开关

ALTER TABLE `sys_user`
    ADD COLUMN `email`            VARCHAR(100) NULL COMMENT '邮箱' AFTER `phone`,
    ADD COLUMN `timezone`         VARCHAR(100) NULL COMMENT '所在时区' AFTER `avatar`,
    ADD COLUMN `language`         VARCHAR(20)  NOT NULL DEFAULT '简体中文' COMMENT '语言' AFTER `timezone`,
    ADD COLUMN `date_format`      VARCHAR(20)  NOT NULL DEFAULT 'YYYY-MM-DD' COMMENT '日期格式' AFTER `language`,
    ADD COLUMN `notify_system`    TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '系统通知' AFTER `date_format`,
    ADD COLUMN `notify_order`     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '订单提醒' AFTER `notify_system`,
    ADD COLUMN `notify_inventory` TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '库存预警' AFTER `notify_order`,
    ADD COLUMN `notify_marketing` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '营销活动' AFTER `notify_inventory`;
