-- ============================================================
-- V1__init_org_user.sql
-- 组织架构 / 门店 / 用户 / 角色 / 权限 — 共8张表
-- ============================================================

-- 1. 组织表
DROP TABLE IF EXISTS `sys_organization`;
CREATE TABLE `sys_organization` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '组织ID',
    `parent_id`   BIGINT       NULL     DEFAULT NULL COMMENT '上级组织ID',
    `org_name`    VARCHAR(100) NOT NULL COMMENT '组织名称',
    `org_type`    VARCHAR(20)  NOT NULL COMMENT 'HEADQUARTERS/GREAT_REGION/REGION/STORE',
    `org_code`    VARCHAR(50)  NOT NULL COMMENT '组织编码',
    `sort_order`  INT          NOT NULL DEFAULT 0  COMMENT '排序',
    `status`      VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_org_code` (`org_code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织表';

-- 2. 门店表
DROP TABLE IF EXISTS `sys_store`;
CREATE TABLE `sys_store` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '门店ID',
    `org_id`           BIGINT       NULL     DEFAULT NULL COMMENT '关联组织节点ID',
    `store_name`       VARCHAR(200) NOT NULL COMMENT '门店名称',
    `store_code`       VARCHAR(50)  NOT NULL COMMENT '门店编码',
    `region_id`        BIGINT       NULL     DEFAULT NULL COMMENT '所属区域ID',
    `address`          VARCHAR(500) NULL     DEFAULT NULL COMMENT '详细地址',
    `store_manager_id` BIGINT       NULL     DEFAULT NULL COMMENT '店长ID',
    `opening_date`     DATE         NULL     DEFAULT NULL COMMENT '开店日期',
    `store_type`       VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT 'NEW/OLD/FLAGSHIP/NORMAL',
    `status`           VARCHAR(20)  NOT NULL DEFAULT 'OPEN'   COMMENT 'OPEN/SUSPENDED/CLOSED',
    `business_hours`   VARCHAR(100) NULL     DEFAULT NULL COMMENT '营业时间',
    `contact_phone`    VARCHAR(20)  NULL     DEFAULT NULL COMMENT '联系电话',
    `is_deleted`       TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_store_code` (`store_code`),
    INDEX `idx_region_id` (`region_id`),
    INDEX `idx_store_manager_id` (`store_manager_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';

-- 3. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt密码哈希',
    `real_name`     VARCHAR(50)  NOT NULL COMMENT '姓名',
    `phone`         VARCHAR(20)  NULL     DEFAULT NULL COMMENT '手机号',
    `avatar`        VARCHAR(500) NULL     DEFAULT NULL COMMENT '头像URL',
    `store_id`      BIGINT       NULL     DEFAULT NULL COMMENT '所属门店ID',
    `region_id`     BIGINT       NULL     DEFAULT NULL COMMENT '所属区域ID',
    `position`      VARCHAR(50)  NULL     DEFAULT NULL COMMENT '岗位',
    `entry_date`    DATE         NULL     DEFAULT NULL COMMENT '入职日期',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RESIGNED/DISABLED',
    `last_login_at` DATETIME     NULL     DEFAULT NULL COMMENT '最近登录时间',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0=否 1=是',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_store_id` (`store_id`),
    INDEX `idx_region_id` (`region_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 4. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code`  VARCHAR(50)  NOT NULL COMMENT '角色编码 ROLE_ADMIN/ROLE_HQ/...',
    `role_name`  VARCHAR(100) NOT NULL COMMENT '角色名称',
    `data_scope` VARCHAR(20)  NOT NULL COMMENT 'ALL/REGION/STORE/SELF/CUSTOM',
    `status`     VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `remark`     VARCHAR(500) NULL     DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 5. 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `parent_id`  BIGINT       NOT NULL DEFAULT 0  COMMENT '上级权限ID(树形)',
    `perm_name`  VARCHAR(100) NOT NULL COMMENT '权限名称',
    `perm_type`  VARCHAR(20)  NOT NULL COMMENT 'MENU/BUTTON/API',
    `perm_code`  VARCHAR(100) NOT NULL COMMENT '权限标识 user:create等',
    `path`       VARCHAR(200) NULL     DEFAULT NULL COMMENT '路由路径(菜单类型)',
    `component`  VARCHAR(200) NULL     DEFAULT NULL COMMENT '组件路径',
    `icon`       VARCHAR(50)  NULL     DEFAULT NULL COMMENT '图标',
    `sort_order` INT          NOT NULL DEFAULT 0  COMMENT '排序',
    `status`     VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_perm_code` (`perm_code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 6. 用户-角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- 7. 角色-权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `role_id`       BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- 8. 角色数据权限扩展表
DROP TABLE IF EXISTS `sys_role_data_scope`;
CREATE TABLE `sys_role_data_scope` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     BIGINT      NOT NULL COMMENT '角色ID',
    `scope_type`  VARCHAR(20) NOT NULL COMMENT 'REGION/STORE/BRAND',
    `scope_value` BIGINT      NOT NULL COMMENT '对应的区域/门店/品牌ID',
    PRIMARY KEY (`id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色数据权限扩展表';
