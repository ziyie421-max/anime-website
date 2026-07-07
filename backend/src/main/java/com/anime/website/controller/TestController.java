package com.anime.website.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证后端API是否正常工作
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "动漫网站后端API正常运行");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 版本信息接口
     */
    @GetMapping("/version")
    public Map<String, Object> version() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "anime-website");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("framework", "Spring Boot 2.7.18");
        response.put("java", System.getProperty("java.version"));
        return response;
    }
}
