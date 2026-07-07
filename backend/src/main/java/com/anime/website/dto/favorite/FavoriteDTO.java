package com.anime.website.dto.favorite;

import com.anime.website.entity.UserFavorite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏DTO - 返回收藏信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {
    
    private Long id;
    private Long userId;
    
    // 本地动漫信息
    private Long animeId;
    private String animeTitle;
    private String animeCover;
    
    // 外部动漫信息
    private String externalAnimeId;
    private String externalAnimeTitle;
    private String externalAnimeCover;
    private String externalAnimeDesc;
    private String sourceKey;
    private String sourceName;
    
    // 是否为外部动漫
    private boolean isExternal;
    
    private LocalDateTime createdAt;
    
    /**
     * 从实体转换
     */
    public static FavoriteDTO fromEntity(UserFavorite favorite) {
        FavoriteDTO dto = FavoriteDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUser().getId())
                .externalAnimeId(favorite.getExternalAnimeId())
                .externalAnimeTitle(favorite.getExternalAnimeTitle())
                .externalAnimeCover(favorite.getExternalAnimeCover())
                .externalAnimeDesc(favorite.getExternalAnimeDesc())
                .sourceKey(favorite.getSourceKey())
                .sourceName(favorite.getSourceName())
                .isExternal(favorite.isExternalAnime())
                .createdAt(favorite.getCreatedAt())
                .build();
        
        // 填充本地动漫信息
        if (favorite.getAnime() != null) {
            dto.setAnimeId(favorite.getAnime().getId());
            dto.setAnimeTitle(favorite.getAnime().getTitle());
            dto.setAnimeCover(favorite.getAnime().getPoster()); // 使用poster字段作为封面
        }
        
        return dto;
    }
}

