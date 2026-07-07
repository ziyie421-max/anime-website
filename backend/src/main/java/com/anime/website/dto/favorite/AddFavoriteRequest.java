package com.anime.website.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加收藏请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteRequest {
    
    // 本地动漫ID（与外部动漫ID二选一）
    private Long animeId;
    
    // 外部动漫ID
    private String externalAnimeId;
    
    // 外部动漫标题
    private String externalAnimeTitle;
    
    // 外部动漫封面
    private String externalAnimeCover;
    
    // 外部动漫简介
    private String externalAnimeDesc;
    
    // 资源源标识
    private String sourceKey;
    
    // 资源源名称
    private String sourceName;
    
    /**
     * 判断是否为外部动漫收藏
     */
    public boolean isExternalAnime() {
        return externalAnimeId != null && !externalAnimeId.isEmpty();
    }
}

