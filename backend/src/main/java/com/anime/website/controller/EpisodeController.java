package com.anime.website.controller;

import com.anime.website.entity.Episode;
import com.anime.website.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剧集控制器 - 处理剧集相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/episodes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EpisodeController {

    private final EpisodeService episodeService;

    /**
     * 根据动漫ID获取剧集列表
     */
    @GetMapping("/anime/{animeId}")
    public ResponseEntity<List<Episode>> getEpisodesByAnimeId(
            @PathVariable Long animeId,
            @RequestParam(defaultValue = "false") boolean publishedOnly) {
        
        log.info("获取动漫剧集列表 - 动漫ID: {}, 仅已发布: {}", animeId, publishedOnly);
        
        List<Episode> episodes;
        if (publishedOnly) {
            episodes = episodeService.getPublishedEpisodesByAnimeId(animeId);
        } else {
            episodes = episodeService.getEpisodesByAnimeId(animeId);
        }
        
        return ResponseEntity.ok(episodes);
    }

    /**
     * 分页获取动漫剧集列表
     */
    @GetMapping("/anime/{animeId}/page")
    public ResponseEntity<Page<Episode>> getEpisodesByAnimeIdPaged(
            @PathVariable Long animeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean publishedOnly) {
        
        log.info("分页获取动漫剧集列表 - 动漫ID: {}, 页码: {}, 大小: {}, 仅已发布: {}", animeId, page, size, publishedOnly);
        
        Page<Episode> episodes;
        if (publishedOnly) {
            episodes = episodeService.getPublishedEpisodesByAnimeId(animeId, page, size);
        } else {
            episodes = episodeService.getEpisodesByAnimeId(animeId, page, size);
        }
        
        return ResponseEntity.ok(episodes);
    }

    /**
     * 根据ID获取剧集详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Episode> getEpisodeById(@PathVariable Long id) {
        log.info("获取剧集详情 - ID: {}", id);
        
        return episodeService.getEpisodeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据动漫ID和剧集编号获取剧集
     */
    @GetMapping("/anime/{animeId}/episode/{episodeNumber}")
    public ResponseEntity<Episode> getEpisodeByAnimeIdAndNumber(
            @PathVariable Long animeId,
            @PathVariable Integer episodeNumber) {
        
        log.info("根据动漫ID和剧集编号获取剧集 - 动漫ID: {}, 剧集编号: {}", animeId, episodeNumber);
        
        return episodeService.getEpisodeByAnimeIdAndNumber(animeId, episodeNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取动漫的热门剧集
     */
    @GetMapping("/anime/{animeId}/popular")
    public ResponseEntity<List<Episode>> getPopularEpisodes(
            @PathVariable Long animeId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("获取动漫热门剧集 - 动漫ID: {}, 限制: {}", animeId, limit);
        
        List<Episode> episodes = episodeService.getPopularEpisodesByAnimeId(animeId, limit);
        return ResponseEntity.ok(episodes);
    }

    /**
     * 获取动漫的最新剧集
     */
    @GetMapping("/anime/{animeId}/latest")
    public ResponseEntity<List<Episode>> getLatestEpisodes(
            @PathVariable Long animeId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("获取动漫最新剧集 - 动漫ID: {}, 限制: {}", animeId, limit);
        
        List<Episode> episodes = episodeService.getLatestEpisodesByAnimeId(animeId, limit);
        return ResponseEntity.ok(episodes);
    }

    /**
     * 搜索动漫剧集
     */
    @GetMapping("/anime/{animeId}/search")
    public ResponseEntity<List<Episode>> searchEpisodes(
            @PathVariable Long animeId,
            @RequestParam String keyword) {
        
        log.info("搜索动漫剧集 - 动漫ID: {}, 关键词: {}", animeId, keyword);
        
        List<Episode> episodes = episodeService.searchEpisodesByAnimeIdAndKeyword(animeId, keyword);
        return ResponseEntity.ok(episodes);
    }

    /**
     * 获取剧集导航信息
     */
    @GetMapping("/anime/{animeId}/episode/{episodeNumber}/navigation")
    public ResponseEntity<Map<String, Episode>> getEpisodeNavigation(
            @PathVariable Long animeId,
            @PathVariable Integer episodeNumber) {
        
        log.info("获取剧集导航信息 - 动漫ID: {}, 剧集编号: {}", animeId, episodeNumber);
        
        EpisodeService.EpisodeNavigation navigation = episodeService.getEpisodeNavigation(animeId, episodeNumber);
        
        Map<String, Episode> result = new HashMap<>();
        result.put("previous", navigation.getPreviousEpisode());
        result.put("next", navigation.getNextEpisode());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 增加剧集观看次数
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, String>> incrementViewCount(@PathVariable Long id) {
        log.info("增加剧集观看次数 - ID: {}", id);
        
        episodeService.incrementViewCount(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "观看次数已增加");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取动漫剧集统计信息
     */
    @GetMapping("/anime/{animeId}/stats")
    public ResponseEntity<EpisodeService.EpisodeStats> getEpisodeStats(@PathVariable Long animeId) {
        log.info("获取动漫剧集统计信息 - 动漫ID: {}", animeId);
        
        EpisodeService.EpisodeStats stats = episodeService.getEpisodeStatsByAnimeId(animeId);
        return ResponseEntity.ok(stats);
    }
}
