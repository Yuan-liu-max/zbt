-- ============================================================
-- V52: 行政区域字典表（省市区三级，地址校验清洗用）
-- 数据由后端启动时通过高德 district API 自动同步，无需手工维护
-- ============================================================

CREATE TABLE IF NOT EXISTS `region_dict` (
  `code`        varchar(20)  NOT NULL COMMENT '行政编码(adcode)',
  `name`        varchar(100) NOT NULL COMMENT '区域名称',
  `level`       varchar(20)  NOT NULL COMMENT 'province|city|district',
  `parent_code` varchar(20)  DEFAULT NULL COMMENT '上级行政编码(省=null, 市=省code, 区=市code)',
  `pinyin`      varchar(100) DEFAULT NULL COMMENT '拼音（预留）',
  `sort`        int          DEFAULT '0' COMMENT '排序',
  `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`),
  KEY `idx_region_parent` (`parent_code`),
  KEY `idx_region_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行政区域字典表';
