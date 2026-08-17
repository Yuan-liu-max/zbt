-- ============================================================
-- V48__init_message.sql
-- 站内信/私信表 —— 用于顶部"邮件"按钮 + /message 页面
-- ============================================================

DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
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
