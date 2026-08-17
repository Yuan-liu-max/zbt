-- V46: AI智能问答历史表
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
