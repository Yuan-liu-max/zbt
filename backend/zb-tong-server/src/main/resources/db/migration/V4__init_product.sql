-- ============================================================
-- V4__init_product.sql
-- 商品 / 盘点 / 养护 / 动销分析 / 新品方案 / 促销方案 — 共6张表
-- ============================================================

-- 1. 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `product_code`      VARCHAR(50)   NOT NULL COMMENT '商品编码',
    `product_name`      VARCHAR(200)  NOT NULL COMMENT '商品名称',
    `category`          VARCHAR(20)   NOT NULL COMMENT 'GOLD/DIAMOND/K_GOLD/ANCIENT/SILVER/GEM/OTHER',
    `style`             VARCHAR(100)  NULL     DEFAULT NULL COMMENT '款式',
    `material`          VARCHAR(50)   NULL     DEFAULT NULL COMMENT '材质',
    `weight`            VARCHAR(50)   NULL     DEFAULT NULL COMMENT '重量',
    `size`              VARCHAR(50)   NULL     DEFAULT NULL COMMENT '手寸/圈号',
    `color`             VARCHAR(50)   NULL     DEFAULT NULL COMMENT '颜色',
    `shape`             VARCHAR(50)   NULL     DEFAULT NULL COMMENT '形状',
    `meaning`           VARCHAR(200)  NULL     DEFAULT NULL COMMENT '寓意',
    `cost_price`        DECIMAL(10,2) NULL     DEFAULT NULL COMMENT '成本价(权限控制)',
    `retail_price`      DECIMAL(10,2) NULL     DEFAULT NULL COMMENT '零售价',
    `gross_margin_rate` DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '毛利率(%)(权限控制)',
    `status`            VARCHAR(20)   NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/SOLD/TRANSFER/REPAIR/OFF_SHELF',
    `store_id`          BIGINT        NULL     DEFAULT NULL COMMENT '所属门店ID',
    `image_url`         VARCHAR(500)  NULL     DEFAULT NULL COMMENT '商品图片URL',
    `is_deleted`        TINYINT       NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_product_code` (`product_code`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 2. 每日盘点表
DROP TABLE IF EXISTS `product_inventory_check`;
CREATE TABLE `product_inventory_check` (
    `id`                  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `check_date`          DATE     NOT NULL COMMENT '检查日期',
    `store_id`            BIGINT   NULL     DEFAULT NULL COMMENT '门店ID',
    `checked_by`          BIGINT   NULL     DEFAULT NULL COMMENT '检查人ID',
    `total_checked_count` INT      NOT NULL COMMENT '盘点总数',
    `abnormal_count`      INT      NOT NULL DEFAULT 0  COMMENT '异常数量',
    `abnormal_items`      JSON     NULL     DEFAULT NULL COMMENT '异常商品列表JSON',
    `photos`              JSON     NULL     DEFAULT NULL COMMENT '照片URL',
    `remark`              TEXT     NULL     DEFAULT NULL COMMENT '备注',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_check_date` (`check_date`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_checked_by` (`checked_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日盘点表';

-- 3. 货品养护检查表
DROP TABLE IF EXISTS `product_maintenance_check`;
CREATE TABLE `product_maintenance_check` (
    `id`                  BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`          BIGINT      NULL     DEFAULT NULL COMMENT '商品ID',
    `category`            VARCHAR(20) NOT NULL COMMENT '品类',
    `check_date`          DATE        NOT NULL COMMENT '检查日期',
    `checker_id`          BIGINT      NULL     DEFAULT NULL COMMENT '检查人ID',
    `maintenance_result`  VARCHAR(20) NOT NULL COMMENT 'NORMAL/NEED_CLEAN/NEED_REPAIR/NEED_OFF_SHELF',
    `issue_description`   TEXT        NULL     DEFAULT NULL COMMENT '问题描述',
    `photo_urls`          JSON        NULL     DEFAULT NULL COMMENT '照片',
    `handled_result`      TEXT        NULL     DEFAULT NULL COMMENT '处理结果',
    `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_check_date` (`check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='货品养护检查表';

-- 4. 周度动销分析表
DROP TABLE IF EXISTS `product_sales_analysis`;
CREATE TABLE `product_sales_analysis` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `analysis_week`          VARCHAR(20)  NOT NULL COMMENT '周标识(2026-W01)',
    `store_id`               BIGINT       NULL     DEFAULT NULL COMMENT '门店ID',
    `analyzer_id`            BIGINT       NULL     DEFAULT NULL COMMENT '分析人ID',
    `hot_products`           JSON         NULL     DEFAULT NULL COMMENT '爆款商品JSON',
    `normal_products`        JSON         NULL     DEFAULT NULL COMMENT '平销商品',
    `slow_products`          JSON         NULL     DEFAULT NULL COMMENT '慢销商品',
    `no_sales_7_days`        JSON         NULL     DEFAULT NULL COMMENT '连续7天无动销',
    `stockout_risk_products` JSON         NULL     DEFAULT NULL COMMENT '缺货风险商品',
    `analysis_summary`       TEXT         NULL     DEFAULT NULL COMMENT '分析总结',
    `action_plan`            TEXT         NULL     DEFAULT NULL COMMENT '行动计划',
    `created_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_analysis_week` (`analysis_week`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周度动销分析表';

-- 5. 新品推介方案表
DROP TABLE IF EXISTS `new_product_plan`;
CREATE TABLE `new_product_plan` (
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `plan_month`            VARCHAR(7)    NOT NULL COMMENT '计划月份',
    `store_id`              BIGINT        NULL     DEFAULT NULL COMMENT '门店ID',
    `planner_id`            BIGINT        NULL     DEFAULT NULL COMMENT '策划人ID',
    `new_product_list`      JSON          NULL     DEFAULT NULL COMMENT '新品清单JSON',
    `selling_points`        TEXT          NULL     DEFAULT NULL COMMENT '核心卖点',
    `target_customer_group` TEXT          NULL     DEFAULT NULL COMMENT '主推人群',
    `display_plan`          TEXT          NULL     DEFAULT NULL COMMENT '陈列展示方案',
    `training_plan`         TEXT          NULL     DEFAULT NULL COMMENT '员工培训计划',
    `sales_target`          DECIMAL(12,2) NULL     DEFAULT NULL COMMENT '销售目标',
    `promotion_script`      TEXT          NULL     DEFAULT NULL COMMENT '推荐话术',
    `attachment_urls`       JSON          NULL     DEFAULT NULL COMMENT '附件',
    `status`                VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/APPROVED',
    `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_plan_month` (`plan_month`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新品推介方案表';

-- 6. 促销活动筹备表
DROP TABLE IF EXISTS `promotion_plan`;
CREATE TABLE `promotion_plan` (
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_month`        VARCHAR(7)    NOT NULL COMMENT '活动月份',
    `store_id`              BIGINT        NULL     DEFAULT NULL COMMENT '门店ID',
    `activity_name`         VARCHAR(200)  NOT NULL COMMENT '活动名称',
    `activity_theme`        VARCHAR(200)  NULL     DEFAULT NULL COMMENT '活动主题',
    `activity_period_start` DATE          NULL     DEFAULT NULL COMMENT '活动开始',
    `activity_period_end`   DATE          NULL     DEFAULT NULL COMMENT '活动结束',
    `promotion_rules`       TEXT          NULL     DEFAULT NULL COMMENT '优惠规则',
    `main_products`         JSON          NULL     DEFAULT NULL COMMENT '主推商品',
    `material_requirements` TEXT          NULL     DEFAULT NULL COMMENT '活动物料',
    `employee_script`       TEXT          NULL     DEFAULT NULL COMMENT '员工话术',
    `preheat_plan`          TEXT          NULL     DEFAULT NULL COMMENT '预热计划',
    `customer_reach_plan`   TEXT          NULL     DEFAULT NULL COMMENT '老客触达计划',
    `expected_sales`        DECIMAL(12,2) NULL     DEFAULT NULL COMMENT '预期销售额',
    `status`                VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/APPROVED',
    `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_activity_month` (`activity_month`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销活动筹备表';
