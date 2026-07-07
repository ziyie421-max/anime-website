package com.anime.website.dto.comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 添加评论请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    
    // 本地动漫ID（可选）
    private Long animeId;
    
    // 外部动漫ID（可选，与animeId二选一）
    private String externalAnimeId;
    
    // 外部动漫标题（外部动漫时必填）
    private String externalAnimeTitle;
    
    // 父评论ID（回复时使用）
    private Long parentId;
    
    // 评论内容
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度需在1-1000字符之间")
    private String content;
}

