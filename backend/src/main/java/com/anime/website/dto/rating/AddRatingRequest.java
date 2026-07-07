package com.anime.website.dto.rating;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 添加/更新评分请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRatingRequest {
    
    // 本地动漫ID（可选）
    private Long animeId;
    
    // 外部动漫ID（可选，与animeId二选一）
    private String externalAnimeId;
    
    // 外部动漫标题（外部动漫时必填）
    private String externalAnimeTitle;
    
    // 评分值（1-100，对应0.1-10分）
    @NotNull(message = "评分不能为空")
    @Min(value = 10, message = "评分最低为1分")
    @Max(value = 100, message = "评分最高为10分")
    private Integer score;
    
    // 评分评语（可选）
    private String review;
}

