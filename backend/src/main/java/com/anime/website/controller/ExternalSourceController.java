package com.anime.website.controller;

import com.anime.website.dto.external.ExternalAnimeResponse;
import com.anime.website.service.ExternalSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部数据源控制器 - 提供外部动漫数据API
 */
@Slf4j
@RestController
@RequestMapping("/external")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExternalSourceController {

    private final ExternalSourceService externalSourceService;

    /**
     * 获取外部动漫列表
     */
    @GetMapping("/anime/list")
    public ResponseEntity<?> getAnimeList(
            @RequestParam(defaultValue = "日本动漫") String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        log.info("获取外部动漫列表 - 分类: {}, 页码: {}, 关键词: {}", category, page, keyword);
        
        try {
            // 修改为每页12条数据，配合4列3行布局
            ExternalAnimeResponse response = externalSourceService.getAnimeList(category, page, keyword, 12);
            
            if (response != null && response.getCode() == 1) {
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "获取数据失败");
                error.put("message", response != null ? response.getMsg() : "外部API响应异常");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            log.error("获取外部动漫列表失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "服务器错误");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取外部动漫详情
     */
    @GetMapping("/anime/detail/{vodId}")
    public ResponseEntity<?> getAnimeDetail(@PathVariable String vodId) {
        log.info("获取外部动漫详情 - ID: {}", vodId);
        
        try {
            ExternalAnimeResponse response = externalSourceService.getAnimeDetail(vodId);
            
            if (response != null && response.getCode() == 1 && 
                response.getList() != null && !response.getList().isEmpty()) {
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "动漫不存在");
                error.put("message", "未找到指定的动漫详情");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("获取外部动漫详情失败 - ID: {}", vodId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "服务器错误");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 搜索外部动漫
     */
    @GetMapping("/anime/search")
    public ResponseEntity<?> searchAnime(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        log.info("搜索外部动漫 - 关键词: {}, 页码: {}", keyword, page);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "参数错误");
            error.put("message", "搜索关键词不能为空");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            ExternalAnimeResponse response = externalSourceService.searchAnime(keyword, page);
            
            if (response != null && response.getCode() == 1) {
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "搜索失败");
                error.put("message", response != null ? response.getMsg() : "外部API响应异常");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            log.error("搜索外部动漫失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "服务器错误");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 解析播放链接
     */
    @GetMapping("/anime/play/{vodId}")
    public ResponseEntity<?> getPlayUrls(@PathVariable String vodId) {
        log.info("获取播放链接 - ID: {}", vodId);
        
        try {
            // 先获取动漫详情
            ExternalAnimeResponse response = externalSourceService.getAnimeDetail(vodId);
            
            if (response != null && response.getCode() == 1 && 
                response.getList() != null && !response.getList().isEmpty()) {
                
                ExternalAnimeResponse.ExternalAnimeItem anime = response.getList().get(0);
                
                // 解析播放链接
                Map<String, List<Map<String, String>>> playUrls = 
                    externalSourceService.parsePlayUrls(anime.getVodPlayUrl(), anime.getVodPlayFrom());
                
                Map<String, Object> result = new HashMap<>();
                result.put("vodId", vodId);
                result.put("title", anime.getVodName());
                result.put("cover", anime.getVodPic());  // 添加封面图片字段
                result.put("playUrls", playUrls);
                result.put("totalEpisodes", anime.getVodTotal());

                return ResponseEntity.ok(result);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "动漫不存在");
                error.put("message", "未找到指定的动漫播放信息");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("获取播放链接失败 - ID: {}", vodId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "服务器错误");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取热门动漫
     */
    @GetMapping("/anime/popular")
    public ResponseEntity<?> getPopularAnime(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        log.info("获取热门动漫 - 页码: {}, 限制: {}", page, limit);
        
        try {
            // 使用专门的热门动漫方法，优先展示索尼资源
            ExternalAnimeResponse response = externalSourceService.getPopularAnime(page, limit);

            if (response != null && response.getCode() == 1) {
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "获取数据失败");
                error.put("message", response != null ? response.getMsg() : "外部API响应异常");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            log.error("获取热门动漫失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "服务器错误");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取支持的分类
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, String>> getCategories() {
        log.info("获取支持的动漫分类");

        try {
            Map<String, String> categories = externalSourceService.getSupportedCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("获取分类失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取支持的资源源列表
     */
    @GetMapping("/sources")
    public ResponseEntity<Map<String, String>> getSources() {
        log.info("获取支持的资源源列表");

        try {
            Map<String, String> sources = externalSourceService.getSupportedSources();
            return ResponseEntity.ok(sources);
        } catch (Exception e) {
            log.error("获取资源源列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 健康检查和网络诊断
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "external-source");
        status.put("timestamp", System.currentTimeMillis());

        try {
            // 测试外部API连接
            long startTime = System.currentTimeMillis();
            ExternalAnimeResponse testResponse = externalSourceService.getAnimeList("日本动漫", 1, null);
            long responseTime = System.currentTimeMillis() - startTime;

            if (testResponse != null && testResponse.getCode() == 1) {
                status.put("status", "healthy");
                status.put("external_api", "connected");
                status.put("response_time_ms", responseTime);
                status.put("total_anime", testResponse.getTotal());
            } else {
                status.put("status", "degraded");
                status.put("external_api", "failed");
                status.put("response_time_ms", responseTime);
                status.put("error", "API返回错误响应");
            }

        } catch (Exception e) {
            status.put("status", "unhealthy");
            status.put("external_api", "error");
            status.put("error", e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    /**
     * 网络连接测试
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("开始测试外部API连接...");

            // 测试列表API
            long startTime = System.currentTimeMillis();
            ExternalAnimeResponse listResponse = externalSourceService.getAnimeList("日本动漫", 1, null);
            long listTime = System.currentTimeMillis() - startTime;

            Map<String, Object> listApiResult = new HashMap<>();
            listApiResult.put("success", listResponse != null && listResponse.getCode() == 1);
            listApiResult.put("response_time_ms", listTime);
            listApiResult.put("total_anime", listResponse != null ? listResponse.getTotal() : 0);
            result.put("list_api", listApiResult);

            // 如果列表API成功，测试详情API
            if (listResponse != null && listResponse.getCode() == 1 &&
                listResponse.getList() != null && !listResponse.getList().isEmpty()) {

                String testId = String.valueOf(listResponse.getList().get(0).getVodId());
                startTime = System.currentTimeMillis();
                ExternalAnimeResponse detailResponse = externalSourceService.getAnimeDetail(testId);
                long detailTime = System.currentTimeMillis() - startTime;

                Map<String, Object> detailApiResult = new HashMap<>();
                detailApiResult.put("success", detailResponse != null && detailResponse.getCode() == 1);
                detailApiResult.put("response_time_ms", detailTime);
                detailApiResult.put("test_id", testId);
                result.put("detail_api", detailApiResult);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("连接测试失败", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
