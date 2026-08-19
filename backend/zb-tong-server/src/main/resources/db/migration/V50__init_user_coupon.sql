-- ============================================================
-- V50: C端用户优惠券（营销三端打通：管理端配置 → 用户领取 → 下单抵扣）
-- ============================================================

CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       bigint       NOT NULL COMMENT '用户ID',
  `promotion_id`  bigint       DEFAULT NULL COMMENT '来源促销ID(promotion.id)',
  `name`          varchar(100) DEFAULT NULL COMMENT '优惠券名称',
  `type`          varchar(30)  DEFAULT NULL COMMENT '优惠类型: discount|full_reduction|gift|member_price',
  `discount_method` varchar(200) DEFAULT NULL COMMENT '优惠说明文案',
  `threshold`     decimal(10,2) DEFAULT NULL COMMENT '使用门槛金额(满X可用, 0=无门槛)',
  `discount_value` decimal(10,2) DEFAULT NULL COMMENT '优惠值: 满减=减免金额, 折扣=折扣率(如8.5表示85折)',
  `status`        varchar(20)  NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED|USED|EXPIRED|DISABLED',
  `used_order_id` bigint       DEFAULT NULL COMMENT '使用订单ID',
  `used_at`       datetime     DEFAULT NULL COMMENT '使用时间',
  `received_at`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time`   datetime     DEFAULT NULL COMMENT '过期时间',
  `is_deleted`    tinyint(1)   NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at`    datetime     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_coupon_user` (`user_id`, `status`),
  KEY `idx_user_coupon_promo` (`promotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='C端用户领取的优惠券';
