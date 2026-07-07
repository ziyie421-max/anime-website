package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 剧集实体类 - 存储动漫剧集信息
 */
@Entity
@Table(name = "episodes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"anime_id", "episode_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Episode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration")
    private Integer duration; // 时长（秒）

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "air_date")
    private LocalDate airDate; // 播出日期
}
