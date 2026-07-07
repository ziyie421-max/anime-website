package com.anime.website.controller;

import com.anime.website.dto.ApiResponse;
import com.anime.website.dto.rating.AddRatingRequest;
import com.anime.website.dto.rating.RatingDTO;
import com.anime.website.dto.rating.RatingStatsDTO;
import com.anime.website.security.UserPrincipal;
import com.anime.website.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 评分控制器 - 处理评分相关请求
 * 注意：context-path已配置为/api，所以这里不需要再加/api前缀
 */
@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    /**
     * 添加或更新评分
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RatingDTO>> addOrUpdateRating(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddRatingRequest request) {
        
        RatingDTO rating = ratingService.addOrUpdateRating(userPrincipal.getId(), request);
        // 使用正确的参数顺序：(message, data)
        return ResponseEntity.ok(ApiResponse.success("评分成功", rating));
    }

    /**
     * 获取动漫评分统计（外部动漫）- 公开接口
     */
    @GetMapping("/stats/external/{externalAnimeId}")
    public ResponseEntity<ApiResponse<RatingStatsDTO>> getRatingStats(
            @PathVariable String externalAnimeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        RatingStatsDTO stats = ratingService.getRatingStats(externalAnimeId, userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取用户对某动漫的评分
     */
    @GetMapping("/user/external/{externalAnimeId}")
    public ResponseEntity<ApiResponse<RatingDTO>> getUserRating(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String externalAnimeId) {
        
        RatingDTO rating = ratingService.getUserRating(userPrincipal.getId(), externalAnimeId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    /**
     * 删除评分
     */
    @DeleteMapping("/external/{externalAnimeId}")
    public ResponseEntity<ApiResponse<Void>> deleteRating(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String externalAnimeId) {
        
        ratingService.deleteRating(userPrincipal.getId(), externalAnimeId);
        // 删除操作只返回消息
        return ResponseEntity.ok(ApiResponse.success("评分已删除"));
    }
}

