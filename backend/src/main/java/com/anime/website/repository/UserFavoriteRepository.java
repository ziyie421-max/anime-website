package com.anime.website.repository;

import com.anime.website.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户收藏Repository - 处理用户收藏数据的持久化操作
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    // === 查询用户的收藏列表 ===
    
    /**
     * 分页获取用户的所有收藏（按创建时间倒序）
     */
    Page<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 获取用户的所有收藏
     */
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    // === 检查是否已收藏 ===
    
    /**
     * 检查用户是否已收藏本地动漫
     */
    boolean existsByUserIdAndAnimeId(Long userId, Long animeId);
    
    /**
     * 检查用户是否已收藏外部动漫
     */
    boolean existsByUserIdAndExternalAnimeId(Long userId, String externalAnimeId);

    // === 查找特定收藏 ===
    
    /**
     * 查找用户对本地动漫的收藏
     */
    Optional<UserFavorite> findByUserIdAndAnimeId(Long userId, Long animeId);
    
    /**
     * 查找用户对外部动漫的收藏
     */
    Optional<UserFavorite> findByUserIdAndExternalAnimeId(Long userId, String externalAnimeId);

    // === 删除收藏 ===
    
    /**
     * 删除用户对本地动漫的收藏
     */
    void deleteByUserIdAndAnimeId(Long userId, Long animeId);
    
    /**
     * 删除用户对外部动漫的收藏
     */
    void deleteByUserIdAndExternalAnimeId(Long userId, String externalAnimeId);
    
    /**
     * 删除用户的所有收藏
     */
    void deleteByUserId(Long userId);

    // === 统计 ===
    
    /**
     * 统计用户的收藏数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计动漫被收藏的次数（本地动漫）
     */
    long countByAnimeId(Long animeId);
    
    /**
     * 统计外部动漫被收藏的次数
     */
    long countByExternalAnimeId(String externalAnimeId);

    // === 批量查询 ===
    
    /**
     * 批量检查外部动漫是否已收藏
     */
    @Query("SELECT f.externalAnimeId FROM UserFavorite f WHERE f.user.id = :userId AND f.externalAnimeId IN :animeIds")
    List<String> findExternalAnimeIdsByUserIdAndExternalAnimeIdIn(@Param("userId") Long userId, @Param("animeIds") List<String> animeIds);
}

