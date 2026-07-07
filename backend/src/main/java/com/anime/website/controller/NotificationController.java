package com.anime.website.controller;

import com.anime.website.dto.ApiResponse;
import com.anime.website.dto.notification.NotificationDTO;
import com.anime.website.security.UserPrincipal;
import com.anime.website.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器 - 处理通知相关请求
 * 注意：context-path已配置为/api，所以这里不需要再加/api前缀
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationDTO> notifications = notificationService.getNotifications(
                userPrincipal.getId(), page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notifications.getContent());
        result.put("totalElements", notifications.getTotalElements());
        result.put("totalPages", notifications.getTotalPages());
        result.put("currentPage", page);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        long count = notificationService.getUnreadCount(userPrincipal.getId());
        
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取未读通知列表
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationDTO> notifications = notificationService.getUnreadNotifications(
                userPrincipal.getId(), page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notifications.getContent());
        result.put("totalElements", notifications.getTotalElements());
        result.put("totalPages", notifications.getTotalPages());
        result.put("currentPage", page);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 标记单个通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long notificationId) {
        
        notificationService.markAsRead(userPrincipal.getId(), notificationId);
        // 标记已读只返回消息
        return ResponseEntity.ok(ApiResponse.success("已标记为已读"));
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        int count = notificationService.markAllAsRead(userPrincipal.getId());
        
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        // 使用正确的参数顺序：(message, data)
        return ResponseEntity.ok(ApiResponse.success("已全部标记为已读", result));
    }

    /**
     * 删除所有通知
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> deleteAllNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        int count = notificationService.deleteAllNotifications(userPrincipal.getId());
        
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        // 使用正确的参数顺序：(message, data)
        return ResponseEntity.ok(ApiResponse.success("已删除所有通知", result));
    }
}

