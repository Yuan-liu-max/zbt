-- ============================================================
-- V9__add_perf_indexes.sql
-- 补充性能索引 — 每条语句带 IF NOT EXISTS 保护
-- ============================================================

-- 辅助存储过程：判断索引是否存在
DROP PROCEDURE IF EXISTS `create_index_if_not_exists`;
DELIMITER ;;
CREATE PROCEDURE `create_index_if_not_exists`(IN tableName VARCHAR(128), IN indexName VARCHAR(128), IN indexDef VARCHAR(512))
BEGIN
    DECLARE idxCount INT DEFAULT 0;
    SELECT COUNT(*) INTO idxCount
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = tableName
          AND index_name = indexName;
    IF idxCount = 0 THEN
        SET @sql = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, ' ', indexDef);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;;
DELIMITER ;

-- task_instance: 审核人查询
CALL create_index_if_not_exists('task_instance',   'idx_task_auditor',        '(auditor_id)');
-- task_submission: 提交时间排序
CALL create_index_if_not_exists('task_submission', 'idx_task_submission_time', '(submitted_at)');
-- product_inventory_check: 检查人筛选
CALL create_index_if_not_exists('product_inventory_check', 'idx_inventory_checker', '(checked_by)');
-- notification: 发送状态筛选
CALL create_index_if_not_exists('notification',    'idx_notification_status',  '(send_status)');
-- operate_log: 按目标类型和ID查询
CALL create_index_if_not_exists('operate_log',     'idx_operate_log_target',   '(target_type, target_id)');
-- store_monthly_score: 按月份查询
CALL create_index_if_not_exists('store_monthly_score', 'idx_store_score_month', '(score_month)');

-- 清理存储过程
DROP PROCEDURE IF EXISTS `create_index_if_not_exists`;
