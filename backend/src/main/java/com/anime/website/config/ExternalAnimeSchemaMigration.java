package com.anime.website.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 将早期只支持站内动漫的表结构升级为同时支持外部动漫。
 * 此迁移在生产环境也执行，且只会放宽旧字段的 NOT NULL 约束。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ExternalAnimeSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        makeColumnNullable("user_favorites", "anime_id");
        makeColumnNullable("user_watch_history", "anime_id");
        makeColumnNullable("user_watch_history", "episode_id");
    }

    private void makeColumnNullable(String tableName, String columnName) {
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );

        if (columnCount == null || columnCount == 0) {
            return;
        }

        String isNullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                String.class,
                tableName,
                columnName
        );

        if ("NO".equalsIgnoreCase(isNullable)) {
            String sql = "ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + columnName + "` BIGINT NULL";
            jdbcTemplate.execute(sql);
            log.info("数据库兼容迁移完成：{}.{} 已允许为空", tableName, columnName);
        }
    }
}
