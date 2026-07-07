package com.anime.website.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求DTO - 用户登录时提交的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // 用户名或邮箱
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(min = 3, max = 100, message = "用户名或邮箱长度必须在3-100个字符之间")
    private String usernameOrEmail;

    // 密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    // 记住我（可选）
    private Boolean rememberMe = false;
}
