package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 评分实体类 - 存储用户对动漫的评分（支持本地和外部动漫）
 */
@Entity
@Table(name = "ratings", indexes = {
    @Index(name = "idx_ratings_user_id", columnList = "user_id"),
    @Index(name = "idx_ratings_anime_id", columnList = "anime_id"),
    @Index(name = "idx_ratings_external", columnList = "external_anime_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating extends BaseEntity {

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 本地动漫关联（可为空）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    // === 外部动漫字段 ===
    
    // 外部动漫ID
    @Column(name = "external_anime_id")
    private String externalAnimeId;
    
    // 外部动漫标题
    @Column(name = "external_anime_title")
    private String externalAnimeTitle;

    // 评分值（1-10分，支持0.5分制，存储为整数 1-100）
    @Column(name = "score", nullable = false)
    private Integer score;

    // 评分评语（可选）
    @Column(name = "review", length = 500)
    private String review;

    /**
     * 判断是否为外部动漫评分
     */
    public boolean isExternalAnime() {
        return externalAnimeId != null && !externalAnimeId.isEmpty();
    }

    /**
     * 获取显示用的评分值（10分制）
     */
    public double getDisplayScore() {
        return score / 10.0;
    }
}

