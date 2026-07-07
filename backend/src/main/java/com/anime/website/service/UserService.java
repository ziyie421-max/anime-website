package com.anime.website.service;

import com.anime.website.dto.user.ChangePasswordRequest;
import com.anime.website.dto.user.UpdateProfileRequest;
import com.anime.website.dto.user.UserProfileDTO;
import com.anime.website.entity.User;
import com.anime.website.repository.UserFavoriteRepository;
import com.anime.website.repository.UserRepository;
import com.anime.website.repository.UserWatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务 - 处理用户个人中心相关业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final UserWatchHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户资料（包含统计信息）
     */
    public UserProfileDTO getUserProfile(Long userId) {
        log.info("获取用户资料 - 用户ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserProfileDTO profile = UserProfileDTO.fromEntity(user);
        
        // 添加统计信息
        profile.setFavoriteCount(favoriteRepository.countByUserId(userId));
        profile.setHistoryCount(historyRepository.countByUserId(userId));
        
        return profile;
    }

    /**
     * 根据用户名获取用户资料
     */
    public UserProfileDTO getUserProfileByUsername(String username) {
        log.info("根据用户名获取用户资料: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return getUserProfile(user.getId());
    }

    /**
     * 更新用户资料
     */
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("更新用户资料 - 用户ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新昵称
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        
        // 更新头像
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        // 更新个人简介
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        
        userRepository.save(user);
        log.info("用户资料更新成功 - 用户ID: {}", userId);
        
        return getUserProfile(userId);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("修改密码 - 用户ID: {}", userId);
        
        // 验证新密码一致性
        if (!request.isPasswordMatching()) {
            throw new RuntimeException("两次输入的新密码不一致");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证当前密码
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 检查新密码是否与当前密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("密码修改成功 - 用户ID: {}", userId);
    }

    /**
     * 获取用户统计信息
     */
    public UserStatsDTO getUserStats(Long userId) {
        log.info("获取用户统计信息 - 用户ID: {}", userId);
        
        return UserStatsDTO.builder()
                .favoriteCount(favoriteRepository.countByUserId(userId))
                .historyCount(historyRepository.countByUserId(userId))
                .build();
    }

    /**
     * 用户统计信息DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatsDTO {
        private Long favoriteCount;
        private Long historyCount;
    }
}

