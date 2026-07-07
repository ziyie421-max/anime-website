package com.anime.website.repository;

import com.anime.website.entity.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动漫数据访问层 - 处理动漫相关的数据库操作
 */
@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    /**
     * 根据标题模糊查询动漫
     */
    Page<Anime> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 根据状态查询动漫
     */
    Page<Anime> findByStatus(Anime.AnimeStatus status, Pageable pageable);

    /**
     * 根据年份查询动漫
     */
    Page<Anime> findByYear(Integer year, Pageable pageable);

    /**
     * 根据季节查询动漫
     */
    Page<Anime> findBySeason(Anime.Season season, Pageable pageable);

    /**
     * 查询推荐动漫
     */
    Page<Anime> findByIsFeaturedTrue(Pageable pageable);

    /**
     * 根据分类查询动漫
     */
    @Query("SELECT a FROM Anime a JOIN a.categories c WHERE c.id = :categoryId")
    Page<Anime> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 根据评分排序查询动漫
     */
    Page<Anime> findAllByOrderByRatingDesc(Pageable pageable);

    /**
     * 根据观看次数排序查询动漫
     */
    Page<Anime> findAllByOrderByViewCountDesc(Pageable pageable);

    /**
     * 根据创建时间排序查询最新动漫
     */
    Page<Anime> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 复合搜索查询
     */
    @Query("SELECT a FROM Anime a WHERE " +
           "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:year IS NULL OR a.year = :year) AND " +
           "(:season IS NULL OR a.season = :season)")
    Page<Anime> findByMultipleConditions(
        @Param("title") String title,
        @Param("status") Anime.AnimeStatus status,
        @Param("year") Integer year,
        @Param("season") Anime.Season season,
        Pageable pageable
    );

    /**
     * 获取热门动漫（按观看次数）
     */
    List<Anime> findTop10ByOrderByViewCountDesc();

    /**
     * 获取最新动漫
     */
    List<Anime> findTop10ByOrderByCreatedAtDesc();

    /**
     * 获取推荐动漫
     */
    List<Anime> findTop10ByIsFeaturedTrueOrderByRatingDesc();

    // ========== Service层需要的方法 ==========

    /**
     * 根据观看次数获取热门动漫（限制数量）
     */
    @Query("SELECT a FROM Anime a ORDER BY a.viewCount DESC")
    List<Anime> findTopByOrderByViewCountDesc(@Param("limit") int limit);

    /**
     * 根据更新时间获取最新动漫（限制数量）
     */
    @Query("SELECT a FROM Anime a ORDER BY a.updatedAt DESC")
    List<Anime> findTopByOrderByUpdatedAtDesc(@Param("limit") int limit);

    /**
     * 获取推荐动漫（按评分排序）
     */
    List<Anime> findByIsFeaturedTrueOrderByRatingDesc();

    /**
     * 根据状态获取动漫（按更新时间排序）
     */
    List<Anime> findByStatusOrderByUpdatedAtDesc(Anime.AnimeStatus status, Pageable pageable);

    /**
     * 根据分类获取动漫（按更新时间排序）
     */
    @Query("SELECT a FROM Anime a JOIN a.categories c WHERE c.id = :categoryId ORDER BY a.updatedAt DESC")
    Page<Anime> findByCategoriesIdOrderByUpdatedAtDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 搜索动漫（标题或原始标题）
     */
    Page<Anime> findByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(
        String title, String originalTitle, Pageable pageable);

    /**
     * 根据年份获取动漫（按更新时间排序）
     */
    Page<Anime> findByYearOrderByUpdatedAtDesc(Integer year, Pageable pageable);

    /**
     * 增加观看次数
     */
    @Modifying
    @Query("UPDATE Anime a SET a.viewCount = a.viewCount + 1 WHERE a.id = :animeId")
    void incrementViewCount(@Param("animeId") Long animeId);

    /**
     * 根据状态统计动漫数量
     */
    long countByStatus(Anime.AnimeStatus status);
}
