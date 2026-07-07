package com.anime.website.controller;

import com.anime.website.dto.AnimeDTO;
import com.anime.website.entity.Anime;
import com.anime.website.service.AnimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动漫控制器 - 处理动漫相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/anime")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域请求
public class AnimeController {

    private final AnimeService animeService;

    /**
     * 获取动漫列表（分页）
     * GET /api/anime?page=0&size=10&sort=updatedAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<AnimeDTO>> getAllAnime(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("获取动漫列表 - 页码: {}, 大小: {}, 排序: {} {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Anime> animePage = animeService.getAllAnime(pageable);
        Page<AnimeDTO> animeDTOPage = animePage.map(AnimeDTO::fromEntity);
        return ResponseEntity.ok(animeDTOPage);
    }

    /**
     * 根据ID获取动漫详情
     * GET /api/anime/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnimeDTO> getAnimeById(@PathVariable Long id) {
        log.info("获取动漫详情 - ID: {}", id);

        return animeService.getAnimeById(id)
            .map(anime -> {
                // 增加观看次数
                animeService.incrementViewCount(id);
                return ResponseEntity.ok(AnimeDTO.fromEntity(anime));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取热门动漫
     * GET /api/anime/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<List<AnimeDTO>> getPopularAnime(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取热门动漫 - 限制: {}", limit);
        List<Anime> popularAnime = animeService.getPopularAnime(limit);
        List<AnimeDTO> popularAnimeDTO = popularAnime.stream()
            .map(AnimeDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(popularAnimeDTO);
    }
    /**
     * 获取最新动漫
     * GET /api/anime/latest?limit=10
     */
    @GetMapping("/latest")
    public ResponseEntity<List<AnimeDTO>> getLatestAnime(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取最新动漫 - 限制: {}", limit);
        List<Anime> latestAnime = animeService.getLatestAnime(limit);
        List<AnimeDTO> latestAnimeDTO = latestAnime.stream()
            .map(AnimeDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(latestAnimeDTO);
    }

    /**
     * 获取推荐动漫
     * GET /api/anime/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<AnimeDTO>> getFeaturedAnime() {
        log.info("获取推荐动漫");
        List<Anime> featuredAnime = animeService.getFeaturedAnime();
        List<AnimeDTO> featuredAnimeDTO = featuredAnime.stream()
            .map(AnimeDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(featuredAnimeDTO);
    }

    /**
     * 根据状态获取动漫
     * GET /api/anime/status/{status}?page=0&size=12
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Anime>> getAnimeByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("根据状态获取动漫 - 状态: {}", status);

        try {
            Anime.AnimeStatus animeStatus = Anime.AnimeStatus.valueOf(status.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            List<Anime> animeList = animeService.getAnimeByStatus(animeStatus, pageable);
            return ResponseEntity.ok(animeList);
        } catch (IllegalArgumentException e) {
            log.warn("无效的动漫状态: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据分类获取动漫
     * GET /api/anime/category/{categoryId}?page=0&size=12
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Anime>> getAnimeByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("根据分类获取动漫 - 分类ID: {}", categoryId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Anime> animePage = animeService.getAnimeByCategory(categoryId, pageable);
        return ResponseEntity.ok(animePage);
    }

    /**
     * 搜索动漫
     * GET /api/anime/search?keyword=进击&page=0&size=12
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Anime>> searchAnime(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("搜索动漫 - 关键词: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Anime> animePage = animeService.searchAnime(keyword.trim(), pageable);
        return ResponseEntity.ok(animePage);
    }

    /**
     * 根据年份获取动漫
     * GET /api/anime/year/{year}?page=0&size=12
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<Page<Anime>> getAnimeByYear(
            @PathVariable Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("根据年份获取动漫 - 年份: {}", year);

        Pageable pageable = PageRequest.of(page, size);
        Page<Anime> animePage = animeService.getAnimeByYear(year, pageable);
        return ResponseEntity.ok(animePage);
    }

    /**
     * 获取动漫统计信息
     * GET /api/anime/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAnimeStats() {
        log.info("获取动漫统计信息");

        long totalCount = animeService.getTotalAnimeCount();
        long ongoingCount = animeService.getAnimeCountByStatus(Anime.AnimeStatus.ONGOING);
        long completedCount = animeService.getAnimeCountByStatus(Anime.AnimeStatus.COMPLETED);
        long upcomingCount = animeService.getAnimeCountByStatus(Anime.AnimeStatus.UPCOMING);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalCount);
        stats.put("ongoing", ongoingCount);
        stats.put("completed", completedCount);
        stats.put("upcoming", upcomingCount);

        return ResponseEntity.ok(stats);
    }

    /**
     * 增加观看次数（单独的API）
     * POST /api/anime/{id}/view
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        log.info("增加观看次数 - ID: {}", id);

        if (animeService.getAnimeById(id).isPresent()) {
            animeService.incrementViewCount(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
