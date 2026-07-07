package com.anime.website.security;

import com.anime.website.entity.User;
import com.anime.website.repository.UserRepository;
import com.anime.website.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * JWT认证过滤器 - 从请求头中提取JWT令牌并验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中提取JWT令牌
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 从令牌中获取用户名
                String username = jwtUtil.getUsernameFromToken(jwt);

                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 验证令牌
                    if (jwtUtil.validateToken(jwt, username)) {
                        // 从数据库加载用户
                        Optional<User> userOptional = userRepository.findByUsername(username);
                        
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            
                            // 检查用户是否活跃
                            if (user.isActive()) {
                                // 创建UserPrincipal
                                UserPrincipal userPrincipal = UserPrincipal.fromUser(user);
                                
                                // 创建认证令牌
                                UsernamePasswordAuthenticationToken authentication = 
                                        new UsernamePasswordAuthenticationToken(
                                                userPrincipal, 
                                                null, 
                                                userPrincipal.getAuthorities());
                                
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                
                                // 设置认证信息到安全上下文
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                
                                log.debug("用户认证成功: {}", username);
                            } else {
                                log.warn("用户账户已被禁用: {}", username);
                            }
                        } else {
                            log.warn("用户不存在: {}", username);
                        }
                    } else {
                        log.warn("JWT令牌验证失败");
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过程中发生错误: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}

