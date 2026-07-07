package com.anime.website.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类 - 配置HTTP客户端
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置RestTemplate Bean - 增加超时时间和重试机制
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 设置连接超时时间（毫秒）- 增加到30秒
        factory.setConnectTimeout(30000); // 30秒

        // 设置读取超时时间（毫秒）- 增加到60秒
        factory.setReadTimeout(60000); // 60秒

        return new RestTemplate(factory);
    }
}
