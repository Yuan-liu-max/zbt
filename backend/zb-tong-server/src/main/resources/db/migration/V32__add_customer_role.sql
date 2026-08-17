-- ============================================================
-- V32__add_customer_role.sql
-- 商城C端用户角色 + 种子测试用户
-- ============================================================

-- 添加 ROLE_CUSTOMER 角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `data_scope`, `status`, `remark`) VALUES
(6, 'ROLE_CUSTOMER', '商城顾客', 'SELF', 'ENABLED', 'C端商城注册用户，仅可查看和购买商品');

