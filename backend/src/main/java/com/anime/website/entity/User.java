package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 存储用户基本信息和认证信息
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore // 防止密码在JSON响应中暴露
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "avatar")
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    // 个人简介
    @Column(name = "bio", length = 200)
    private String bio;

    // 最后登录时间
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 登录次数
    @Column(name = "login_count")
    private Integer loginCount = 0;

    // 邮箱验证状态
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        USER("普通用户"),
        ADMIN("管理员");

        private final String description;

        UserRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("正常"),
        BANNED("封禁"),
        INACTIVE("未激活");

        private final String description;

        UserStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 获取显示名称（优先使用昵称，否则使用用户名）
     */
    public String getDisplayName() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : username;
    }

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * 检查用户是否活跃
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
