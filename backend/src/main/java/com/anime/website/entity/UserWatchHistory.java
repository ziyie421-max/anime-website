package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户观看历史实体类 - 存储用户的观看记录和进度（支持本地和外部动漫）
 */
@Entity
@Table(name = "user_watch_history", indexes = {
    @Index(name = "idx_watch_history_user_id", columnList = "user_id"),
    @Index(name = "idx_watch_history_external", columnList = "user_id, external_anime_id"),
    @Index(name = "idx_watch_history_last_watch", columnList = "user_id, last_watch_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWatchHistory extends BaseEntity {

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 本地动漫关联（可为空）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    // 本地剧集关联（可为空）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    // === 外部动漫字段 ===

    // 外部动漫ID
    @Column(name = "external_anime_id")
    private String externalAnimeId;

    // 外部动漫标题
    @Column(name = "external_anime_title")
    private String externalAnimeTitle;

    // 外部动漫封面图
    @Column(name = "external_anime_cover", length = 500)
    private String externalAnimeCover;

    // 外部剧集信息（如 "第1集"）
    @Column(name = "external_episode_name")
    private String externalEpisodeName;

    // 外部剧集索引
    @Column(name = "external_episode_index")
    private Integer externalEpisodeIndex;

    // 资源源标识
    @Column(name = "source_key", length = 50)
    private String sourceKey;

    // 资源源名称
    @Column(name = "source_name", length = 100)
    private String sourceName;

    // === 观看进度字段 ===

    // 观看进度（秒）
    @Builder.Default
    @Column(name = "watch_progress")
    private Integer watchProgress = 0;

    // 视频总时长（秒）
    @Builder.Default
    @Column(name = "total_duration")
    private Integer totalDuration = 0;

    // 最后观看时间
    @Column(name = "last_watch_time")
    private LocalDateTime lastWatchTime;

    // 观看次数
    @Builder.Default
    @Column(name = "watch_count")
    private Integer watchCount = 1;

    /**
     * 判断是否为外部动漫
     */
    public boolean isExternalAnime() {
        return externalAnimeId != null && !externalAnimeId.isEmpty();
    }

    /**
     * 计算观看进度百分比
     */
    public int getProgressPercent() {
        if (totalDuration == null || totalDuration == 0) {
            return 0;
        }
        return (int) ((watchProgress * 100.0) / totalDuration);
    }
}
