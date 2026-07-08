package com.anime.website.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

/**
 * 视频代理控制器 - 解决外部视频 CDN 对用户本地 IP（VPN/校园网/企业网）的屏蔽/限速
 *
 * 设计：
 *   - 前端先直连视频 CDN；若失败（被屏蔽/超时），自动回退到本代理
 *   - 后端在美国机房（Render），出口 IP 通常不被视频 CDN 屏蔽
 *   - 仅做域名白名单校验（防 SSRF），不强制签名（简化前端逻辑）
 *
 * 调用方式：
 *   GET /api/video/proxy?url={Base64编码的视频URL}
 */
@Slf4j
@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "*")
public class VideoProxyController {

    /**
     * 允许代理的视频 CDN 域名关键字白名单
     * URL 中包含任一关键字即放行（宽松策略，避免误拦合法视频源）
     */
    private static final Set<String> ALLOWED_KEYWORDS = Set.of(
            "m3u8", "mp4", "flv", "avi", "mkv", "mov", "webm",
            "vod", "cache", "cdn", "video", "stream", "hls",
            "aliyuncs", "myqcloud", "bcebos", "ksyuncs", "qiniu",
            "lzcdn", "lziapi", "bfn", "bfncdn", "tiankong", "cdnzz",
            "m3u8s", "m3u8cache", "vod1", "vod2", "vod3",
            "play", "play1", "play2", "play3"
    );

    /**
     * 明确禁止的域名（内网/私有地址）
     */
    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
            "localhost", "127.", "192.168.", "10.", "172.16.", "169.254.",
            "0.0.0.0", "::1", "metadata.google", "internal"
    );

    /**
     * 视频流代理接口
     *
     * @param encodedUrl Base64 编码的视频 URL（前端用 base64 编码避免特殊字符问题）
     */
    @GetMapping(value = "/proxy", produces = {
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "application/x-mpegURL",
            "video/mp2t",
            "video/mp4",
            "application/vnd.apple.mpegurl"
    })
    public ResponseEntity<StreamingResponseBody> proxyVideo(
            @RequestParam("url") String encodedUrl
    ) {
        try {
            // 1. 解码 URL
            String videoUrl;
            try {
                videoUrl = new String(Base64.getUrlDecoder().decode(encodedUrl), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("视频代理 URL 解码失败");
                return ResponseEntity.badRequest().body(null);
            }

            if (videoUrl.isBlank() || (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://"))) {
                log.warn("视频代理 URL 格式非法");
                return ResponseEntity.badRequest().body(null);
            }

            // 2. 安全校验：禁止内网地址
            String lowerUrl = videoUrl.toLowerCase();
            for (String blocked : BLOCKED_KEYWORDS) {
                if (lowerUrl.contains(blocked)) {
                    log.warn("视频代理命中黑名单 | url={}", videoUrl.substring(0, Math.min(50, videoUrl.length())));
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            }

            // 3. 安全校验：域名白名单
            if (ALLOWED_KEYWORDS.stream().noneMatch(lowerUrl::contains)) {
                log.warn("视频域名不在白名单 | url={}", videoUrl.substring(0, Math.min(50, videoUrl.length())));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // 4. 流式代理
            log.info("视频代理开始流 | url={}", videoUrl.substring(0, Math.min(80, videoUrl.length())));

            URL url = URI.create(videoUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setRequestProperty("Referer", "https://www.baidu.com/");
            conn.setRequestProperty("Accept", "*/*");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.warn("视频源返回非200 | code={} | url={}", responseCode, videoUrl.substring(0, Math.min(50, videoUrl.length())));
                conn.disconnect();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
            }

            String contentType = conn.getContentType() != null ? conn.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            long contentLength = conn.getContentLengthLong();

            final HttpURLConnection finalConn = conn;
            StreamingResponseBody responseBody = out -> {
                try (InputStream in = finalConn.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        out.flush();
                    }
                } catch (IOException e) {
                    log.warn("视频流传输中断 | {}", e.getMessage());
                } finally {
                    finalConn.disconnect();
                }
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            if (contentLength > 0) {
                headers.setContentLength(contentLength);
            }
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Expose-Headers", "Content-Length,Content-Type");
            // m3u8 不缓存（实时更新），ts 段可缓存
            if (videoUrl.contains(".m3u8")) {
                headers.setCacheControl("no-cache");
            } else {
                headers.setCacheControl("public, max-age=3600");
            }

            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("视频代理异常 | {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 健康检查：前端可调用此接口测试代理是否可用
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("video-proxy-ok");
    }
}
