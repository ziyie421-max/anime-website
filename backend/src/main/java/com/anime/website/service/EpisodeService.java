package com.anime.website.service;

import com.anime.website.entity.Episode;
import com.anime.website.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 剧集业务逻辑层
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    /**
     * 根据动漫ID获取剧集列表
     */
    public List<Episode> getEpisodesByAnimeId(Long animeId) {
        log.debug("获取动漫剧集列表，动漫ID: {}", animeId);
        return episodeRepository.findByAnimeIdOrderByEpisodeNumberAsc(animeId);
    }

    /**
     * 根据动漫ID获取已发布的剧集列表
     */
    public List<Episode> getPublishedEpisodesByAnimeId(Long animeId) {
        log.debug("获取动漫已发布剧集列表，动漫ID: {}", animeId);
        return episodeRepository.findPublishedByAnimeIdOrderByEpisodeNumberAsc(animeId);
    }

    /**
     * 分页获取动漫剧集列表
     */
    public Page<Episode> getEpisodesByAnimeId(Long animeId, int page, int size) {
        log.debug("分页获取动漫剧集列表，动漫ID: {}, 页码: {}, 大小: {}", animeId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return episodeRepository.findByAnimeIdOrderByEpisodeNumberAsc(animeId, pageable);
    }

    /**
     * 分页获取动漫已发布剧集列表
     */
    public Page<Episode> getPublishedEpisodesByAnimeId(Long animeId, int page, int size) {
        log.debug("分页获取动漫已发布剧集列表，动漫ID: {}, 页码: {}, 大小: {}", animeId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return episodeRepository.findPublishedByAnimeIdOrderByEpisodeNumberAsc(animeId, pageable);
    }

    /**
     * 根据ID获取剧集详情
     */
    public Optional<Episode> getEpisodeById(Long id) {
        log.debug("获取剧集详情，ID: {}", id);
        return episodeRepository.findById(id);
    }

    /**
     * 根据动漫ID和剧集编号获取剧集
     */
    public Optional<Episode> getEpisodeByAnimeIdAndNumber(Long animeId, Integer episodeNumber) {
        log.debug("根据动漫ID和剧集编号获取剧集，动漫ID: {}, 剧集编号: {}", animeId, episodeNumber);
        return episodeRepository.findByAnimeIdAndEpisodeNumber(animeId, episodeNumber);
    }

    /**
     * 获取动漫的最新剧集
     */
    public List<Episode> getLatestEpisodesByAnimeId(Long animeId, int limit) {
        log.debug("获取动漫最新剧集，动漫ID: {}, 限制数量: {}", animeId, limit);
        Pageable pageable = PageRequest.of(0, limit);
        return episodeRepository.findLatestByAnimeId(animeId, pageable);
    }

    /**
     * 获取动漫的热门剧集
     */
    public List<Episode> getPopularEpisodesByAnimeId(Long animeId, int limit) {
        log.debug("获取动漫热门剧集，动漫ID: {}, 限制数量: {}", animeId, limit);
        Pageable pageable = PageRequest.of(0, limit);
        return episodeRepository.findPopularByAnimeId(animeId, pageable);
    }

    /**
     * 搜索动漫剧集
     */
    public List<Episode> searchEpisodesByAnimeIdAndKeyword(Long animeId, String keyword) {
        log.debug("搜索动漫剧集，动漫ID: {}, 关键词: {}", animeId, keyword);
        return episodeRepository.searchByAnimeIdAndKeyword(animeId, keyword);
    }

    /**
     * 获取剧集导航信息（上一集、下一集）
     */
    public EpisodeNavigation getEpisodeNavigation(Long animeId, Integer episodeNumber) {
        log.debug("获取剧集导航信息，动漫ID: {}, 剧集编号: {}", animeId, episodeNumber);
        
        // 获取上一集
        Optional<Episode> previousEpisode = Optional.empty();
        if (episodeNumber > 1) {
            previousEpisode = episodeRepository.findByAnimeIdAndEpisodeNumber(animeId, episodeNumber - 1);
        }
        
        // 获取下一集
        Optional<Episode> nextEpisode = episodeRepository.findByAnimeIdAndEpisodeNumber(animeId, episodeNumber + 1);
        
        return new EpisodeNavigation(previousEpisode.orElse(null), nextEpisode.orElse(null));
    }

    /**
     * 增加剧集观看次数
     */
    @Transactional
    public void incrementViewCount(Long episodeId) {
        log.debug("增加剧集观看次数，ID: {}", episodeId);
        episodeRepository.findById(episodeId).ifPresent(episode -> {
            episode.setViewCount((episode.getViewCount() == null ? 0 : episode.getViewCount()) + 1);
            episodeRepository.save(episode);
        });
    }

    /**
     * 获取动漫剧集统计信息
     */
    public EpisodeStats getEpisodeStatsByAnimeId(Long animeId) {
        log.debug("获取动漫剧集统计信息，动漫ID: {}", animeId);
        
        Long totalCount = episodeRepository.countByAnimeId(animeId);
        Long publishedCount = episodeRepository.countPublishedByAnimeId(animeId);
        Object[] range = episodeRepository.getEpisodeNumberRangeByAnimeId(animeId);
        
        Integer minEpisode = range[0] != null ? (Integer) range[0] : null;
        Integer maxEpisode = range[1] != null ? (Integer) range[1] : null;
        
        return new EpisodeStats(totalCount, publishedCount, minEpisode, maxEpisode);
    }

    /**
     * 剧集导航信息
     */
    public static class EpisodeNavigation {
        private final Episode previousEpisode;
        private final Episode nextEpisode;

        public EpisodeNavigation(Episode previousEpisode, Episode nextEpisode) {
            this.previousEpisode = previousEpisode;
            this.nextEpisode = nextEpisode;
        }

        public Episode getPreviousEpisode() { return previousEpisode; }
        public Episode getNextEpisode() { return nextEpisode; }
    }

    /**
     * 剧集统计信息
     */
    public static class EpisodeStats {
        private final Long totalCount;
        private final Long publishedCount;
        private final Integer minEpisode;
        private final Integer maxEpisode;

        public EpisodeStats(Long totalCount, Long publishedCount, Integer minEpisode, Integer maxEpisode) {
            this.totalCount = totalCount;
            this.publishedCount = publishedCount;
            this.minEpisode = minEpisode;
            this.maxEpisode = maxEpisode;
        }

        public Long getTotalCount() { return totalCount; }
        public Long getPublishedCount() { return publishedCount; }
        public Integer getMinEpisode() { return minEpisode; }
        public Integer getMaxEpisode() { return maxEpisode; }
    }
}
