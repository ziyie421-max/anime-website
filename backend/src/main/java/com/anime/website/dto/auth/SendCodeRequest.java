package com.anime.website.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发送验证码请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeRequest {

    // 邮箱
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    // 验证码类型
    @NotBlank(message = "验证码类型不能为空")
    private String type; // register, reset-password, change-email, login
}
