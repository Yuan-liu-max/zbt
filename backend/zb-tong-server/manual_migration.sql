-- 手动执行迁移脚本（Flyway 已禁用）
-- 1. 订单表加 user_id
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'zb_tong_dev' AND TABLE_NAME = 'sales_order' AND COLUMN_NAME = 'user_id');
SET @sql1 = IF(@col_exists = 0,
  'ALTER TABLE `sales_order` ADD COLUMN `user_id` BIGINT NULL AFTER `id`, ADD INDEX `idx_user_id` (`user_id`)',
  'SELECT "user_id already exists"');
PREPARE stmt1 FROM @sql1; EXECUTE stmt1; DEALLOCATE PREPARE stmt1;

-- 2. 退换货表加 order_id
SET @col_exists2 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'zb_tong_dev' AND TABLE_NAME = 'order_return' AND COLUMN_NAME = 'order_id');
SET @sql2 = IF(@col_exists2 = 0,
  'ALTER TABLE `order_return` ADD COLUMN `order_id` BIGINT NULL AFTER `id`, ADD INDEX `idx_return_order_id` (`order_id`)',
  'SELECT "order_id already exists"');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- 3. 退换货表加 refund_amount
SET @col_exists3 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'zb_tong_dev' AND TABLE_NAME = 'order_return' AND COLUMN_NAME = 'refund_amount');
SET @sql3 = IF(@col_exists3 = 0,
  'ALTER TABLE `order_return` ADD COLUMN `refund_amount` DECIMAL(12,2) NULL AFTER `order_amount`',
  'SELECT "refund_amount already exists"');
PREPARE stmt3 FROM @sql3; EXECUTE stmt3; DEALLOCATE PREPARE stmt3;

-- 4. 新增 ROLE_CUSTOMER（sys_role 表无 description/is_deleted 列，用 remark）
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `remark`, `data_scope`, `status`)
VALUES (6, 'ROLE_CUSTOMER', '顾客', '商城注册用户，仅可查看和操作自己的订单', 'NONE', 'ENABLED');

-- 5. 删除失败的 V30 记录
DELETE FROM `flyway_schema_history` WHERE `version` = '30';

-- 6. 员工能力考核表加 type 字段（考核类型）
SET @col_exists_type = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'zb_tong_dev' AND TABLE_NAME = 'employee_assessment' AND COLUMN_NAME = 'type');
SET @sql_type = IF(@col_exists_type = 0,
  'ALTER TABLE `employee_assessment` ADD COLUMN `type` VARCHAR(20) NULL DEFAULT NULL AFTER `assessment_week`',
  'SELECT "type already exists"');
PREPARE stmt_type FROM @sql_type; EXECUTE stmt_type; DEALLOCATE PREPARE stmt_type;

-- 7. sys_user 表加 token_version（强制下线用）
SET @col_exists_tv = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'zb_tong_dev' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'token_version');
SET @sql_tv = IF(@col_exists_tv = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT ''令牌版本号，强制下线时+1''',
  'SELECT "token_version already exists"');
PREPARE stmt_tv FROM @sql_tv; EXECUTE stmt_tv; DEALLOCATE PREPARE stmt_tv;

-- 8. 场景运营 JSON 列改 TEXT（前端存逗号分隔字符串/自由文本，JSON 列会拒绝非法 JSON）
ALTER TABLE `scene_health_inspection` MODIFY `photo_urls` TEXT,
                                          MODIFY `area_results` TEXT;
ALTER TABLE `scene_display_inspection` MODIFY `before_photos` TEXT,
                                             MODIFY `after_photos` TEXT;
ALTER TABLE `scene_material_update` MODIFY `updated_photos` TEXT;
ALTER TABLE `scene_equipment_check` MODIFY `photo_urls` TEXT;

-- 9. AI智能问答历史表
CREATE TABLE IF NOT EXISTS `ai_chat_history` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question`   TEXT        NULL COMMENT '用户问题',
    `answer`     TEXT        NULL COMMENT 'AI回答',
    `model_name` VARCHAR(64) NULL COMMENT '模型名',
    `is_deleted` TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI智能问答历史';

-- 10. ai_result 表 JSON 列改 TEXT（AI 输出/快照/Token 可能是空串或纯文本，JSON 列会拒绝非法 JSON）
ALTER TABLE `ai_result` MODIFY COLUMN `input_snapshot` TEXT NULL DEFAULT NULL COMMENT '输入快照';
ALTER TABLE `ai_result` MODIFY COLUMN `output_json` TEXT NULL DEFAULT NULL COMMENT '结构化结果';
ALTER TABLE `ai_result` MODIFY COLUMN `token_usage` TEXT NULL DEFAULT NULL COMMENT 'Token消耗';

-- 11. 站内信/私信表
CREATE TABLE IF NOT EXISTS `sys_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sender_id`   BIGINT       NULL     DEFAULT NULL COMMENT '发送人ID(系统消息为空)',
    `receiver_id` BIGINT       NOT NULL COMMENT '接收人ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `content`     TEXT         NULL     DEFAULT NULL COMMENT '内容',
    `is_read`     TINYINT      NOT NULL DEFAULT 0  COMMENT '是否已读 0=否 1=是',
    `read_at`     DATETIME     NULL     DEFAULT NULL COMMENT '阅读时间',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信/私信表';

-- 12. 提示词模板默认数据（文档生成用）
INSERT INTO `prompt_template` (`template_name`, `business_type`, `prompt_content`, `model_name`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
('员工分析报告', 'EMPLOYEE', '请生成一份《员工分析报告》，要求包含以下章节：一、员工基本情况；二、优势与亮点；三、待提升领域；四、改进与发展建议。语言专业、条理清晰。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()),
('货品运营分析报告', 'PRODUCT', '请生成一份《货品运营分析报告》，要求包含以下章节：一、商品结构分析；二、动销与库存情况；三、问题诊断；四、运营改进建议。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()),
('门店场景巡检报告', 'SCENE', '请生成一份《门店场景巡检报告》，要求包含以下章节：一、巡检概况；二、发现问题；三、整改措施；四、后续跟进计划。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()),
('任务复盘报告', 'TASK', '请生成一份《任务复盘报告》，要求包含以下章节：一、任务背景与目标；二、执行情况；三、问题与不足；四、改进计划。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW());
