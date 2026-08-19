-- 生产初始化：创建系统管理员账号 (admin / 123456)
USE zb_tong;

INSERT INTO sys_user (username, password_hash, real_name, phone, status, is_deleted, created_at, updated_at)
SELECT 'admin', '$2a$10$9VPNBxDgOxdBaTphgPSaWu17C26Go0O6bcoCUrL3NX5pEqvLrKJsC', '系统管理员', '13800000000', 'ACTIVE', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, 1 FROM sys_user u WHERE u.username = 'admin' AND u.is_deleted = 0
AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = 1);
