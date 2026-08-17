-- V44: employee_assessment 表增加考核类型字段（monthly/quarterly/special）
ALTER TABLE `employee_assessment`
    ADD COLUMN `type` VARCHAR(20) NULL DEFAULT NULL COMMENT '考核类型：monthly/quarterly/special' AFTER `assessment_week`;
