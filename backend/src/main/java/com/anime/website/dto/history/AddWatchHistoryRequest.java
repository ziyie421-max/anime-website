package com.anime.website.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加/更新观看历史请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddWatchHistoryRequest {
    
    // 本地动漫ID（与外部动漫ID二选一）
    private Long animeId;
    
    // 本地剧集ID
    private Long episodeId;
    
    // 外部动漫ID
    private String externalAnimeId;
    
    // 外部动漫标题
    private String externalAnimeTitle;
    
    // 外部动漫封面
    private String externalAnimeCover;
    
    // 外部剧集名称
    private String externalEpisodeName;
    
    // 外部剧集索引
    private Integer externalEpisodeIndex;
    
    // 资源源标识
    private String sourceKey;
    
    // 资源源名称
    private String sourceName;
    
    // 观看进度（秒）
    private Integer watchProgress;
    
    // 视频总时长（秒）
    private Integer totalDuration;
    
    /**
     * 判断是否为外部动漫
     */
    public boolean isExternalAnime() {
        return externalAnimeId != null && !externalAnimeId.isEmpty();
    }
}

