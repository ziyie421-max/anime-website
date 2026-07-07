package com.anime.website.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 - 给所有 @RestController 统一添加 /api 路径前缀
 *
 * 生产环境去掉了 application.yml 中的 server.servlet.context-path=/api
 * （否则 context-path 会让整个应用挂在 /api 下，前端 SPA 无法放在根路径 /）。
 * 改用这里的前缀方式：只对 controller 生效，静态资源（前端 SPA）仍由根路径提供，
 * 这样 SPA 在 / ，API 在 /api/** ，单域名同源，无跨域。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 所有标注了 @RestController 的控制器，其映射路径前统一加 /api
        configurer.addPathPrefix("/api",
                HandlerTypePredicate.forAnnotation(
                        org.springframework.web.bind.annotation.RestController.class));
    }
}
