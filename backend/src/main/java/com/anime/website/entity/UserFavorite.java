package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 用户收藏实体类 - 存储用户收藏的动漫（支持本地和外部动漫）
 */
@Entity
@Table(name = "user_favorites", indexes = {
    @Index(name = "idx_user_favorites_user_id", columnList = "user_id"),
    @Index(name = "idx_user_favorites_external", columnList = "user_id, external_anime_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite extends BaseEntity {

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 本地动漫关联（可为空，用于本地动漫收藏）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private Anime anime;

    // === 外部动漫字段（用于外部资源收藏） ===

    // 外部动漫ID
    @Column(name = "external_anime_id")
    private String externalAnimeId;

    // 外部动漫标题
    @Column(name = "external_anime_title")
    private String externalAnimeTitle;

    // 外部动漫封面图
    @Column(name = "external_anime_cover", length = 500)
    private String externalAnimeCover;

    // 外部动漫简介
    @Column(name = "external_anime_desc", length = 2000)
    private String externalAnimeDesc;

    // 资源源标识（如 lzzy）
    @Column(name = "source_key", length = 50)
    private String sourceKey;

    // 资源源名称
    @Column(name = "source_name", length = 100)
    private String sourceName;

    /**
     * 判断是否为外部动漫收藏
     */
    public boolean isExternalAnime() {
        return externalAnimeId != null && !externalAnimeId.isEmpty();
    }
}
