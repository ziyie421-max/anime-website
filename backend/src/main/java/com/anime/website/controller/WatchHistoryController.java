package com.anime.website.controller;

import com.anime.website.dto.ApiResponse;
import com.anime.website.dto.history.AddWatchHistoryRequest;
import com.anime.website.dto.history.WatchHistoryDTO;
import com.anime.website.security.UserPrincipal;
import com.anime.website.service.WatchHistoryService;
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
 * 观看历史控制器 - 处理用户观看历史相关API
 */
@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService historyService;

    /**
     * 添加/更新观看历史
     * POST /api/history
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WatchHistoryDTO>> addOrUpdateHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AddWatchHistoryRequest request) {
        log.info("添加/更新观看历史 - 用户ID: {}", userPrincipal.getId());
        
        WatchHistoryDTO history = historyService.addOrUpdateHistory(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("观看记录已保存", history));
    }

    /**
     * 更新观看进度
     * PUT /api/history/{id}/progress
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam Integer progress,
            @RequestParam(required = false) Integer totalDuration) {
        log.info("更新观看进度 - 用户ID: {}, 记录ID: {}, 进度: {}s", userPrincipal.getId(), id, progress);
        
        historyService.updateProgress(userPrincipal.getId(), id, progress, totalDuration);
        return ResponseEntity.ok(ApiResponse.success("进度已更新", null));
    }

    /**
     * 获取观看历史（分页）
     * GET /api/history
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WatchHistoryDTO>>> getWatchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取观看历史 - 用户ID: {}, 页码: {}, 大小: {}", userPrincipal.getId(), page, size);
        
        Page<WatchHistoryDTO> histories = historyService.getWatchHistory(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    /**
     * 获取最近观看的动漫列表
     * GET /api/history/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<WatchHistoryDTO>>> getRecentWatched(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("获取最近观看的动漫 - 用户ID: {}, 限制: {}", userPrincipal.getId(), limit);
        
        List<WatchHistoryDTO> histories = historyService.getRecentWatchedAnime(userPrincipal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    /**
     * 获取继续观看列表
     * GET /api/history/continue
     */
    @GetMapping("/continue")
    public ResponseEntity<ApiResponse<List<WatchHistoryDTO>>> getContinueWatching(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("获取继续观看列表 - 用户ID: {}, 限制: {}", userPrincipal.getId(), limit);
        
        List<WatchHistoryDTO> histories = historyService.getContinueWatching(userPrincipal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    /**
     * 获取某部动漫的观看记录
     * GET /api/history/anime/{externalAnimeId}
     */
    @GetMapping("/anime/{externalAnimeId}")
    public ResponseEntity<ApiResponse<WatchHistoryDTO>> getAnimeWatchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String externalAnimeId) {
        log.info("获取动漫观看记录 - 用户ID: {}, 外部动漫ID: {}", userPrincipal.getId(), externalAnimeId);
        
        WatchHistoryDTO history = historyService.getAnimeWatchHistory(userPrincipal.getId(), externalAnimeId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 删除单条观看记录
     * DELETE /api/history/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        log.info("删除观看记录 - 用户ID: {}, 记录ID: {}", userPrincipal.getId(), id);
        
        historyService.deleteHistory(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("观看记录已删除", null));
    }

    /**
     * 清空所有观看历史
     * DELETE /api/history/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> clearAllHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("清空所有观看历史 - 用户ID: {}", userPrincipal.getId());
        
        historyService.clearAllHistory(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("已清空所有观看历史", null));
    }

    /**
     * 获取观看历史数量
     * GET /api/history/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getHistoryCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("获取观看历史数量 - 用户ID: {}", userPrincipal.getId());
        
        long count = historyService.getHistoryCount(userPrincipal.getId());
        // 使用Collections.singletonMap替代Map.of（兼容Java 8）
        return ResponseEntity.ok(ApiResponse.success(Collections.singletonMap("count", count)));
    }
}

