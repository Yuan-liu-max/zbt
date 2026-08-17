package com.zhubao.manage.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.infrastructure.mybatis.DataScopePlugin;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * MyBatis-Plus 配置 —— 分页插件 + 逻辑删除 + 数据权限插件
 */
@Configuration
@MapperScan("com.zhubao.manage.module.**.mapper")
public class MyBatisPlusConfig {

    private static final Logger log = LoggerFactory.getLogger(MyBatisPlusConfig.class);
    private final DataSource dataSource;

    public MyBatisPlusConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** 启动时自动执行顾客支持迁移 */
    @PostConstruct
    public void runMigration() {
        try (Connection conn = dataSource.getConnection()) {
            java.sql.Statement stmt = conn.createStatement();

            // 清理失败的 Flyway V30 记录
            try { stmt.execute("DELETE FROM flyway_schema_history WHERE version = '30'"); } catch (Exception ignored) {}

            // sales_order.user_id
            if (!colExists(conn, "sales_order", "user_id")) {
                stmt.execute("ALTER TABLE sales_order ADD COLUMN user_id BIGINT NULL AFTER id");
                stmt.execute("ALTER TABLE sales_order ADD INDEX idx_user_id (user_id)");
                log.info("[Migration] sales_order.user_id added");
            }

            // order_return.order_id
            if (!colExists(conn, "order_return", "order_id")) {
                stmt.execute("ALTER TABLE order_return ADD COLUMN order_id BIGINT NULL AFTER id");
                stmt.execute("ALTER TABLE order_return ADD INDEX idx_return_order_id (order_id)");
                log.info("[Migration] order_return.order_id added");
            }

            // order_return.refund_amount
            if (!colExists(conn, "order_return", "refund_amount")) {
                stmt.execute("ALTER TABLE order_return ADD COLUMN refund_amount DECIMAL(12,2) NULL AFTER order_amount");
                log.info("[Migration] order_return.refund_amount added");
            }

            // ROLE_CUSTOMER
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_role WHERE role_code = 'ROLE_CUSTOMER'");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO sys_role (id, role_code, role_name, remark, data_scope, status, created_at) " +
                        "VALUES (6, 'ROLE_CUSTOMER', '顾客', '商城注册用户', 'NONE', 'ENABLED', NOW())");
                log.info("[Migration] ROLE_CUSTOMER created");
            }
            rs.close();

            // ai_chat_history（AI智能问答历史）
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS `ai_chat_history` (" +
                        "`id` BIGINT NOT NULL AUTO_INCREMENT, " +
                        "`question` TEXT NULL COMMENT '用户问题', " +
                        "`answer` TEXT NULL COMMENT 'AI回答', " +
                        "`model_name` VARCHAR(64) NULL COMMENT '模型名', " +
                        "`is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除', " +
                        "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                        "PRIMARY KEY (`id`), INDEX `idx_created_at` (`created_at`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI智能问答历史'");
                log.info("[Migration] ai_chat_history ensured");
            } catch (Exception ignored) {}

            // ai_result 表 JSON 列改 TEXT（AI 输出可能是空串/纯文本，JSON 列会拒绝非法 JSON）
            try {
                if ("json".equalsIgnoreCase(colType(conn, "ai_result", "output_json"))) {
                    stmt.execute("ALTER TABLE `ai_result` MODIFY COLUMN `output_json` TEXT NULL DEFAULT NULL COMMENT '结构化结果'");
                    log.info("[Migration] ai_result.output_json -> text");
                }
                if ("json".equalsIgnoreCase(colType(conn, "ai_result", "input_snapshot"))) {
                    stmt.execute("ALTER TABLE `ai_result` MODIFY COLUMN `input_snapshot` TEXT NULL DEFAULT NULL COMMENT '输入快照'");
                    log.info("[Migration] ai_result.input_snapshot -> text");
                }
                if ("json".equalsIgnoreCase(colType(conn, "ai_result", "token_usage"))) {
                    stmt.execute("ALTER TABLE `ai_result` MODIFY COLUMN `token_usage` TEXT NULL DEFAULT NULL COMMENT 'Token消耗'");
                    log.info("[Migration] ai_result.token_usage -> text");
                }
            } catch (Exception ignored) {}

            // sys_message（站内信/私信）
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS `sys_message` (" +
                        "`id` BIGINT NOT NULL AUTO_INCREMENT, " +
                        "`sender_id` BIGINT NULL DEFAULT NULL COMMENT '发送人ID(系统消息为空)', " +
                        "`receiver_id` BIGINT NOT NULL COMMENT '接收人ID', " +
                        "`title` VARCHAR(200) NOT NULL COMMENT '标题', " +
                        "`content` TEXT NULL DEFAULT NULL COMMENT '内容', " +
                        "`is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读 0=否 1=是', " +
                        "`read_at` DATETIME NULL DEFAULT NULL COMMENT '阅读时间', " +
                        "`is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除', " +
                        "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                        "PRIMARY KEY (`id`), INDEX `idx_receiver_id` (`receiver_id`), INDEX `idx_is_read` (`is_read`), INDEX `idx_created_at` (`created_at`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信/私信表'");
                log.info("[Migration] sys_message ensured");
            } catch (Exception ignored) {}

            // prompt_template 默认文档模板（文档生成用）
            try {
                ResultSet rsTpl = stmt.executeQuery("SELECT COUNT(*) FROM prompt_template WHERE is_deleted = 0");
                rsTpl.next();
                if (rsTpl.getInt(1) == 0) {
                    stmt.execute("INSERT INTO `prompt_template` (`template_name`, `business_type`, `prompt_content`, `model_name`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES " +
                            "('员工分析报告', 'EMPLOYEE', '请生成一份《员工分析报告》，要求包含以下章节：一、员工基本情况；二、优势与亮点；三、待提升领域；四、改进与发展建议。语言专业、条理清晰。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()), " +
                            "('货品运营分析报告', 'PRODUCT', '请生成一份《货品运营分析报告》，要求包含以下章节：一、商品结构分析；二、动销与库存情况；三、问题诊断；四、运营改进建议。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()), " +
                            "('门店场景巡检报告', 'SCENE', '请生成一份《门店场景巡检报告》，要求包含以下章节：一、巡检概况；二、发现问题；三、整改措施；四、后续跟进计划。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()), " +
                            "('任务复盘报告', 'TASK', '请生成一份《任务复盘报告》，要求包含以下章节：一、任务背景与目标；二、执行情况；三、问题与不足；四、改进计划。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW())");
                    log.info("[Migration] prompt_template seeded");
                }
                rsTpl.close();
            } catch (Exception ignored) {}

            stmt.close();
            log.info("[Migration] All customer-support migrations completed");
        } catch (Exception e) {
            log.warn("[Migration] Failed (may already be applied): {}", e.getMessage());
        }
    }

    private boolean colExists(Connection conn, String table, String col) throws Exception {
        ResultSet rs = conn.getMetaData().getColumns(null, null, table.toLowerCase(), col.toLowerCase());
        boolean exists = rs.next();
        rs.close();
        if (!exists) {
            rs = conn.getMetaData().getColumns(null, null, table.toUpperCase(), col.toUpperCase());
            exists = rs.next();
            rs.close();
        }
        return exists;
    }

    private String colType(Connection conn, String table, String col) throws Exception {
        ResultSet rs = conn.getMetaData().getColumns(null, null, table.toLowerCase(), col.toLowerCase());
        String type = null;
        if (rs.next()) type = rs.getString("TYPE_NAME");
        rs.close();
        if (type == null) {
            rs = conn.getMetaData().getColumns(null, null, table.toUpperCase(), col.toUpperCase());
            if (rs.next()) type = rs.getString("TYPE_NAME");
            rs.close();
        }
        return type;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(100L);
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

    @Bean
    public DataScopePlugin dataScopePlugin(UserContextHolder uch) {
        return new DataScopePlugin(uch);
    }
}
