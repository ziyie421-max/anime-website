package com.anime.website.dto.history;

import com.anime.website.entity.UserWatchHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 观看历史DTO - 返回观看历史信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistoryDTO {
    
    private Long id;
    private Long userId;
    
    // 本地动漫信息
    private Long animeId;
    private String animeTitle;
    private String animeCover;
    private Long episodeId;
    private String episodeTitle;
    private Integer episodeNumber;
    
    // 外部动漫信息
    private String externalAnimeId;
    private String externalAnimeTitle;
    private String externalAnimeCover;
    private String externalEpisodeName;
    private Integer externalEpisodeIndex;
    private String sourceKey;
    private String sourceName;
    
    // 观看进度
    private Integer watchProgress;
    private Integer totalDuration;
    private Integer progressPercent;
    private Integer watchCount;
    
    // 是否为外部动漫
    private boolean isExternal;
    
    private LocalDateTime lastWatchTime;
    private LocalDateTime createdAt;
    
    /**
     * 从实体转换
     */
    public static WatchHistoryDTO fromEntity(UserWatchHistory history) {
        WatchHistoryDTO dto = WatchHistoryDTO.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .externalAnimeId(history.getExternalAnimeId())
                .externalAnimeTitle(history.getExternalAnimeTitle())
                .externalAnimeCover(history.getExternalAnimeCover())
                .externalEpisodeName(history.getExternalEpisodeName())
                .externalEpisodeIndex(history.getExternalEpisodeIndex())
                .sourceKey(history.getSourceKey())
                .sourceName(history.getSourceName())
                .watchProgress(history.getWatchProgress())
                .totalDuration(history.getTotalDuration())
                .progressPercent(history.getProgressPercent())
                .watchCount(history.getWatchCount())
                .isExternal(history.isExternalAnime())
                .lastWatchTime(history.getLastWatchTime())
                .createdAt(history.getCreatedAt())
                .build();
        
        // 填充本地动漫信息
        if (history.getAnime() != null) {
            dto.setAnimeId(history.getAnime().getId());
            dto.setAnimeTitle(history.getAnime().getTitle());
            dto.setAnimeCover(history.getAnime().getPoster()); // 使用poster字段作为封面
        }
        
        // 填充本地剧集信息
        if (history.getEpisode() != null) {
            dto.setEpisodeId(history.getEpisode().getId());
            dto.setEpisodeTitle(history.getEpisode().getTitle());
            dto.setEpisodeNumber(history.getEpisode().getEpisodeNumber());
        }
        
        return dto;
    }
}

