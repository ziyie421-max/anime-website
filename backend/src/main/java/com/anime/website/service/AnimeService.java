package com.anime.website.service;

import com.anime.website.entity.Anime;
import com.anime.website.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 动漫服务类 - 处理动漫相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimeService {

    private final AnimeRepository animeRepository;

    /**
     * 获取所有动漫列表（分页）
     */
    public Page<Anime> getAllAnime(Pageable pageable) {
        log.debug("获取动漫列表，页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return animeRepository.findAll(pageable);
    }

    /**
     * 根据ID获取动漫详情
     */
    public Optional<Anime> getAnimeById(Long id) {
        log.debug("获取动漫详情，ID: {}", id);
        return animeRepository.findById(id);
    }

    /**
     * 获取热门动漫（按观看次数排序）
     */
    public List<Anime> getPopularAnime(int limit) {
        log.debug("获取热门动漫，限制数量: {}", limit);
        return animeRepository.findTopByOrderByViewCountDesc(limit);
    }

    /**
     * 获取最新更新的动漫
     */
    public List<Anime> getLatestAnime(int limit) {
        log.debug("获取最新动漫，限制数量: {}", limit);
        return animeRepository.findTopByOrderByUpdatedAtDesc(limit);
    }

    /**
     * 获取推荐动漫
     */
    public List<Anime> getFeaturedAnime() {
        log.debug("获取推荐动漫");
        return animeRepository.findByIsFeaturedTrueOrderByRatingDesc();
    }

    /**
     * 根据状态获取动漫
     */
    public List<Anime> getAnimeByStatus(Anime.AnimeStatus status, Pageable pageable) {
        log.debug("根据状态获取动漫: {}", status);
        return animeRepository.findByStatusOrderByUpdatedAtDesc(status, pageable);
    }

    /**
     * 根据分类获取动漫
     */
    public Page<Anime> getAnimeByCategory(Long categoryId, Pageable pageable) {
        log.debug("根据分类获取动漫，分类ID: {}", categoryId);
        return animeRepository.findByCategoriesIdOrderByUpdatedAtDesc(categoryId, pageable);
    }

    /**
     * 搜索动漫（根据标题）
     */
    public Page<Anime> searchAnime(String keyword, Pageable pageable) {
        log.debug("搜索动漫，关键词: {}", keyword);
        return animeRepository.findByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(
            keyword, keyword, pageable);
    }

    /**
     * 根据年份获取动漫
     */
    public Page<Anime> getAnimeByYear(Integer year, Pageable pageable) {
        log.debug("根据年份获取动漫: {}", year);
        return animeRepository.findByYearOrderByUpdatedAtDesc(year, pageable);
    }

    /**
     * 增加动漫观看次数
     */
    @Transactional
    public void incrementViewCount(Long animeId) {
        log.debug("增加动漫观看次数，ID: {}", animeId);
        animeRepository.incrementViewCount(animeId);
    }

    /**
     * 创建新动漫（管理员功能）
     */
    @Transactional
    public Anime createAnime(Anime anime) {
        log.info("创建新动漫: {}", anime.getTitle());
        return animeRepository.save(anime);
    }

    /**
     * 更新动漫信息（管理员功能）
     */
    @Transactional
    public Anime updateAnime(Long id, Anime animeDetails) {
        log.info("更新动漫信息，ID: {}", id);
        return animeRepository.findById(id)
            .map(anime -> {
                // 更新基本信息
                anime.setTitle(animeDetails.getTitle());
                anime.setOriginalTitle(animeDetails.getOriginalTitle());
                anime.setDescription(animeDetails.getDescription());
                anime.setPoster(animeDetails.getPoster());
                anime.setBanner(animeDetails.getBanner());
                anime.setYear(animeDetails.getYear());
                anime.setSeason(animeDetails.getSeason());
                anime.setStatus(animeDetails.getStatus());
                anime.setTotalEpisodes(animeDetails.getTotalEpisodes());
                anime.setCurrentEpisodes(animeDetails.getCurrentEpisodes());
                anime.setIsFeatured(animeDetails.getIsFeatured());
                anime.setStudio(animeDetails.getStudio());
                anime.setDirector(animeDetails.getDirector());
                
                return animeRepository.save(anime);
            })
            .orElseThrow(() -> new RuntimeException("动漫不存在，ID: " + id));
    }

    /**
     * 删除动漫（管理员功能）
     */
    @Transactional
    public void deleteAnime(Long id) {
        log.info("删除动漫，ID: {}", id);
        if (!animeRepository.existsById(id)) {
            throw new RuntimeException("动漫不存在，ID: " + id);
        }
        animeRepository.deleteById(id);
    }

    /**
     * 获取动漫统计信息
     */
    public long getTotalAnimeCount() {
        return animeRepository.count();
    }

    /**
     * 获取各状态动漫数量统计
     */
    public long getAnimeCountByStatus(Anime.AnimeStatus status) {
        return animeRepository.countByStatus(status);
    }
}
