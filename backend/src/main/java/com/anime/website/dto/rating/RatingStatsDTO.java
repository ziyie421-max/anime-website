package com.anime.website.dto.rating;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 评分统计信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingStatsDTO {
    
    // 动漫ID
    private Long animeId;
    private String externalAnimeId;
    
    // 平均评分（1.0-10.0）
    private Double averageScore;
    
    // 评分人数
    private Long ratingCount;
    
    // 当前用户的评分（如果已评分）
    private RatingDTO userRating;
}

