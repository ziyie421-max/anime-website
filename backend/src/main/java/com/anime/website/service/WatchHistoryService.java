package com.anime.website.service;

import com.anime.website.dto.history.AddWatchHistoryRequest;
import com.anime.website.dto.history.WatchHistoryDTO;
import com.anime.website.entity.Anime;
import com.anime.website.entity.Episode;
import com.anime.website.entity.User;
import com.anime.website.entity.UserWatchHistory;
import com.anime.website.repository.AnimeRepository;
import com.anime.website.repository.EpisodeRepository;
import com.anime.website.repository.UserRepository;
import com.anime.website.repository.UserWatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 观看历史服务 - 处理用户观看历史相关业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchHistoryService {

    private final UserWatchHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;

    /**
     * 添加或更新观看历史
     */
    @Transactional
    public WatchHistoryDTO addOrUpdateHistory(Long userId, AddWatchHistoryRequest request) {
        log.info("添加/更新观看历史 - 用户ID: {}, 外部动漫ID: {}, 剧集索引: {}", 
                userId, request.getExternalAnimeId(), request.getExternalEpisodeIndex());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserWatchHistory history;
        
        if (request.isExternalAnime()) {
            // 查找是否已有该剧集的观看记录
            Optional<UserWatchHistory> existingHistory = historyRepository
                    .findByUserIdAndExternalAnimeIdAndExternalEpisodeIndex(
                            userId, request.getExternalAnimeId(), request.getExternalEpisodeIndex());
            
            if (existingHistory.isPresent()) {
                // 更新现有记录
                history = existingHistory.get();
                history.setWatchProgress(request.getWatchProgress() != null ? request.getWatchProgress() : history.getWatchProgress());
                history.setTotalDuration(request.getTotalDuration() != null ? request.getTotalDuration() : history.getTotalDuration());
                history.setLastWatchTime(LocalDateTime.now());
                history.setWatchCount(history.getWatchCount() + 1);
            } else {
                // 创建新记录
                history = UserWatchHistory.builder()
                        .user(user)
                        .externalAnimeId(request.getExternalAnimeId())
                        .externalAnimeTitle(request.getExternalAnimeTitle())
                        .externalAnimeCover(request.getExternalAnimeCover())
                        .externalEpisodeName(request.getExternalEpisodeName())
                        .externalEpisodeIndex(request.getExternalEpisodeIndex())
                        .sourceKey(request.getSourceKey())
                        .sourceName(request.getSourceName())
                        .watchProgress(request.getWatchProgress() != null ? request.getWatchProgress() : 0)
                        .totalDuration(request.getTotalDuration() != null ? request.getTotalDuration() : 0)
                        .lastWatchTime(LocalDateTime.now())
                        .watchCount(1)
                        .build();
            }
        } else {
            // 本地动漫处理
            Optional<UserWatchHistory> existingHistory = historyRepository
                    .findByUserIdAndEpisodeId(userId, request.getEpisodeId());
            
            if (existingHistory.isPresent()) {
                history = existingHistory.get();
                history.setWatchProgress(request.getWatchProgress() != null ? request.getWatchProgress() : history.getWatchProgress());
                history.setTotalDuration(request.getTotalDuration() != null ? request.getTotalDuration() : history.getTotalDuration());
                history.setLastWatchTime(LocalDateTime.now());
                history.setWatchCount(history.getWatchCount() + 1);
            } else {
                Anime anime = animeRepository.findById(request.getAnimeId())
                        .orElseThrow(() -> new RuntimeException("动漫不存在"));
                Episode episode = request.getEpisodeId() != null 
                        ? episodeRepository.findById(request.getEpisodeId()).orElse(null) 
                        : null;
                
                history = UserWatchHistory.builder()
                        .user(user)
                        .anime(anime)
                        .episode(episode)
                        .watchProgress(request.getWatchProgress() != null ? request.getWatchProgress() : 0)
                        .totalDuration(request.getTotalDuration() != null ? request.getTotalDuration() : 0)
                        .lastWatchTime(LocalDateTime.now())
                        .watchCount(1)
                        .build();
            }
        }
        
        history = historyRepository.save(history);
        log.info("观看历史已保存 - 记录ID: {}", history.getId());
        
        return WatchHistoryDTO.fromEntity(history);
    }

    /**
     * 更新观看进度
     */
    @Transactional
    public void updateProgress(Long userId, Long historyId, Integer progress, Integer totalDuration) {
        log.info("更新观看进度 - 用户ID: {}, 记录ID: {}, 进度: {}s", userId, historyId, progress);
        
        UserWatchHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("观看记录不存在"));
        
        // 验证记录属于当前用户
        if (!history.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }
        
        history.setWatchProgress(progress);
        if (totalDuration != null) {
            history.setTotalDuration(totalDuration);
        }
        history.setLastWatchTime(LocalDateTime.now());
        
        historyRepository.save(history);
        log.info("观看进度已更新");
    }

    /**
     * 获取用户观看历史（分页）
     */
    public Page<WatchHistoryDTO> getWatchHistory(Long userId, int page, int size) {
        log.info("获取用户观看历史 - 用户ID: {}, 页码: {}, 大小: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserWatchHistory> histories = historyRepository.findByUserIdOrderByLastWatchTimeDesc(userId, pageable);
        
        return histories.map(WatchHistoryDTO::fromEntity);
    }

    /**
     * 获取用户最近观看的动漫列表（按动漫分组）
     */
    public List<WatchHistoryDTO> getRecentWatchedAnime(Long userId, int limit) {
        log.info("获取用户最近观看的动漫 - 用户ID: {}, 限制: {}", userId, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<UserWatchHistory> histories = historyRepository.findDistinctExternalAnimeByUserId(userId, pageable);
        
        return histories.stream()
                .map(WatchHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取继续观看列表
     */
    public List<WatchHistoryDTO> getContinueWatching(Long userId, int limit) {
        log.info("获取继续观看列表 - 用户ID: {}, 限制: {}", userId, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<UserWatchHistory> histories = historyRepository.findContinueWatchingByUserId(userId, pageable);
        
        return histories.stream()
                .map(WatchHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 删除单条观看记录
     */
    @Transactional
    public void deleteHistory(Long userId, Long historyId) {
        log.info("删除观看记录 - 用户ID: {}, 记录ID: {}", userId, historyId);
        
        UserWatchHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("观看记录不存在"));
        
        // 验证记录属于当前用户
        if (!history.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }
        
        historyRepository.delete(history);
        log.info("观看记录已删除");
    }

    /**
     * 清空用户所有观看历史
     */
    @Transactional
    public void clearAllHistory(Long userId) {
        log.info("清空用户所有观看历史 - 用户ID: {}", userId);
        historyRepository.deleteByUserId(userId);
        log.info("用户观看历史已清空");
    }

    /**
     * 获取观看历史数量
     */
    public long getHistoryCount(Long userId) {
        return historyRepository.countByUserId(userId);
    }

    /**
     * 获取某部动漫的观看记录
     */
    public WatchHistoryDTO getAnimeWatchHistory(Long userId, String externalAnimeId) {
        log.info("获取动漫观看记录 - 用户ID: {}, 外部动漫ID: {}", userId, externalAnimeId);
        
        Optional<UserWatchHistory> history = historyRepository.findByUserIdAndExternalAnimeId(userId, externalAnimeId);
        return history.map(WatchHistoryDTO::fromEntity).orElse(null);
    }
}

