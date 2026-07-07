package com.anime.website.repository;

import com.anime.website.entity.UserWatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户观看历史Repository - 处理观看历史数据的持久化操作
 */
@Repository
public interface UserWatchHistoryRepository extends JpaRepository<UserWatchHistory, Long> {

    // === 查询用户的观看历史 ===
    
    /**
     * 分页获取用户的观看历史（按最后观看时间倒序）
     */
    Page<UserWatchHistory> findByUserIdOrderByLastWatchTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 获取用户的全部观看历史
     */
    List<UserWatchHistory> findByUserIdOrderByLastWatchTimeDesc(Long userId);
    
    /**
     * 获取用户最近的观看历史（限制数量）
     */
    @Query("SELECT h FROM UserWatchHistory h WHERE h.user.id = :userId ORDER BY h.lastWatchTime DESC")
    List<UserWatchHistory> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    // === 查找特定观看记录 ===
    
    /**
     * 查找用户对本地动漫的观看记录
     */
    Optional<UserWatchHistory> findByUserIdAndAnimeId(Long userId, Long animeId);
    
    /**
     * 查找用户对本地剧集的观看记录
     */
    Optional<UserWatchHistory> findByUserIdAndEpisodeId(Long userId, Long episodeId);
    
    /**
     * 查找用户对外部动漫的观看记录
     */
    Optional<UserWatchHistory> findByUserIdAndExternalAnimeId(Long userId, String externalAnimeId);
    
    /**
     * 查找用户对外部动漫特定剧集的观看记录
     */
    Optional<UserWatchHistory> findByUserIdAndExternalAnimeIdAndExternalEpisodeIndex(
            Long userId, String externalAnimeId, Integer externalEpisodeIndex);

    // === 检查是否存在 ===
    
    /**
     * 检查是否存在本地动漫观看记录
     */
    boolean existsByUserIdAndAnimeId(Long userId, Long animeId);
    
    /**
     * 检查是否存在外部动漫观看记录
     */
    boolean existsByUserIdAndExternalAnimeId(Long userId, String externalAnimeId);

    // === 删除操作 ===
    
    /**
     * 删除指定的观看记录
     */
    void deleteByUserIdAndId(Long userId, Long id);
    
    /**
     * 删除用户的所有观看历史
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除用户某个时间之前的观看历史
     */
    void deleteByUserIdAndLastWatchTimeBefore(Long userId, LocalDateTime beforeTime);

    // === 统计 ===
    
    /**
     * 统计用户的观看记录数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计用户今日的观看记录数量
     */
    @Query("SELECT COUNT(h) FROM UserWatchHistory h WHERE h.user.id = :userId AND h.lastWatchTime >= :startOfDay")
    long countTodayWatchByUserId(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    // === 继续观看 ===
    
    /**
     * 获取用户的继续观看列表（有进度但未看完的）
     */
    @Query("SELECT h FROM UserWatchHistory h WHERE h.user.id = :userId " +
           "AND h.watchProgress > 0 AND h.totalDuration > 0 " +
           "AND (h.watchProgress * 1.0 / h.totalDuration) < 0.95 " +
           "ORDER BY h.lastWatchTime DESC")
    List<UserWatchHistory> findContinueWatchingByUserId(@Param("userId") Long userId, Pageable pageable);

    // === 按动漫分组查询（获取每部动漫最新观看记录） ===
    
    /**
     * 获取用户观看的外部动漫列表（每部动漫只返回最新一条记录）
     */
    @Query("SELECT h FROM UserWatchHistory h WHERE h.user.id = :userId " +
           "AND h.externalAnimeId IS NOT NULL " +
           "AND h.lastWatchTime = (SELECT MAX(h2.lastWatchTime) FROM UserWatchHistory h2 " +
           "WHERE h2.user.id = :userId AND h2.externalAnimeId = h.externalAnimeId) " +
           "ORDER BY h.lastWatchTime DESC")
    List<UserWatchHistory> findDistinctExternalAnimeByUserId(@Param("userId") Long userId, Pageable pageable);
}

