package com.anime.website.service;

import com.anime.website.dto.auth.AuthResponse;
import com.anime.website.dto.auth.LoginRequest;
import com.anime.website.dto.auth.RegisterRequest;
import com.anime.website.entity.EmailVerificationCode;
import com.anime.website.entity.User;
import com.anime.website.repository.UserRepository;
import com.anime.website.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务 - 处理用户登录、注册等认证相关业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    /**
     * 用户登录
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("用户尝试登录: {}", request.getUsernameOrEmail());

        // 查找用户
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail());
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户名或密码错误");
        }

        User user = userOptional.get();

        // 检查用户状态
        if (!user.isActive()) {
            throw new RuntimeException("账户已被禁用，请联系管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        // 生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        Long expiresIn = 86400L; // 24小时，从配置文件读取

        log.info("用户登录成功: {} (ID: {})", user.getUsername(), user.getId());
        return AuthResponse.success(accessToken, refreshToken, expiresIn, user);
    }

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("用户尝试注册: {}", request.getUsername());

        // 验证密码一致性
        if (!request.isPasswordMatching()) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 验证邮箱验证码
        boolean isCodeValid = emailService.verifyCode(
            request.getEmail(),
            request.getEmailCode(),
            EmailVerificationCode.CodeType.REGISTER
        );
        if (!isCodeValid) {
            throw new RuntimeException("邮箱验证码无效或已过期");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(User.UserRole.USER);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(true); // 通过邮箱验证码注册，直接设为已验证
        user.setLoginCount(0);

        // 保存用户
        user = userRepository.save(user);

        // 生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        Long expiresIn = 86400L; // 24小时，从配置文件读取

        log.info("用户注册成功: {} (ID: {})", user.getUsername(), user.getId());
        return AuthResponse.success(accessToken, refreshToken, expiresIn, user);
    }

    /**
     * 刷新访问令牌
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.info("尝试刷新访问令牌");

        try {
            String username = jwtUtil.getUsernameFromToken(refreshToken);

            // 验证令牌有效性
            if (!jwtUtil.validateToken(refreshToken, username)) {
                throw new RuntimeException("刷新令牌已过期");
            }

            // 查找用户
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (!userOptional.isPresent()) {
                throw new RuntimeException("用户不存在");
            }

            User user = userOptional.get();

            // 检查用户状态
            if (!user.isActive()) {
                throw new RuntimeException("账户已被禁用");
            }

            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole().name());
            Long expiresIn = 86400L; // 24小时

            log.info("访问令牌刷新成功: {}", username);
            return AuthResponse.success(newAccessToken, refreshToken, expiresIn, user);

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 验证访问令牌
     */
    public User validateAccessToken(String accessToken) {
        try {
            String username = jwtUtil.getUsernameFromToken(accessToken);
            
            if (!jwtUtil.validateToken(accessToken, username)) {
                throw new RuntimeException("访问令牌无效或已过期");
            }

            Optional<User> userOptional = userRepository.findByUsername(username);
            if (!userOptional.isPresent()) {
                throw new RuntimeException("用户不存在");
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                throw new RuntimeException("账户已被禁用");
            }

            return user;

        } catch (Exception e) {
            log.error("验证访问令牌失败: {}", e.getMessage());
            throw new RuntimeException("访问令牌验证失败");
        }
    }

    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
