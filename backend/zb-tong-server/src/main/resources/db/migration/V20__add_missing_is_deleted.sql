-- ============================================================
-- V20__add_missing_is_deleted.sql  补充 V10 遗漏的逻辑删除列（幂等）
-- 覆盖：AI/订单/采购/会员/报表/评分 模块业务表
-- ============================================================

DROP PROCEDURE IF EXISTS `add_missing_is_deleted`;
DELIMITER ;;
CREATE PROCEDURE `add_missing_is_deleted`(IN tbl VARCHAR(128))
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

CALL add_missing_is_deleted('prompt_template');
CALL add_missing_is_deleted('form_schema');
CALL add_missing_is_deleted('member_level');
CALL add_missing_is_deleted('order_item');
CALL add_missing_is_deleted('order_log');
CALL add_missing_is_deleted('order_return');
CALL add_missing_is_deleted('purchase_item');
CALL add_missing_is_deleted('report_snapshot');
CALL add_missing_is_deleted('store_monthly_score');

DROP PROCEDURE IF EXISTS `add_missing_is_deleted`;
