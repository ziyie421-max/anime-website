package com.anime.website.dto.auth;

import com.anime.website.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 认证响应DTO - 登录成功后返回的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // 访问令牌
    private String accessToken;

    // 刷新令牌
    private String refreshToken;

    // 令牌类型
    private String tokenType = "Bearer";

    // 访问令牌过期时间（秒）
    private Long expiresIn;

    // 用户信息
    private UserInfo user;

    /**
     * 用户信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String bio;
        private User.UserRole role;
        private User.UserStatus status;
        private Boolean emailVerified;

        /**
         * 从User实体创建UserInfo
         */
        public static UserInfo fromUser(User user) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setBio(user.getBio());
            userInfo.setRole(user.getRole());
            userInfo.setStatus(user.getStatus());
            userInfo.setEmailVerified(user.getEmailVerified());
            return userInfo;
        }
    }

    /**
     * 创建成功的认证响应
     */
    public static AuthResponse success(String accessToken, String refreshToken, Long expiresIn, User user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUser(UserInfo.fromUser(user));
        return response;
    }
}
