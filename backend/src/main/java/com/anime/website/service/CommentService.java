package com.anime.website.service;

import com.anime.website.dto.comment.AddCommentRequest;
import com.anime.website.dto.comment.CommentDTO;
import com.anime.website.entity.Comment;
import com.anime.website.entity.Comment.CommentStatus;
import com.anime.website.entity.Notification;
import com.anime.website.entity.User;
import com.anime.website.repository.CommentRepository;
import com.anime.website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 添加评论
     */
    @Transactional
    public CommentDTO addComment(Long userId, AddCommentRequest request) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 创建评论
        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .likeCount(0)
                .status(CommentStatus.ACTIVE)
                .build();

        // 设置动漫信息（外部或本地）
        if (request.getExternalAnimeId() != null && !request.getExternalAnimeId().isEmpty()) {
            comment.setExternalAnimeId(request.getExternalAnimeId());
            comment.setExternalAnimeTitle(request.getExternalAnimeTitle());
        }
        // 注意：本地动漫暂时不支持，因为需要关联 Anime 实体

        // 设置父评论（如果是回复）
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parentComment);
            
            // 继承父评论的动漫信息
            if (parentComment.isExternalAnime()) {
                comment.setExternalAnimeId(parentComment.getExternalAnimeId());
                comment.setExternalAnimeTitle(parentComment.getExternalAnimeTitle());
            } else if (parentComment.getAnime() != null) {
                comment.setAnime(parentComment.getAnime());
            }
            
            // 发送通知给被回复的用户
            if (!parentComment.getUser().getId().equals(userId)) {
                notificationService.createCommentReplyNotification(parentComment, comment, user);
            }
        }

        Comment savedComment = commentRepository.save(comment);
        log.info("用户 {} 发表评论: {}", userId, savedComment.getId());

        return CommentDTO.fromEntity(savedComment);
    }

    /**
     * 获取动漫评论列表（支持外部动漫）
     * 使用@Transactional(readOnly = true)确保懒加载在事务内完成
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByAnime(String externalAnimeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findTopLevelByExternalAnimeId(
                externalAnimeId, CommentStatus.ACTIVE, pageable);
        
        return comments.map(comment -> {
            CommentDTO dto = CommentDTO.fromEntity(comment, true);
            // 加载回复
            List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId(), CommentStatus.ACTIVE);
            dto.setReplies(replies.stream()
                    .map(reply -> CommentDTO.fromEntity(reply, false))
                    .collect(Collectors.toList()));
            dto.setReplyCount(replies.size());
            return dto;
        });
    }

    /**
     * 获取评论的回复列表
     * 使用@Transactional(readOnly = true)确保懒加载在事务内完成
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getReplies(Long commentId) {
        List<Comment> replies = commentRepository.findRepliesByParentId(commentId, CommentStatus.ACTIVE);
        return replies.stream()
                .map(reply -> CommentDTO.fromEntity(reply, false))
                .collect(Collectors.toList());
    }

    /**
     * 删除评论（软删除）
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 检查权限
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
        log.info("用户 {} 删除评论: {}", userId, commentId);
    }

    /**
     * 点赞评论
     */
    @Transactional
    public CommentDTO likeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        comment.setLikeCount(comment.getLikeCount() + 1);
        Comment savedComment = commentRepository.save(comment);

        // 发送点赞通知
        if (!comment.getUser().getId().equals(userId)) {
            User liker = userRepository.findById(userId).orElse(null);
            if (liker != null) {
                notificationService.createCommentLikeNotification(comment, liker);
            }
        }

        log.info("用户 {} 点赞评论: {}", userId, commentId);
        return CommentDTO.fromEntity(savedComment);
    }

    /**
     * 获取评论统计
     */
    public long getCommentCount(String externalAnimeId) {
        return commentRepository.countByExternalAnimeId(externalAnimeId, CommentStatus.ACTIVE);
    }
}

