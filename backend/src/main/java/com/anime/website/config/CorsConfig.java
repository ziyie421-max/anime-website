package com.anime.website.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 跨域配置类 - 配置CORS以允许前端访问后端API
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 设置允许的源 - 使用 OriginPatterns 支持局域网任意 IP 访问
        // 注意：allowCredentials=true 时不能用 setAllowedOrigins("*")，必须用 setAllowedOriginPatterns
        // 这样不管局域网 IP 怎么变（DHCP），同网段设备都能正常访问
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 设置允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // 设置允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 设置是否允许携带凭证
        configuration.setAllowCredentials(true);

        // 设置预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
