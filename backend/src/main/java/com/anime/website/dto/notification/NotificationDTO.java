package com.anime.website.dto.notification;

import com.anime.website.entity.Notification;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 通知响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    
    private Long id;
    
    // 发送者信息
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    
    // 通知信息
    private String type;
    private String typeName;
    private String title;
    private String content;
    private String link;
    private Boolean isRead;
    
    // 关联信息
    private Long commentId;
    private Long animeId;
    private String externalAnimeId;
    private String externalAnimeTitle;
    
    // 时间
    private LocalDateTime createdAt;
    
    /**
     * 从实体转换
     */
    public static NotificationDTO fromEntity(Notification notification) {
        if (notification == null) return null;
        
        NotificationDTO dto = NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .typeName(notification.getType().getName())
                .title(notification.getTitle())
                .content(notification.getContent())
                .link(notification.getLink())
                .isRead(notification.getIsRead())
                .commentId(notification.getCommentId())
                .animeId(notification.getAnimeId())
                .externalAnimeId(notification.getExternalAnimeId())
                .externalAnimeTitle(notification.getExternalAnimeTitle())
                .createdAt(notification.getCreatedAt())
                .build();
        
        // 填充发送者信息
        if (notification.getSender() != null) {
            dto.setSenderId(notification.getSender().getId());
            dto.setSenderName(notification.getSender().getNickname() != null 
                    ? notification.getSender().getNickname() 
                    : notification.getSender().getUsername());
            dto.setSenderAvatar(notification.getSender().getAvatar());
        }
        
        return dto;
    }
}

