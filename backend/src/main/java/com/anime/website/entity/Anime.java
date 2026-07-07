package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 动漫实体类 - 存储动漫基本信息
 */
@Entity
@Table(name = "anime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anime extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "original_title", length = 200)
    private String originalTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster")
    private String poster;

    @Column(name = "banner")
    private String banner;

    @Column(name = "year")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "season")
    private Season season;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnimeStatus status = AnimeStatus.UPCOMING;

    @Column(name = "total_episodes")
    private Integer totalEpisodes = 0;

    @Column(name = "current_episodes")
    private Integer currentEpisodes = 0;

    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "studio", length = 100)
    private String studio; // 制作公司

    @Column(name = "director", length = 100)
    private String director; // 导演

    // 多对多关系：动漫-分类
    @JsonIgnore // 避免JSON序列化时的懒加载问题
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "anime_categories",
        joinColumns = @JoinColumn(name = "anime_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    /**
     * 季节枚举
     */
    public enum Season {
        SPRING("春季"),
        SUMMER("夏季"),
        AUTUMN("秋季"),
        WINTER("冬季");

        private final String description;

        Season(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 动漫状态枚举
     */
    public enum AnimeStatus {
        ONGOING("连载中"),
        COMPLETED("已完结"),
        UPCOMING("即将播出");

        private final String description;

        AnimeStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
