package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 通知实体类 - 存储用户通知（互动通知）
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_read", columnList = "user_id, is_read"),
    @Index(name = "idx_notifications_created", columnList = "user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    // 接收通知的用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 触发通知的用户（如回复者）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // 通知类型
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    // 通知标题
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 通知内容
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 关联的评论ID（用于回复通知）
    @Column(name = "comment_id")
    private Long commentId;

    // 关联的动漫ID或外部动漫ID
    @Column(name = "anime_id")
    private Long animeId;
    
    @Column(name = "external_anime_id")
    private String externalAnimeId;
    
    @Column(name = "external_anime_title")
    private String externalAnimeTitle;

    // 跳转链接（前端用）
    @Column(name = "link", length = 500)
    private String link;

    // 是否已读
    @Builder.Default
    @Column(name = "is_read")
    private Boolean isRead = false;

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        COMMENT_REPLY("评论回复", "有人回复了你的评论"),
        COMMENT_LIKE("评论点赞", "有人赞了你的评论"),
        SYSTEM("系统通知", "系统消息");

        private final String name;
        private final String description;

        NotificationType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}

