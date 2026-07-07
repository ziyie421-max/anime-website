package com.anime.website.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 数据库初始化器 - 在应用启动时检查并初始化数据库
 * 仅在开发环境使用，生产环境请手动执行SQL脚本
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.db.auto-init", havingValue = "true", matchIfMissing = true)
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查数据库初始化状态...");
        
        try {
            // 检查是否已有数据
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'anime_website' AND table_name = 'anime'", 
                Integer.class
            );
            
            if (count != null && count > 0) {
                // 检查是否有测试数据
                Integer animeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM anime", Integer.class);
                if (animeCount != null && animeCount > 0) {
                    log.info("数据库已初始化，包含 {} 条动漫数据", animeCount);
                    return;
                }
                
                // 表存在但没有数据，只插入测试数据
                log.info("数据库表已存在，开始插入测试数据...");
                executeTestDataScript();
            } else {
                // 表不存在，执行完整初始化
                log.info("数据库表不存在，开始执行完整初始化...");
                executeInitScript();
                executeTestDataScript();
            }
            
            log.info("数据库初始化完成！");
            
        } catch (Exception e) {
            log.warn("数据库初始化过程中出现异常（这在首次运行时是正常的）: {}", e.getMessage());
            try {
                // 尝试执行初始化脚本
                executeInitScript();
                executeTestDataScript();
                log.info("数据库初始化成功完成！");
            } catch (Exception initException) {
                log.error("数据库初始化失败: {}", initException.getMessage());
                // 不抛出异常，让应用继续启动
            }
        }
    }

    /**
     * 执行数据库初始化脚本
     */
    private void executeInitScript() throws Exception {
        log.info("执行数据库初始化脚本...");
        ClassPathResource resource = new ClassPathResource("db/init.sql");
        String sql = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        
        // 分割SQL语句并执行
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try {
                    jdbcTemplate.execute(trimmed);
                } catch (Exception e) {
                    log.debug("执行SQL语句时出现异常（可能是正常的）: {} - SQL: {}", e.getMessage(), trimmed);
                }
            }
        }
        log.info("数据库初始化脚本执行完成");
    }

    /**
     * 执行测试数据插入脚本
     */
    private void executeTestDataScript() throws Exception {
        log.info("执行测试数据插入脚本...");
        ClassPathResource resource = new ClassPathResource("db/test-data.sql");
        String sql = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        
        // 分割SQL语句并执行
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try {
                    jdbcTemplate.execute(trimmed);
                } catch (Exception e) {
                    log.debug("执行测试数据SQL时出现异常（可能是重复数据）: {} - SQL: {}", e.getMessage(), trimmed.substring(0, Math.min(50, trimmed.length())));
                }
            }
        }
        log.info("测试数据插入完成");
    }


}
