-- ============================================================
-- V10__add_is_deleted.sql — 补全逻辑删除字段（幂等）
-- ============================================================

DROP PROCEDURE IF EXISTS `add_is_deleted_col`;
DELIMITER ;;
CREATE PROCEDURE `add_is_deleted_col`(IN tbl VARCHAR(128))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = tbl AND column_name = 'is_deleted'
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0');
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END;;
DELIMITER ;

CALL add_is_deleted_col('task_instance');
CALL add_is_deleted_col('task_submission');
CALL add_is_deleted_col('task_audit');
CALL add_is_deleted_col('task_reminder_log');
CALL add_is_deleted_col('employee_profile');
CALL add_is_deleted_col('employee_interview');
CALL add_is_deleted_col('employee_assessment');
CALL add_is_deleted_col('employee_training');
CALL add_is_deleted_col('employee_training_record');
CALL add_is_deleted_col('employee_monthly_review');
CALL add_is_deleted_col('employee_level_record');
CALL add_is_deleted_col('product_inventory_check');
CALL add_is_deleted_col('product_maintenance_check');
CALL add_is_deleted_col('product_sales_analysis');
CALL add_is_deleted_col('new_product_plan');
CALL add_is_deleted_col('promotion_plan');
CALL add_is_deleted_col('scene_health_inspection');
CALL add_is_deleted_col('scene_display_inspection');
CALL add_is_deleted_col('scene_material_update');
CALL add_is_deleted_col('scene_equipment_check');
CALL add_is_deleted_col('scene_customer_experience_review');
CALL add_is_deleted_col('sales_item');
CALL add_is_deleted_col('notification');
CALL add_is_deleted_col('ai_result');
CALL add_is_deleted_col('operate_log');

DROP PROCEDURE IF EXISTS `add_is_deleted_col`;
