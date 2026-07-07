package com.anime.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 动漫网站主应用类 - Spring Boot应用入口
 */
@SpringBootApplication
@EnableJpaAuditing // 启用JPA审计功能，自动填充创建时间和更新时间
public class AnimeWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeWebsiteApplication.class, args);
    }
}
