-- ============================================================
-- V38__fix_seed_passwords.sql
-- 修复 V37 测试用户密码哈希（密码：123456）
-- 使用 BCryptPasswordEncoder 生成有效哈希
-- ============================================================

-- 生产上线安全：测试账号已在 V37 注释，此处修复密码哈希的 UPDATE 同步注释
-- UPDATE `sys_user` SET `password_hash` = '$2b$10$w.yQxOMRGdsqJcz3hSM0yeU.Wq6n/4PMEyi0ngwghKTvf0oDcpmE6'
-- WHERE `id` IN (100, 101) AND `username` IN ('customer1', 'customer2');
