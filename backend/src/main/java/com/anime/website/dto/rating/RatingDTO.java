package com.anime.website.dto.rating;

import com.anime.website.entity.Rating;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 评分响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {
    
    private Long id;
    
    // 用户信息
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    
    // 动漫信息
    private Long animeId;
    private String externalAnimeId;
    private String externalAnimeTitle;
    
    // 评分信息
    private Integer score;          // 原始分数（1-100）
    private Double displayScore;    // 显示分数（1.0-10.0）
    private String review;
    
    // 时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 是否为外部动漫
    private boolean isExternal;
    
    /**
     * 从实体转换
     */
    public static RatingDTO fromEntity(Rating rating) {
        if (rating == null) return null;
        
        RatingDTO dto = RatingDTO.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .nickname(rating.getUser().getNickname())
                .avatar(rating.getUser().getAvatar())
                .score(rating.getScore())
                .displayScore(rating.getDisplayScore())
                .review(rating.getReview())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .isExternal(rating.isExternalAnime())
                .build();
        
        // 填充动漫信息
        if (rating.isExternalAnime()) {
            dto.setExternalAnimeId(rating.getExternalAnimeId());
            dto.setExternalAnimeTitle(rating.getExternalAnimeTitle());
        } else if (rating.getAnime() != null) {
            dto.setAnimeId(rating.getAnime().getId());
        }
        
        return dto;
    }
}

