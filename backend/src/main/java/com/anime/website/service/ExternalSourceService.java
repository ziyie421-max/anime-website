package com.anime.website.service;

import com.anime.website.dto.external.ExternalAnimeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部数据源服务 - 集成多个视频资源API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalSourceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 量子资源API配置 - 只使用量子资源
    private static final String LZZY_API_BASE = "https://cj.lziapi.com/api.php/provide/vod/";
    private static final String SOURCE_KEY = "lzzy";
    private static final String SOURCE_NAME = "🎥┃量子┃资源";

    /**
     * 可用于网页播放的标准 VOD 接口白名单。
     *
     * 仅保留 type=1 且已验证返回标准 JSON 的接口。TVBox 配置中的 JS、Python、JAR
     * 以及 csp_* 插件都不会被读取或执行。
     */
    private static final Map<String, PlaybackSource> PLAYBACK_SOURCES = createPlaybackSources();

    private static Map<String, PlaybackSource> createPlaybackSources() {
        Map<String, PlaybackSource> sources = new LinkedHashMap<>();
        addPlaybackSource(sources, SOURCE_KEY, SOURCE_NAME, LZZY_API_BASE);
        addPlaybackSource(sources, "360zy", "360资源", "https://360zy.com/api.php/provide/vod/");
        addPlaybackSource(sources, "jinying", "金鹰资源", "https://jyzyapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "subo", "速播资源", "https://subocaiji.com/api.php/provide/vod/");
        addPlaybackSource(sources, "yinghua", "樱花资源", "https://m3u8.apiyhzy.com/api.php/provide/vod/");
        addPlaybackSource(sources, "dytt", "电影天堂", "http://caiji.dyttzyapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "ffzy", "非凡资源", "http://cj.ffzyapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "hhzy", "豪华资源", "https://hhzyapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "suoni", "索尼资源", "https://suoniapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "ikun", "爱坤资源", "https://ikunzyapi.com/api.php/provide/vod/from/ikm3u8/at/json");
        addPlaybackSource(sources, "guangsu", "光速资源", "https://api.guangsuapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "modu", "魔都资源", "https://www.moduzy.com/api.php/provide/vod/");
        addPlaybackSource(sources, "hongniu", "红牛资源", "https://www.hongniuzy2.com/api.php/provide/vod/");
        addPlaybackSource(sources, "baidu", "百度资源", "https://api.apibdzy.com/api.php/provide/vod/");
        addPlaybackSource(sources, "jisu", "极速资源", "https://jszyapi.com/api.php/provide/vod/at/json");
        addPlaybackSource(sources, "huya", "虎牙资源", "https://www.huyaapi.com/api.php/provide/vod/");
        addPlaybackSource(sources, "xinlang", "新浪资源", "https://api.xinlangapi.com/xinlangapi.php/provide/vod/");
        addPlaybackSource(sources, "1080zy", "1080资源", "https://api.1080zyku.com/inc/apijson.php");
        return sources;
    }

    private static void addPlaybackSource(Map<String, PlaybackSource> sources, String key, String name, String apiUrl) {
        sources.put(key, new PlaybackSource(key, name, apiUrl));
    }

    // 动漫分类映射 - 使用索尼资源的正确动漫分类ID
    private static final Map<String, String> ANIME_CATEGORIES = new HashMap<>();

    static {
        // 动漫分类配置 - 对应索尼资源API的动漫分类
        ANIME_CATEGORIES.put("日本动漫", "30");      // 日韩动漫
        ANIME_CATEGORIES.put("日韩动漫", "30");      // 日韩动漫
        ANIME_CATEGORIES.put("中国动漫", "29");      // 国产动漫
        ANIME_CATEGORIES.put("国产动漫", "29");      // 国产动漫
        ANIME_CATEGORIES.put("欧美动漫", "31");      // 欧美动漫
        ANIME_CATEGORIES.put("动画片", "39");        // 动画片
        ANIME_CATEGORIES.put("港台动漫", "44");      // 港台动漫
        ANIME_CATEGORIES.put("海外动漫", "45");      // 海外动漫
        ANIME_CATEGORIES.put("有声动漫", "63");      // 有声动漫
        ANIME_CATEGORIES.put("动漫", "4");           // 通用动漫分类
    }

    /**
     * 获取动漫列表 - 使用量子资源
     */
    public ExternalAnimeResponse getAnimeList(String category, int page, String keyword) {
        return getAnimeList(category, page, keyword, 20); // 默认20条，保持向后兼容
    }

    /**
     * 获取动漫列表 - 使用量子资源，支持自定义数量
     */
    public ExternalAnimeResponse getAnimeList(String category, int page, String keyword, int limit) {
        log.info("获取外部动漫列表 - 分类: {}, 页码: {}, 关键词: {}, 限制: {}", category, page, keyword, limit);

        log.info("从量子资源获取数据: {}", SOURCE_NAME);

        ExternalAnimeResponse response = getAnimeListFromSource(LZZY_API_BASE, SOURCE_KEY, category, page, keyword);
        if (response != null && response.getCode() == 1 && response.getList() != null && !response.getList().isEmpty()) {
            // 为每个动漫项调用详情API获取完整信息（包括封面和简介）
            List<ExternalAnimeResponse.ExternalAnimeItem> enrichedList = new ArrayList<>();

            // 限制处理的数量，提高性能
            List<ExternalAnimeResponse.ExternalAnimeItem> itemsToProcess = response.getList();
            if (itemsToProcess.size() > limit) {
                itemsToProcess = itemsToProcess.subList(0, limit);
            }

            for (ExternalAnimeResponse.ExternalAnimeItem item : itemsToProcess) {
                // 调用详情API获取完整信息
                ExternalAnimeResponse detailResponse = getAnimeDetailFromSource(LZZY_API_BASE, SOURCE_KEY, item.getVodId().toString());

                if (detailResponse != null && detailResponse.getList() != null && !detailResponse.getList().isEmpty()) {
                    ExternalAnimeResponse.ExternalAnimeItem detailItem = detailResponse.getList().get(0);
                    // 使用详情API的完整数据
                    detailItem.setSourceKey(SOURCE_KEY);
                    detailItem.setSourceName(SOURCE_NAME);
                    enrichedList.add(detailItem);
                    log.debug("成功获取动漫详情: {} - 封面: {}", detailItem.getVodName(),
                             detailItem.getVodPic() != null ? "有封面" : "无封面");
                } else {
                    // 如果详情API失败，使用原始数据
                    item.setSourceKey(SOURCE_KEY);
                    item.setSourceName(SOURCE_NAME);
                    enrichedList.add(item);
                    log.debug("详情API失败，使用原始数据: {}", item.getVodName());
                }
            }

            response.setList(enrichedList);
            log.info("成功从量子资源获取数据 - 共{}条（已补充封面和简介，限制{}条）", response.getList().size(), limit);
            return response;
        }

        log.warn("量子资源无法获取数据");
        return createEmptyResponse();
    }

    /**
     * 从指定资源源获取动漫列表
     */
    private ExternalAnimeResponse getAnimeListFromSource(String apiUrl, String sourceKey, String category, int page, String keyword) {
        // 重试3次
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                        .queryParam("ac", "list");

                // 添加分类参数
                if (category != null && ANIME_CATEGORIES.containsKey(category)) {
                    String categoryId = ANIME_CATEGORIES.get(category);
                    builder.queryParam("t", categoryId);
                } else {
                    // 默认获取日韩动漫
                    builder.queryParam("t", "30");
                }

                // 添加页码参数
                builder.queryParam("pg", page);

                // 添加搜索关键词
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String encodedKeyword = URLEncoder.encode(keyword.trim(), "UTF-8");
                    builder.queryParam("wd", encodedKeyword);
                }

                String url = builder.toUriString();
                log.debug("请求URL (尝试 {}/3): {}", attempt, url);

                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                headers.set("Accept", "application/json, text/plain, */*");
                headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // 发送请求
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    ExternalAnimeResponse result = objectMapper.readValue(response.getBody(), ExternalAnimeResponse.class);
                    log.info("成功获取外部动漫数据 (尝试 {}/3) - 总数: {}, 页数: {}", attempt, result.getTotal(), result.getPagecount());
                    return result;
                } else {
                    log.warn("外部API请求失败 (尝试 {}/3) - 状态码: {}", attempt, response.getStatusCode());
                }

            } catch (Exception e) {
                log.warn("获取外部动漫列表失败 (尝试 {}/3): {}", attempt, e.getMessage());

                if (attempt < 3) {
                    try {
                        // 等待1秒后重试
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("所有重试都失败，返回空响应");
        return createEmptyResponse();
    }

    /**
     * 获取动漫详情 - 使用索尼资源
     */
    public ExternalAnimeResponse getAnimeDetail(String vodId) {
        log.info("获取外部动漫详情 - ID: {}", vodId);

        log.info("从量子资源获取详情: {}", SOURCE_NAME);

        ExternalAnimeResponse response = getAnimeDetailFromSource(LZZY_API_BASE, SOURCE_KEY, vodId);
        if (response != null && response.getCode() == 1 && response.getList() != null && !response.getList().isEmpty()) {
            // 为动漫项添加资源源信息
            response.getList().forEach(item -> {
                item.setSourceKey(SOURCE_KEY);
                item.setSourceName(SOURCE_NAME);
            });
            log.info("成功从量子资源获取详情");
            return response;
        }

        log.warn("量子资源无法获取详情");
        return createEmptyResponse();
    }

    /**
     * 获取指定播放源的剧集。备用源使用原标题检索，因为各站的 vodId 并不通用。
     */
    public ExternalAnimeResponse getAnimeForPlayback(String vodId, String sourceKey) {
        if (sourceKey == null || SOURCE_KEY.equals(sourceKey)) {
            return getAnimeDetail(vodId);
        }

        PlaybackSource source = PLAYBACK_SOURCES.get(sourceKey);
        if (source == null) {
            log.warn("拒绝未在白名单中的播放源: {}", sourceKey);
            return createEmptyResponse();
        }

        ExternalAnimeResponse primaryResponse = getAnimeDetail(vodId);
        if (primaryResponse.getCode() != 1 || primaryResponse.getList() == null || primaryResponse.getList().isEmpty()) {
            return primaryResponse;
        }

        String title = primaryResponse.getList().get(0).getVodName();
        if (title == null || title.trim().isEmpty()) {
            return createEmptyResponse();
        }

        return getAnimeDetailByTitle(source, title);
    }

    /**
     * 从指定资源源获取动漫详情
     */
    private ExternalAnimeResponse getAnimeDetailFromSource(String apiUrl, String sourceKey, String vodId) {
        // 重试3次
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                        .queryParam("ac", "detail")
                        .queryParam("ids", vodId)
                        .toUriString();

                log.debug("请求URL (尝试 {}/3): {}", attempt, url);

                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                headers.set("Accept", "application/json, text/plain, */*");
                headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // 发送请求
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    ExternalAnimeResponse result = objectMapper.readValue(response.getBody(), ExternalAnimeResponse.class);
                    log.info("成功获取外部动漫详情 (尝试 {}/3) - ID: {}", attempt, vodId);
                    return result;
                } else {
                    log.warn("外部API详情请求失败 (尝试 {}/3) - 状态码: {}", attempt, response.getStatusCode());
                }

            } catch (Exception e) {
                log.warn("获取外部动漫详情失败 (尝试 {}/3) - ID: {}: {}", attempt, vodId, e.getMessage());

                if (attempt < 3) {
                    try {
                        // 等待2秒后重试
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("获取动漫详情的所有重试都失败 - ID: {}", vodId);
        return createEmptyResponse();
    }

    /**
     * 通过标题从标准 MacCMS/VOD 接口获取详情。只接受服务端定义的白名单源。
     */
    private ExternalAnimeResponse getAnimeDetailByTitle(PlaybackSource source, String title) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                String url = UriComponentsBuilder.fromHttpUrl(source.apiUrl)
                        .queryParam("ac", "detail")
                        .queryParam("wd", title.trim())
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                headers.set("Accept", "application/json, text/plain, */*");
                headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    continue;
                }

                ExternalAnimeResponse result = objectMapper.readValue(response.getBody(), ExternalAnimeResponse.class);
                ExternalAnimeResponse.ExternalAnimeItem matched = findBestTitleMatch(result.getList(), title);
                if (result.getCode() == 1 && matched != null) {
                    matched.setSourceKey(source.key);
                    matched.setSourceName(source.name);
                    List<ExternalAnimeResponse.ExternalAnimeItem> singleResult = new ArrayList<>();
                    singleResult.add(matched);
                    result.setList(singleResult);
                    return result;
                }
            } catch (Exception e) {
                log.warn("备用播放源查询失败 ({}/2) - {}: {}", attempt, source.key, e.getMessage());
            }
        }

        log.info("备用播放源未找到同名条目 - {}: {}", source.key, title);
        return createEmptyResponse();
    }

    private ExternalAnimeResponse.ExternalAnimeItem findBestTitleMatch(
            List<ExternalAnimeResponse.ExternalAnimeItem> items, String title) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        String expected = normalizeTitle(title);
        for (ExternalAnimeResponse.ExternalAnimeItem item : items) {
            if (expected.equals(normalizeTitle(item.getVodName()))) {
                return item;
            }
        }
        for (ExternalAnimeResponse.ExternalAnimeItem item : items) {
            String candidate = normalizeTitle(item.getVodName());
            if (!candidate.isEmpty() && (candidate.contains(expected) || expected.contains(candidate))) {
                return item;
            }
        }
        return null;
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.toLowerCase().replaceAll("[\\p{P}\\p{S}\\s]+", "");
    }

    /**
     * 搜索动漫
     */
    public ExternalAnimeResponse searchAnime(String keyword, int page) {
        log.info("搜索外部动漫 - 关键词: {}, 页码: {}", keyword, page);
        return getAnimeList(null, page, keyword);
    }

    /**
     * 获取热门动漫 - 从量子资源获取
     */
    public ExternalAnimeResponse getPopularAnime(int page, int limit) {
        log.info("获取热门动漫 - 页码: {}, 限制: {}", page, limit);

        // 从量子资源获取日韩动漫列表，限制数量提高性能
        ExternalAnimeResponse response = getAnimeList("日韩动漫", page, null, limit);

        // 按观看次数排序
        if (response != null && response.getList() != null) {
            response.getList().sort((a, b) -> {
                Integer hitsA = a.getVodHits() != null ? a.getVodHits() : 0;
                Integer hitsB = b.getVodHits() != null ? b.getVodHits() : 0;
                return hitsB.compareTo(hitsA); // 降序排列
            });

            // 限制返回数量
            if (response.getList().size() > limit) {
                response.setList(response.getList().subList(0, limit));
            }
        }

        return response;
    }

    /**
     * 解析播放链接
     */
    public Map<String, List<Map<String, String>>> parsePlayUrls(String vodPlayUrl, String vodPlayFrom) {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        
        if (vodPlayUrl == null || vodPlayFrom == null) {
            return result;
        }
        
        try {
            String[] sources = vodPlayUrl.split("\\$\\$\\$");
            String[] sourceNames = vodPlayFrom.split("\\$\\$\\$");
            
            for (int i = 0; i < sources.length && i < sourceNames.length; i++) {
                String sourceName = sourceNames[i];
                String sourceUrls = sources[i];
                
                List<Map<String, String>> episodes = new ArrayList<>();
                String[] episodeUrls = sourceUrls.split("#");
                
                for (String episodeUrl : episodeUrls) {
                    if (episodeUrl.contains("$")) {
                        String[] parts = episodeUrl.split("\\$", 2);
                        if (parts.length == 2) {
                            Map<String, String> episode = new HashMap<>();
                            episode.put("name", parts[0]);
                            episode.put("url", parts[1]);
                            episodes.add(episode);
                        }
                    }
                }
                
                result.put(sourceName, episodes);
            }
            
        } catch (Exception e) {
            log.error("解析播放链接失败", e);
        }
        
        return result;
    }

    /**
     * 获取支持的动漫分类
     */
    public Map<String, String> getSupportedCategories() {
        return new HashMap<>(ANIME_CATEGORIES);
    }

    /**
     * 获取支持的播放源列表。
     */
    public Map<String, String> getSupportedSources() {
        Map<String, String> sources = new LinkedHashMap<>();
        for (PlaybackSource source : PLAYBACK_SOURCES.values()) {
            sources.put(source.key, source.name);
        }
        return sources;
    }

    private static class PlaybackSource {
        private final String key;
        private final String name;
        private final String apiUrl;

        private PlaybackSource(String key, String name, String apiUrl) {
            this.key = key;
            this.name = name;
            this.apiUrl = apiUrl;
        }
    }

    /**
     * 创建空响应
     */
    private ExternalAnimeResponse createEmptyResponse() {
        ExternalAnimeResponse response = new ExternalAnimeResponse();
        response.setCode(0);
        response.setMsg("获取数据失败");
        response.setTotal(0);
        response.setPagecount(0);
        response.setList(new ArrayList<>());
        return response;
    }


}
