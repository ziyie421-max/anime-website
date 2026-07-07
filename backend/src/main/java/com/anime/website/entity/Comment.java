package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论实体类 - 存储用户评论信息（支持本地和外部动漫）
 */
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comments_user_id", columnList = "user_id"),
    @Index(name = "idx_comments_anime_id", columnList = "anime_id"),
    @Index(name = "idx_comments_external", columnList = "external_anime_id"),
    @Index(name = "idx_comments_parent_id", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

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

    // 外部动漫标题（用于显示）
    @Column(name = "external_anime_title")
    private String externalAnimeTitle;

    // 父评论（用于回复）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 回复列表
    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    // 评论内容
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 点赞数
    @Builder.Default
    @Column(name = "like_count")
    private Integer likeCount = 0;

    // 评论状态
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CommentStatus status = CommentStatus.ACTIVE;

    /**
     * 评论状态枚举
     */
    public enum CommentStatus {
        ACTIVE("正常"),
        HIDDEN("隐藏"),
        DELETED("已删除");

        private final String description;

        CommentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 判断是否为外部动漫评论
     */
    public boolean isExternalAnime() {
        return externalAnimeId != null && !externalAnimeId.isEmpty();
    }
}
