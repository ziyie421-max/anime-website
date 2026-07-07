package com.anime.website.service;

import com.anime.website.dto.notification.NotificationDTO;
import com.anime.website.entity.Comment;
import com.anime.website.entity.Notification;
import com.anime.website.entity.Notification.NotificationType;
import com.anime.website.entity.User;
import com.anime.website.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知服务层 - 处理互动通知
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 创建评论回复通知
     */
    @Transactional
    public void createCommentReplyNotification(Comment parentComment, Comment replyComment, User replier) {
        // 构建通知链接
        String link = buildCommentLink(parentComment);
        
        // 构建通知内容
        String content = String.format("%s 回复了你的评论: \"%s\"",
                replier.getNickname() != null ? replier.getNickname() : replier.getUsername(),
                truncateContent(replyComment.getContent(), 50));

        Notification notification = Notification.builder()
                .user(parentComment.getUser())  // 接收者是被回复的用户
                .sender(replier)                // 发送者是回复者
                .type(NotificationType.COMMENT_REPLY)
                .title("收到新回复")
                .content(content)
                .commentId(replyComment.getId())
                .link(link)
                .isRead(false)
                .build();

        // 设置动漫信息
        if (parentComment.isExternalAnime()) {
            notification.setExternalAnimeId(parentComment.getExternalAnimeId());
            notification.setExternalAnimeTitle(parentComment.getExternalAnimeTitle());
        } else if (parentComment.getAnime() != null) {
            notification.setAnimeId(parentComment.getAnime().getId());
        }

        notificationRepository.save(notification);
        log.info("创建评论回复通知: 用户 {} 收到来自 {} 的回复", 
                parentComment.getUser().getId(), replier.getId());
    }

    /**
     * 创建评论点赞通知
     */
    @Transactional
    public void createCommentLikeNotification(Comment comment, User liker) {
        String link = buildCommentLink(comment);
        
        String content = String.format("%s 赞了你的评论: \"%s\"",
                liker.getNickname() != null ? liker.getNickname() : liker.getUsername(),
                truncateContent(comment.getContent(), 50));

        Notification notification = Notification.builder()
                .user(comment.getUser())
                .sender(liker)
                .type(NotificationType.COMMENT_LIKE)
                .title("评论被点赞")
                .content(content)
                .commentId(comment.getId())
                .link(link)
                .isRead(false)
                .build();

        // 设置动漫信息
        if (comment.isExternalAnime()) {
            notification.setExternalAnimeId(comment.getExternalAnimeId());
            notification.setExternalAnimeTitle(comment.getExternalAnimeTitle());
        } else if (comment.getAnime() != null) {
            notification.setAnimeId(comment.getAnime().getId());
        }

        notificationRepository.save(notification);
        log.info("创建评论点赞通知: 用户 {} 的评论被 {} 点赞", 
                comment.getUser().getId(), liker.getId());
    }

    /**
     * 获取用户通知列表
     * 使用@Transactional(readOnly = true)确保懒加载在事务内完成
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return notifications.map(NotificationDTO::fromEntity);
    }

    /**
     * 获取用户未读通知
     * 使用@Transactional(readOnly = true)确保懒加载在事务内完成
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findUnreadByUserId(userId, pageable);
        return notifications.map(NotificationDTO::fromEntity);
    }

    /**
     * 获取未读通知数量
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(notificationId, userId);
        log.info("用户 {} 标记通知 {} 为已读", userId, notificationId);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId);
        log.info("用户 {} 标记所有通知为已读, 共 {} 条", userId, count);
        return count;
    }

    /**
     * 删除所有通知
     */
    @Transactional
    public int deleteAllNotifications(Long userId) {
        int count = notificationRepository.deleteAllByUserId(userId);
        log.info("用户 {} 删除所有通知, 共 {} 条", userId, count);
        return count;
    }

    /**
     * 构建评论链接
     */
    private String buildCommentLink(Comment comment) {
        if (comment.isExternalAnime()) {
            return "/anime/" + comment.getExternalAnimeId() + "#comment-" + comment.getId();
        } else if (comment.getAnime() != null) {
            return "/anime/" + comment.getAnime().getId() + "#comment-" + comment.getId();
        }
        return "#";
    }

    /**
     * 截断内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}

