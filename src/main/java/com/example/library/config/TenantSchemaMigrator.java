package com.example.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 将已有单租户库升级为多租户结构（补 tenant 表、book/app_users.tenant_id、唯一索引）。
 * 新库若已由 {@code schema.sql} 建好完整表结构则此处多为空操作。
 */
@Component
@Order(0)
public class TenantSchemaMigrator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantSchemaMigrator.class);

    private final JdbcTemplate jdbcTemplate;

    public TenantSchemaMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS tenant (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    code VARCHAR(64) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    enabled TINYINT(1) NOT NULL DEFAULT 1,
                    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_tenant_code (code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        Long tenantCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tenant", Long.class);
        if (tenantCount != null && tenantCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO tenant (code, name, enabled) VALUES (?, ?, ?)",
                    "default",
                    "默认租户",
                    1);
            log.info("已插入默认租户 code=default");
        }

        Long defaultTenantId = jdbcTemplate.queryForObject(
                "SELECT id FROM tenant WHERE code = 'default' LIMIT 1", Long.class);
        if (defaultTenantId == null) {
            log.warn("未找到 code=default 的租户，跳过后续回填");
            return;
        }

        if (tableExists("book") && !columnExists("book", "tenant_id")) {
            jdbcTemplate.execute("ALTER TABLE book ADD COLUMN tenant_id BIGINT NULL");
            jdbcTemplate.update("UPDATE book SET tenant_id = ? WHERE tenant_id IS NULL", defaultTenantId);
            jdbcTemplate.execute("ALTER TABLE book MODIFY COLUMN tenant_id BIGINT NOT NULL");
            jdbcTemplate.execute("ALTER TABLE book ADD KEY idx_book_tenant (tenant_id)");
            log.info("已为 book 表增加 tenant_id 并回填");
        }

        if (tableExists("app_users") && !columnExists("app_users", "tenant_id")) {
            jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN tenant_id BIGINT NULL");
            jdbcTemplate.update("UPDATE app_users SET tenant_id = ? WHERE tenant_id IS NULL", defaultTenantId);
            try {
                jdbcTemplate.execute("ALTER TABLE app_users DROP INDEX uk_app_users_username");
            } catch (Exception e) {
                log.debug("删除旧 username 唯一索引（可能不存在）: {}", e.getMessage());
            }
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE app_users ADD UNIQUE KEY uk_app_users_tenant_username (tenant_id, username)");
            } catch (Exception e) {
                log.debug("增加组合唯一索引（可能已存在）: {}", e.getMessage());
            }
            jdbcTemplate.execute("ALTER TABLE app_users MODIFY COLUMN tenant_id BIGINT NOT NULL");
            try {
                jdbcTemplate.execute("ALTER TABLE app_users ADD KEY idx_app_users_tenant (tenant_id)");
            } catch (Exception e) {
                log.debug("增加 tenant 索引（可能已存在）: {}", e.getMessage());
            }
            log.info("已为 app_users 表增加 tenant_id 并调整唯一约束");
        }

        if (tableExists("app_users") && !columnExists("app_users", "platform_admin")) {
            jdbcTemplate.execute(
                    "ALTER TABLE app_users ADD COLUMN platform_admin TINYINT(1) NOT NULL DEFAULT 0");
            jdbcTemplate.update(
                    """
                    UPDATE app_users u
                    INNER JOIN tenant t ON u.tenant_id = t.id
                    SET u.platform_admin = 1
                    WHERE t.code = 'default' AND u.username = 'admin'
                    """);
            log.info("已为 app_users 增加 platform_admin，并为默认租户 admin 设为平台管理员");
        }

        if (tableExists("app_users")) {
            int n = jdbcTemplate.update(
                    "UPDATE app_users SET role = 'ROLE_TENANT_ADMIN' WHERE role = 'ROLE_ADMIN'");
            if (n > 0) {
                log.info("已将 {} 条 app_users.role 从 ROLE_ADMIN 迁移为 ROLE_TENANT_ADMIN", n);
            }
        }
    }

    private boolean tableExists(String tableName) {
        Integer n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name = ?
                """,
                Integer.class,
                tableName);
        return n != null && n > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName);
        return n != null && n > 0;
    }
}
