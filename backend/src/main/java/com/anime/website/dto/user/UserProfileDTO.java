package com.anime.website.dto.user;

import com.anime.website.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户资料DTO - 用于返回用户信息（排除敏感数据）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private String role;
    private String status;
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
    private LocalDateTime createdAt;
    
    // 统计信息
    private Long favoriteCount;
    private Long historyCount;
    
    /**
     * 从User实体转换
     */
    public static UserProfileDTO fromEntity(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .loginCount(user.getLoginCount())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

