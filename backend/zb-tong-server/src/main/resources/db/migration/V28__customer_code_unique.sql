-- V28__customer_code_unique.sql — 客户编号唯一索引
-- 先清理重复数据（保留最早创建的）
DELETE c1 FROM customer c1
INNER JOIN customer c2
WHERE c1.id > c2.id AND c1.code = c2.code AND c1.code IS NOT NULL AND c1.code != '';
ALTER TABLE `customer` ADD UNIQUE INDEX `uk_code` (`code`);
