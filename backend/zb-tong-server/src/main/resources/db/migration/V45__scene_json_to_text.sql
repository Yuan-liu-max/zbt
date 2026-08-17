-- V45: 场景运营 JSON 列改为 TEXT
-- 前端以「逗号分隔字符串 + 自由文本」方式存储照片/区域结果，JSON 列会拒绝非法 JSON 导致写入失败
ALTER TABLE `scene_health_inspection` MODIFY COLUMN `area_results` TEXT NULL DEFAULT NULL COMMENT '各区域检查结果';
ALTER TABLE `scene_health_inspection` MODIFY COLUMN `photo_urls` TEXT NULL DEFAULT NULL COMMENT '照片';
ALTER TABLE `scene_display_inspection` MODIFY COLUMN `before_photos` TEXT NULL DEFAULT NULL COMMENT '整改前照片';
ALTER TABLE `scene_display_inspection` MODIFY COLUMN `after_photos` TEXT NULL DEFAULT NULL COMMENT '整改后照片';
ALTER TABLE `scene_material_update` MODIFY COLUMN `updated_photos` TEXT NULL DEFAULT NULL COMMENT '更新后照片';
ALTER TABLE `scene_equipment_check` MODIFY COLUMN `photo_urls` TEXT NULL DEFAULT NULL COMMENT '照片';
