package com.zhubao.manage.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 启动时自动执行 V30 数据库迁移（解决 Flyway 校验失败导致启动阻塞的问题）
 */
@Component
public class StartupMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupMigration.class);
    private final DataSource dataSource;

    public StartupMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {

            // 清理失败的 Flyway V30 记录
            try {
                stmt.execute("DELETE FROM flyway_schema_history WHERE version = '30'");
                log.info("已清理 flyway V30 失败记录");
            } catch (Exception ignored) {}

            // 1. sales_order 加 user_id
            if (!columnExists(conn, "sales_order", "user_id")) {
                stmt.execute("ALTER TABLE sales_order ADD COLUMN user_id BIGINT NULL AFTER id");
                stmt.execute("ALTER TABLE sales_order ADD INDEX idx_user_id (user_id)");
                log.info("sales_order.user_id 字段已添加");
            }

            // 2. order_return 加 order_id
            if (!columnExists(conn, "order_return", "order_id")) {
                stmt.execute("ALTER TABLE order_return ADD COLUMN order_id BIGINT NULL AFTER id");
                stmt.execute("ALTER TABLE order_return ADD INDEX idx_return_order_id (order_id)");
                log.info("order_return.order_id 字段已添加");
            }

            // 3. order_return 加 refund_amount
            if (!columnExists(conn, "order_return", "refund_amount")) {
                stmt.execute("ALTER TABLE order_return ADD COLUMN refund_amount DECIMAL(12,2) NULL AFTER order_amount");
                log.info("order_return.refund_amount 字段已添加");
            }

            // 4. 插入 ROLE_CUSTOMER
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_role WHERE role_code = 'ROLE_CUSTOMER'");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO sys_role (id, role_code, role_name, description, data_scope, status, is_deleted, created_at) " +
                        "VALUES (6, 'ROLE_CUSTOMER', '顾客', '商城注册用户，仅可查看和操作自己的订单', 'NONE', 'ENABLED', 0, NOW())");
                log.info("ROLE_CUSTOMER 角色已创建");
            }
            rs.close();

            // 5. 创建 shop_cart 表
            if (!tableExists(conn, "shop_cart")) {
                stmt.execute("CREATE TABLE shop_cart (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id BIGINT NOT NULL, product_id BIGINT NOT NULL, " +
                        "quantity INT DEFAULT 1, checked TINYINT DEFAULT 1, " +
                        "created_at DATETIME DEFAULT NOW(), updated_at DATETIME DEFAULT NOW() ON UPDATE NOW(), " +
                        "UNIQUE KEY uk_user_product (user_id, product_id), " +
                        "INDEX idx_user_id (user_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                log.info("shop_cart 表已创建");
            }

            // 6. 创建 user_address 表
            if (!tableExists(conn, "user_address")) {
                stmt.execute("CREATE TABLE user_address (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id BIGINT NOT NULL, receiver_name VARCHAR(50) NOT NULL, " +
                        "receiver_phone VARCHAR(20) NOT NULL, " +
                        "province VARCHAR(50), city VARCHAR(50), district VARCHAR(50), " +
                        "detail_address VARCHAR(200) NOT NULL, is_default TINYINT DEFAULT 0, " +
                        "created_at DATETIME DEFAULT NOW(), updated_at DATETIME DEFAULT NOW() ON UPDATE NOW(), " +
                        "INDEX idx_user_id (user_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                log.info("user_address 表已创建");
            }

            // 7. 订单增加商城字段
            if (!columnExists(conn, "sales_order", "buyer_id")) {
                stmt.execute("ALTER TABLE sales_order ADD COLUMN buyer_id BIGINT NULL AFTER user_id");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN address_id BIGINT NULL AFTER customer_address");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN address_snapshot TEXT NULL AFTER address_id");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN payment_time DATETIME NULL AFTER payment_method");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN payment_trade_no VARCHAR(64) NULL AFTER payment_time");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN delivery_company VARCHAR(50) NULL AFTER delivery_method");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN delivery_track_no VARCHAR(64) NULL AFTER delivery_company");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN delivery_time DATETIME NULL AFTER delivery_track_no");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN receive_time DATETIME NULL AFTER delivery_time");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN finish_time DATETIME NULL AFTER receive_time");
                stmt.execute("ALTER TABLE sales_order ADD COLUMN order_type VARCHAR(20) DEFAULT 'MANUAL' AFTER finish_time");
                log.info("sales_order 商城字段已添加");
            }

            // 8. 创建 user_favorite 表
            if (!tableExists(conn, "user_favorite")) {
                stmt.execute("CREATE TABLE user_favorite (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id BIGINT NOT NULL, product_id BIGINT NOT NULL, " +
                        "created_at DATETIME DEFAULT NOW(), " +
                        "UNIQUE KEY uk_user_product (user_id, product_id), " +
                        "INDEX idx_user_id (user_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                log.info("user_favorite 表已创建");
            }

            // 9. 插入C端测试用户
            rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_user WHERE username IN ('customer1','customer2')");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO sys_user (username, password_hash, real_name, phone, status, is_deleted, created_at, updated_at) VALUES " +
                        "('customer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '张顾客', '13800000001', 'ENABLED', 0, NOW(), NOW()), " +
                        "('customer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '李顾客', '13800000002', 'ENABLED', 0, NOW(), NOW())");
                // 为测试用户绑定 CUSTOMER 角色
                stmt.execute("INSERT IGNORE INTO sys_user_role (user_id, role_id) " +
                        "SELECT u.id, 6 FROM sys_user u WHERE u.username IN ('customer1','customer2')");
                log.info("C端测试用户已创建");
            }
            rs.close();

            // 10. product 加 image_url 字段
            if (!columnExists(conn, "product", "image_url")) {
                stmt.execute("ALTER TABLE product ADD COLUMN image_url VARCHAR(500) NULL AFTER status");
                log.info("product.image_url 字段已添加");
            }

            // 11. 补充测试商品数据（20条，图片由 product_image 表管理）
            rs = stmt.executeQuery("SELECT COUNT(*) FROM product WHERE id >= 1001");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT IGNORE INTO product (id, product_code, product_name, category, retail_price, cost_price, style, material, weight, size, color, status, stock, store_id, meaning, is_deleted, created_at, updated_at) VALUES " +
                        "(1001, 'P-GOLD-001', '足金花开富贵手镯', '黄金', 12800.00, 9800.00, '周大福', '足金999', '30.5g', '56mm', '金色', 'ON_SALE', 20, 1, '花开富贵，吉祥如意', 0, NOW(), NOW()), " +
                        "(1002, 'P-GOLD-002', '古法黄金传承吊坠', '黄金', 6800.00, 5200.00, '老凤祥', '足金999', '15.2g', '25mm', '金色', 'ON_SALE', 35, 1, '传承经典，古法工艺', 0, NOW(), NOW()), " +
                        "(1003, 'P-DIAMOND-001', '1克拉六爪钻戒', '钻石', 58000.00, 45000.00, '周生生', '铂金950', '1.02ct', '12#', '白色', 'ON_SALE', 8, 1, '经典六爪镶嵌，钻石火彩闪耀', 0, NOW(), NOW()), " +
                        "(1004, 'P-DIAMOND-002', '30分钻石项链', '钻石', 15800.00, 12000.00, '六福珠宝', '18K白金', '0.30ct', '45cm', '白色', 'ON_SALE', 15, 1, '简约时尚，日常佩戴之选', 0, NOW(), NOW()), " +
                        "(1005, 'P-JADE-001', '冰种翡翠手镯', '翡翠', 88000.00, 65000.00, '七彩云南', '冰种翡翠', '52.3g', '58mm', '绿色', 'ON_SALE', 5, 1, '冰种质地，温润通透', 0, NOW(), NOW()), " +
                        "(1006, 'P-JADE-002', '糯种翡翠平安扣', '翡翠', 12800.00, 9800.00, '翡翠王朝', '糯种翡翠', '18.6g', '35mm', '绿色', 'ON_SALE', 12, 1, '平安扣寓意平安健康', 0, NOW(), NOW()), " +
                        "(1007, 'P-PEARL-001', '南洋金珠项链', '珍珠', 25800.00, 19800.00, '珍珠世家', '南洋金珠', '45.2g', '42cm', '金色', 'ON_SALE', 10, 1, '南洋金珠，奢华典雅', 0, NOW(), NOW()), " +
                        "(1008, 'P-PEARL-002', '淡水珍珠耳钉', '珍珠', 1280.00, 880.00, '京润珍珠', '淡水珍珠', '8.5g', '8mm', '白色', 'ON_SALE', 50, 1, '经典珍珠耳钉，百搭时尚', 0, NOW(), NOW()), " +
                        "(1009, 'P-KGOLD-001', '18K金时尚锁骨链', 'K金', 3280.00, 2500.00, '周大生', '18K金', '3.8g', '40cm', '玫瑰金', 'ON_SALE', 25, 1, '时尚锁骨链，展现颈部线条', 0, NOW(), NOW()), " +
                        "(1010, 'P-KGOLD-002', '18K玫瑰金戒指', 'K金', 2580.00, 1900.00, '潮宏基', '18K玫瑰金', '2.6g', '14#', '玫瑰金', 'ON_SALE', 30, 1, '玫瑰金与钻石的浪漫邂逅', 0, NOW(), NOW()), " +
                        "(1011, 'P-PLAT-001', '铂金情侣对戒', '铂金', 9800.00, 7500.00, 'I Do', '铂金950', '5.8g', '13#+16#', '白色', 'ON_SALE', 18, 1, '铂金对戒，见证永恒爱情', 0, NOW(), NOW()), " +
                        "(1012, 'P-PLAT-002', '铂金素圈戒指', '铂金', 4500.00, 3200.00, '钻石小鸟', '铂金950', '3.2g', '自定义', '白色', 'ON_SALE', 40, 1, '简约素圈，低调奢华', 0, NOW(), NOW()), " +
                        "(1013, 'P-SILVER-001', '925银镶锆石手链', '银饰', 680.00, 420.00, '银时代', '925银', '12.5g', '18cm', '银色', 'ON_SALE', 60, 1, '银饰手链，时尚百搭', 0, NOW(), NOW()), " +
                        "(1014, 'P-SILVER-002', '藏银民族风耳环', '银饰', 380.00, 260.00, '七度银饰', '藏银', '6.8g', '15mm', '银色', 'ON_SALE', 45, 1, '民族风格，独具魅力', 0, NOW(), NOW()), " +
                        "(1015, 'P-GOLD-003', '3D硬金生肖转运珠', '黄金', 1880.00, 1400.00, '中国黄金', '足金999', '2.8g', '10mm', '金色', 'ON_SALE', 100, 1, '3D硬金工艺，生肖守护', 0, NOW(), NOW()), " +
                        "(1016, 'P-JADE-003', '和田玉平安牌', '翡翠', 19800.00, 14800.00, '和玉缘', '和田玉', '38.5g', '50x30mm', '白色', 'ON_SALE', 8, 1, '和田美玉，平安是福', 0, NOW(), NOW()), " +
                        "(1017, 'P-GOLD-004', '足金戒指男款', '黄金', 8800.00, 6500.00, '周大福', '足金999', '12.8g', '18#', '金色', 'ON_SALE', 22, 1, '男士足金戒指，大气稳重', 0, NOW(), NOW()), " +
                        "(1018, 'P-DIAMOND-003', '钻石耳钉一对', '钻石', 8800.00, 6500.00, '周大生', '18K白金', '0.15ct*2', '4mm', '白色', 'ON_SALE', 30, 1, '闪耀钻石耳钉，简约精致', 0, NOW(), NOW()), " +
                        "(1019, 'P-PEARL-003', '大溪地黑珍珠吊坠', '珍珠', 16800.00, 12500.00, '珍珠世家', '大溪地黑珍珠', '12.8g', '11mm', '黑色', 'ON_SALE', 7, 1, '大溪地黑珍珠，神秘奢华', 0, NOW(), NOW()), " +
                        "(1020, 'P-KGOLD-003', '18K金编织手镯', 'K金', 5800.00, 4200.00, '老凤祥', '18K金', '18.2g', '58mm', '金色', 'ON_SALE', 15, 1, '精美编织工艺，时尚百搭', 0, NOW(), NOW())");
                log.info("20条测试商品数据已创建");
            }
            rs.close();

            // 11b. 创建 product_image 表（dev 环境 Flyway 禁用，由此处建表）
            if (!tableExists(conn, "product_image")) {
                stmt.execute("CREATE TABLE product_image (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "product_id BIGINT NOT NULL, " +
                        "image_url VARCHAR(500) NOT NULL COMMENT '图片URL', " +
                        "sort_order INT NOT NULL DEFAULT 0 COMMENT '排序', " +
                        "is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主图', " +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "INDEX idx_product_id (product_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表'");
                log.info("product_image 表已创建");
            }

            // 11c. 补全 product_image 种子数据（仅当该表为空时）
            rs = stmt.executeQuery("SELECT COUNT(*) FROM product_image");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO product_image (product_id, image_url, sort_order, is_primary, created_at) VALUES " +
                        "(1001, '/files/static/images/products/product_1001.jpg', 0, 1, NOW()), " +
                        "(1002, '/files/static/images/products/product_1002.jpg', 0, 1, NOW()), " +
                        "(1003, '/files/static/images/products/product_1003.jpg', 0, 1, NOW()), " +
                        "(1004, '/files/static/images/products/product_1004.jpg', 0, 1, NOW()), " +
                        "(1005, '/files/static/images/products/product_1005.jpg', 0, 1, NOW()), " +
                        "(1006, '/files/static/images/products/product_1006.jpg', 0, 1, NOW()), " +
                        "(1007, '/files/static/images/products/product_1007.jpg', 0, 1, NOW()), " +
                        "(1008, '/files/static/images/products/product_1008.jpg', 0, 1, NOW()), " +
                        "(1009, '/files/static/images/products/product_1009.jpg', 0, 1, NOW()), " +
                        "(1010, '/files/static/images/products/product_1010.jpg', 0, 1, NOW()), " +
                        "(1011, '/files/static/images/products/product_1011.jpg', 0, 1, NOW()), " +
                        "(1012, '/files/static/images/products/product_1012.jpg', 0, 1, NOW()), " +
                        "(1013, '/files/static/images/products/product_1013.jpg', 0, 1, NOW()), " +
                        "(1014, '/files/static/images/products/product_1014.jpg', 0, 1, NOW()), " +
                        "(1015, '/files/static/images/products/product_1015.jpg', 0, 1, NOW()), " +
                        "(1016, '/files/static/images/products/product_1016.jpg', 0, 1, NOW()), " +
                        "(1017, '/files/static/images/products/product_1017.jpg', 0, 1, NOW()), " +
                        "(1018, '/files/static/images/products/product_1018.jpg', 0, 1, NOW()), " +
                        "(1019, '/files/static/images/products/product_1019.jpg', 0, 1, NOW()), " +
                        "(1020, '/files/static/images/products/product_1020.jpg', 0, 1, NOW())");
                log.info("20条商品图片种子数据已写入 product_image 表");
                // 同步 product.image_url 为主图路径（兼容旧前端读取）
                stmt.execute("UPDATE product p SET image_url = (" +
                        "SELECT pi.image_url FROM product_image pi " +
                        "WHERE pi.product_id = p.id AND pi.is_primary = 1 LIMIT 1" +
                        ") WHERE id BETWEEN 1001 AND 1020");
            }
            rs.close();

            // 12. 补充促销活动数据
            rs = stmt.executeQuery("SELECT COUNT(*) FROM promotion WHERE id >= 100");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT IGNORE INTO promotion (id, name, type, discount_method, start_time, end_time, status, scope, is_deleted, created_at) VALUES " +
                        "(100, '新用户首单9折', 'discount', '首单享9折优惠', '2025-01-01', '2026-12-31', 'ongoing', '新注册用户', 0, NOW()), " +
                        "(101, '满2000减200', 'full_reduction', '满2000元减200元', '2025-06-01', '2026-12-31', 'ongoing', '全场商品', 0, NOW()), " +
                        "(102, '黄金品类满5000赠银饰', 'gift', '购黄金满5000元赠银饰一件', '2025-06-15', '2026-06-15', 'ongoing', '黄金品类', 0, NOW())");
                log.info("3条促销活动数据已创建");
            }
            rs.close();

            // 13. customer 表增加 user_id 关联 sys_user
            if (!columnExists(conn, "customer", "user_id")) {
                stmt.execute("ALTER TABLE customer ADD COLUMN user_id BIGINT NULL AFTER id");
                stmt.execute("ALTER TABLE customer ADD INDEX idx_customer_user_id (user_id)");
                log.info("customer.user_id 字段已添加");
            }

            // 14. employee_assessment 加 type 字段（考核类型 monthly/quarterly/special）
            if (!columnExists(conn, "employee_assessment", "type")) {
                stmt.execute("ALTER TABLE employee_assessment ADD COLUMN `type` VARCHAR(20) NULL DEFAULT NULL AFTER assessment_week");
                log.info("employee_assessment.type 字段已添加");
            }

            log.info("StartupMigration 执行完成");

        } catch (Exception e) {
            log.error("StartupMigration 执行失败: {}", e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        ResultSet rs = conn.getMetaData().getColumns(null, null, table, column);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
}
