package com.anime.website.repository;

import com.anime.website.entity.Episode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 剧集数据访问层
 */
@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    /**
     * 根据动漫ID查找所有剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId ORDER BY e.episodeNumber ASC")
    List<Episode> findByAnimeIdOrderByEpisodeNumberAsc(@Param("animeId") Long animeId);

    /**
     * 根据动漫ID查找已发布的剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true ORDER BY e.episodeNumber ASC")
    List<Episode> findPublishedByAnimeIdOrderByEpisodeNumberAsc(@Param("animeId") Long animeId);

    /**
     * 根据动漫ID分页查找剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId ORDER BY e.episodeNumber ASC")
    Page<Episode> findByAnimeIdOrderByEpisodeNumberAsc(@Param("animeId") Long animeId, Pageable pageable);

    /**
     * 根据动漫ID分页查找已发布的剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true ORDER BY e.episodeNumber ASC")
    Page<Episode> findPublishedByAnimeIdOrderByEpisodeNumberAsc(@Param("animeId") Long animeId, Pageable pageable);

    /**
     * 根据动漫ID和剧集编号查找剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId AND e.episodeNumber = :episodeNumber")
    Optional<Episode> findByAnimeIdAndEpisodeNumber(@Param("animeId") Long animeId, @Param("episodeNumber") Integer episodeNumber);

    /**
     * 根据动漫ID获取剧集统计信息
     */
    @Query("SELECT COUNT(e) FROM Episode e WHERE e.anime.id = :animeId")
    Long countByAnimeId(@Param("animeId") Long animeId);

    /**
     * 根据动漫ID获取已发布剧集数量
     */
    @Query("SELECT COUNT(e) FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true")
    Long countPublishedByAnimeId(@Param("animeId") Long animeId);

    /**
     * 根据动漫ID获取最新剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true ORDER BY e.episodeNumber DESC")
    List<Episode> findLatestByAnimeId(@Param("animeId") Long animeId, Pageable pageable);

    /**
     * 根据动漫ID获取热门剧集（按观看次数排序）
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true ORDER BY e.viewCount DESC")
    List<Episode> findPopularByAnimeId(@Param("animeId") Long animeId, Pageable pageable);

    /**
     * 根据动漫ID和关键词搜索剧集
     */
    @Query("SELECT e FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY e.episodeNumber ASC")
    List<Episode> searchByAnimeIdAndKeyword(@Param("animeId") Long animeId, @Param("keyword") String keyword);

    /**
     * 检查动漫是否存在指定剧集编号
     */
    @Query("SELECT COUNT(e) > 0 FROM Episode e WHERE e.anime.id = :animeId AND e.episodeNumber = :episodeNumber")
    boolean existsByAnimeIdAndEpisodeNumber(@Param("animeId") Long animeId, @Param("episodeNumber") Integer episodeNumber);

    /**
     * 获取动漫的剧集编号范围
     */
    @Query("SELECT MIN(e.episodeNumber), MAX(e.episodeNumber) FROM Episode e WHERE e.anime.id = :animeId AND e.isPublished = true")
    Object[] getEpisodeNumberRangeByAnimeId(@Param("animeId") Long animeId);
}
