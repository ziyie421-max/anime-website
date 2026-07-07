package com.anime.website.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

/**
 * 更新用户资料请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(max = 50, message = "昵称最多50个字符")
    private String nickname;
    
    @Size(max = 500, message = "头像URL最多500个字符")
    private String avatar;
    
    @Size(max = 200, message = "个人简介最多200个字符")
    private String bio;
}

