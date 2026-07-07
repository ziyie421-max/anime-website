package com.anime.website.controller;

import com.anime.website.dto.ApiResponse;
import com.anime.website.dto.favorite.AddFavoriteRequest;
import com.anime.website.dto.favorite.FavoriteDTO;
import com.anime.website.security.UserPrincipal;
import com.anime.website.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器 - 处理用户收藏相关API
 */
@Slf4j
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     * POST /api/favorites
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteDTO>> addFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AddFavoriteRequest request) {
        log.info("添加收藏 - 用户ID: {}", userPrincipal.getId());
        
        FavoriteDTO favorite = favoriteService.addFavorite(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("收藏成功", favorite));
    }

    /**
     * 取消收藏
     * DELETE /api/favorites
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String externalAnimeId,
            @RequestParam(required = false) Long animeId) {
        log.info("取消收藏 - 用户ID: {}, 外部动漫ID: {}, 本地动漫ID: {}", 
                userPrincipal.getId(), externalAnimeId, animeId);
        
        favoriteService.removeFavorite(userPrincipal.getId(), externalAnimeId, animeId);
        return ResponseEntity.ok(ApiResponse.success("已取消收藏", null));
    }

    /**
     * 获取收藏列表（分页）
     * GET /api/favorites
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FavoriteDTO>>> getFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取收藏列表 - 用户ID: {}, 页码: {}, 大小: {}", userPrincipal.getId(), page, size);
        
        Page<FavoriteDTO> favorites = favoriteService.getFavorites(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    /**
     * 获取全部收藏列表
     * GET /api/favorites/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getAllFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("获取全部收藏列表 - 用户ID: {}", userPrincipal.getId());
        
        List<FavoriteDTO> favorites = favoriteService.getAllFavorites(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    /**
     * 检查是否已收藏
     * GET /api/favorites/check
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorited(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String externalAnimeId,
            @RequestParam(required = false) Long animeId) {
        log.info("检查是否已收藏 - 用户ID: {}, 外部动漫ID: {}, 本地动漫ID: {}", 
                userPrincipal.getId(), externalAnimeId, animeId);
        
        boolean isFavorited = favoriteService.isFavorited(userPrincipal.getId(), externalAnimeId, animeId);
        // 使用Collections.singletonMap替代Map.of（兼容Java 8）
        return ResponseEntity.ok(ApiResponse.success(Collections.singletonMap("isFavorited", isFavorited)));
    }

    /**
     * 批量检查是否已收藏（外部动漫）
     * POST /api/favorites/check-batch
     */
    @PostMapping("/check-batch")
    public ResponseEntity<ApiResponse<List<String>>> checkFavoritedBatch(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody List<String> externalAnimeIds) {
        log.info("批量检查是否已收藏 - 用户ID: {}, 数量: {}", userPrincipal.getId(), externalAnimeIds.size());
        
        List<String> favoritedIds = favoriteService.checkFavoritedExternalAnimeIds(
                userPrincipal.getId(), externalAnimeIds);
        return ResponseEntity.ok(ApiResponse.success(favoritedIds));
    }

    /**
     * 获取收藏数量
     * GET /api/favorites/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getFavoriteCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("获取收藏数量 - 用户ID: {}", userPrincipal.getId());
        
        long count = favoriteService.getFavoriteCount(userPrincipal.getId());
        // 使用Collections.singletonMap替代Map.of（兼容Java 8）
        return ResponseEntity.ok(ApiResponse.success(Collections.singletonMap("count", count)));
    }

    /**
     * 清空所有收藏
     * DELETE /api/favorites/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> clearAllFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("清空所有收藏 - 用户ID: {}", userPrincipal.getId());
        
        favoriteService.clearAllFavorites(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("已清空所有收藏", null));
    }
}

