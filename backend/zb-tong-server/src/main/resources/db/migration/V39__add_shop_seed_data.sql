-- ============================================================
-- V39__add_shop_seed_data.sql
-- 种子数据补充：5个产品 + 测试地址 + 促销活动
-- ============================================================

-- 1. 新增 5 个测试产品（1016-1020），凑满 20 个
INSERT IGNORE INTO `product` (`id`, `product_code`, `product_name`, `category`, `retail_price`, `cost_price`, `style`, `material`, `weight`, `size`, `status`, `stock`, `store_id`, `meaning`, `image_url`) VALUES
(1016, 'P-GEM-001', '天然红宝石戒指', '宝石', 35000.00, 28000.00, '周大生', '红宝石', '2.15ct', '13#', 'ON_SALE', 6, 1, '天然鸽血红宝石，热情似火', '/images/products/gem-ruby-ring.jpg'),
(1017, 'P-GEM-002', '蓝宝石吊坠项链', '宝石', 18800.00, 14500.00, '谢瑞麟', '蓝宝石', '1.85ct', '42cm', 'ON_SALE', 10, 1, '皇家蓝宝石，高贵典雅', '/images/products/gem-sapphire-pendant.jpg'),
(1018, 'P-GOLD-004', '5G黄金套链', '黄金', 8800.00, 6500.00, '中国黄金', '足金999', '12.5g', '45cm', 'ON_SALE', 22, 1, '5G工艺，硬度更高更闪亮', '/images/products/gold-5g-necklace.jpg'),
(1019, 'P-DIAMOND-003', '公主方钻戒指', '钻石', 42000.00, 32000.00, '周生生', '18K白金', '0.80ct', '12#', 'ON_SALE', 8, 1, '公主方切割，简约现代', '/images/products/diamond-princess-ring.jpg'),
(1020, 'P-SILVER-003', '泰银手工戒指', '银饰', 880.00, 580.00, '银时代', '泰银', '8.5g', '自定义', 'ON_SALE', 35, 1, '泰银复古做旧工艺', '/images/products/silver-thai-ring.jpg');

-- 2. 测试用户的收货地址
INSERT IGNORE INTO `user_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `is_default`) VALUES
(1, 100, '张顾客', '13800000001', '广东省', '深圳市', '罗湖区', '水贝珠宝城A栋1001室', 1),
(2, 100, '张顾客', '13800000001', '广东省', '广州市', '天河区', '体育西路100号', 0),
(3, 101, '李顾客', '13800000002', '北京市', '北京市', '朝阳区', '建国路88号院1号楼1501', 1);

-- 3. 促销活动数据（status=ongoing 表示进行中）
INSERT IGNORE INTO `promotion` (`id`, `name`, `type`, `discount_method`, `start_time`, `end_time`, `status`, `scope`, `usage_count`) VALUES
(1, '七夕珠宝节', 'discount', '满1000减100', '2025-08-01 00:00:00', '2026-12-31 23:59:59', 'ongoing', 'ALL', 0),
(2, '会员专享95折', 'member_price', '钻石品类95折', '2025-01-01 00:00:00', '2026-12-31 23:59:59', 'ongoing', 'DIAMOND', 0),
(3, '新客首单礼', 'discount', '满500减50', '2025-01-01 00:00:00', '2026-12-31 23:59:59', 'ongoing', 'ALL', 0),
(4, '黄金工费5折', 'full_reduction', '黄金工费半价', '2025-06-01 00:00:00', '2026-12-31 23:59:59', 'ongoing', 'GOLD', 0);
