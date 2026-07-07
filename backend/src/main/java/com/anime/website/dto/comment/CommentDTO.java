package com.anime.website.dto.comment;

import com.anime.website.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    
    private Long id;
    
    // 用户信息
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    
    // 动漫信息
    private Long animeId;
    private String externalAnimeId;
    private String externalAnimeTitle;
    
    // 评论内容
    private String content;
    private Integer likeCount;
    private String status;
    
    // 父评论ID
    private Long parentId;
    
    // 回复列表
    @Builder.Default
    private List<CommentDTO> replies = new ArrayList<>();
    
    // 回复数量
    private Integer replyCount;
    
    // 时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 是否为外部动漫
    private boolean isExternal;
    
    /**
     * 从实体转换（不含回复）
     */
    public static CommentDTO fromEntity(Comment comment) {
        return fromEntity(comment, false);
    }
    
    /**
     * 从实体转换
     * @param comment 评论实体
     * @param includeReplies 是否包含回复
     */
    public static CommentDTO fromEntity(Comment comment, boolean includeReplies) {
        if (comment == null) return null;
        
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .nickname(comment.getUser().getNickname())
                .avatar(comment.getUser().getAvatar())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .status(comment.getStatus().name())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replyCount(comment.getReplies() != null ? comment.getReplies().size() : 0)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isExternal(comment.isExternalAnime())
                .build();
        
        // 填充动漫信息
        if (comment.isExternalAnime()) {
            dto.setExternalAnimeId(comment.getExternalAnimeId());
            dto.setExternalAnimeTitle(comment.getExternalAnimeTitle());
        } else if (comment.getAnime() != null) {
            dto.setAnimeId(comment.getAnime().getId());
        }
        
        // 填充回复
        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .filter(reply -> reply.getStatus() == Comment.CommentStatus.ACTIVE)
                    .map(reply -> fromEntity(reply, false))
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}

