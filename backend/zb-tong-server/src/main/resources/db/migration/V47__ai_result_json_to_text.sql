-- V47: ai_result 表 JSON 列改 TEXT（AI 输出/快照/Token 可能是空串或纯文本，JSON 列会拒绝非法 JSON）
ALTER TABLE `ai_result` MODIFY COLUMN `input_snapshot` TEXT NULL DEFAULT NULL COMMENT '输入快照';
ALTER TABLE `ai_result` MODIFY COLUMN `output_json` TEXT NULL DEFAULT NULL COMMENT '结构化结果';
ALTER TABLE `ai_result` MODIFY COLUMN `token_usage` TEXT NULL DEFAULT NULL COMMENT 'Token消耗';
