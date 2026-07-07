package com.anime.website.config;

import com.anime.website.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类 - 配置Spring Security和JWT认证
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // JWT认证过滤器
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置HTTP安全
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（使用JWT不需要CSRF）
            .csrf().disable()
            // 配置CORS
            .cors()
            .and()
            // 配置会话管理为无状态（使用JWT）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求授权
            .authorizeRequests()
                // 公开访问的端点
                .antMatchers("/auth/**").permitAll()           // 认证相关
                .antMatchers("/anime/**").permitAll()          // 动漫浏览
                .antMatchers("/episodes/**").permitAll()       // 剧集浏览
                .antMatchers("/categories/**").permitAll()     // 分类浏览
                .antMatchers("/external/**").permitAll()       // 外部资源
                .antMatchers("/test/**").permitAll()           // 测试端点
                // 公开的评论和评分接口（GET请求）
                .antMatchers("/comments/anime/**").permitAll()       // 获取评论列表
                .antMatchers("/ratings/stats/**").permitAll()        // 获取评分统计
                // 需要认证的端点
                .antMatchers("/user/**").authenticated()       // 用户个人中心
                .antMatchers("/favorites/**").authenticated()  // 收藏功能
                .antMatchers("/history/**").authenticated()    // 观看历史
                .antMatchers("/notifications/**").authenticated()    // 通知功能
                // 其他请求允许访问（开发阶段）
                .anyRequest().permitAll()
            .and()
            // 禁用默认登录页面
            .formLogin().disable()
            // 禁用HTTP Basic认证
            .httpBasic().disable();

        // 添加JWT认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
