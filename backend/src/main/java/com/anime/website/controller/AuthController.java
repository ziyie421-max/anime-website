package com.anime.website.controller;

import com.anime.website.dto.auth.AuthResponse;
import com.anime.website.dto.auth.LoginRequest;
import com.anime.website.dto.auth.RegisterRequest;
import com.anime.website.dto.auth.SendCodeRequest;
import com.anime.website.dto.auth.VerifyCodeRequest;
import com.anime.website.entity.EmailVerificationCode;
import com.anime.website.entity.User;
import com.anime.website.service.AuthService;
import com.anime.website.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 处理用户认证相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: {}", request.getUsernameOrEmail());
        
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "登录失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: {}", request.getUsername());
        
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "注册失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
        log.info("收到发送验证码请求: {} - {}", request.getEmail(), request.getType());

        try {
            String ipAddress = getClientIpAddress(httpRequest);

            switch (request.getType().toLowerCase()) {
                case "register":
                    emailService.sendRegisterVerificationCode(request.getEmail(), ipAddress);
                    break;
                case "reset-password":
                    emailService.sendResetPasswordVerificationCode(request.getEmail(), ipAddress);
                    break;
                default:
                    throw new RuntimeException("不支持的验证码类型");
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "验证码已发送到您的邮箱");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "发送验证码失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 验证邮箱验证码
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        log.info("收到验证码验证请求: {} - {}", request.getEmail(), request.getType());

        try {
            EmailVerificationCode.CodeType codeType;
            switch (request.getType().toLowerCase()) {
                case "register":
                    codeType = EmailVerificationCode.CodeType.REGISTER;
                    break;
                case "reset-password":
                    codeType = EmailVerificationCode.CodeType.RESET_PASSWORD;
                    break;
                default:
                    throw new RuntimeException("不支持的验证码类型");
            }

            boolean isValid = emailService.verifyCode(request.getEmail(), request.getCode(), codeType);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "验证码验证成功" : "验证码无效或已过期");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("验证码验证失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "验证码验证失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.info("收到刷新令牌请求");

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "刷新令牌失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        log.info("检查用户名可用性: {}", username);
        
        boolean available = authService.isUsernameAvailable(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        log.info("检查邮箱可用性: {}", email);
        
        boolean available = authService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 验证当前用户令牌（用于前端检查登录状态）
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.info("获取当前用户信息");
        
        try {
            // 提取令牌
            String token = authHeader.replace("Bearer ", "");
            
            // 验证令牌并获取用户信息
            User user = authService.validateAccessToken(token);
            
            // 返回用户信息（不包含敏感信息）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("bio", user.getBio());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("emailVerified", user.getEmailVerified());
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "未授权");
            error.put("message", e.getMessage());
            return ResponseEntity.status(401).body(error);
        }
    }

    /**
     * 用户登出（可选实现，主要在前端清除令牌）
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("用户登出");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
