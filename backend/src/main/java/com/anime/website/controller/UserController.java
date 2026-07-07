package com.anime.website.controller;

import com.anime.website.dto.ApiResponse;
import com.anime.website.dto.user.ChangePasswordRequest;
import com.anime.website.dto.user.UpdateProfileRequest;
import com.anime.website.dto.user.UserProfileDTO;
import com.anime.website.security.UserPrincipal;
import com.anime.website.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器 - 处理用户个人中心相关API
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户资料
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("获取用户资料 - 用户ID: {}", userPrincipal.getId());
        
        UserProfileDTO profile = userService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * 根据用户名获取用户资料（公开信息）
     * GET /api/user/profile/{username}
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfileByUsername(
            @PathVariable String username) {
        log.info("根据用户名获取用户资料: {}", username);
        
        UserProfileDTO profile = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * 更新用户资料
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("更新用户资料 - 用户ID: {}", userPrincipal.getId());
        
        UserProfileDTO profile = userService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("资料更新成功", profile));
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码 - 用户ID: {}", userPrincipal.getId());
        
        userService.changePassword(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    /**
     * 获取用户统计信息
     * GET /api/user/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserService.UserStatsDTO>> getStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("获取用户统计信息 - 用户ID: {}", userPrincipal.getId());
        
        UserService.UserStatsDTO stats = userService.getUserStats(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

