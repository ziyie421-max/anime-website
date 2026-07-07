package com.anime.website.repository;

import com.anime.website.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评分数据访问层
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // 根据用户ID和本地动漫ID查找评分
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.anime.id = :animeId")
    Optional<Rating> findByUserIdAndAnimeId(@Param("userId") Long userId, @Param("animeId") Long animeId);

    // 根据用户ID和外部动漫ID查找评分（JOIN FETCH加载用户信息避免懒加载问题）
    @Query("SELECT r FROM Rating r JOIN FETCH r.user WHERE r.user.id = :userId AND r.externalAnimeId = :externalAnimeId")
    Optional<Rating> findByUserIdAndExternalAnimeId(@Param("userId") Long userId, @Param("externalAnimeId") String externalAnimeId);

    // 计算本地动漫的平均评分
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.anime.id = :animeId")
    Double getAverageScoreByAnimeId(@Param("animeId") Long animeId);

    // 计算外部动漫的平均评分
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.externalAnimeId = :externalAnimeId")
    Double getAverageScoreByExternalAnimeId(@Param("externalAnimeId") String externalAnimeId);

    // 统计本地动漫的评分人数
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.anime.id = :animeId")
    long countByAnimeId(@Param("animeId") Long animeId);

    // 统计外部动漫的评分人数
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.externalAnimeId = :externalAnimeId")
    long countByExternalAnimeId(@Param("externalAnimeId") String externalAnimeId);

    // 获取用户的所有评分
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    Page<Rating> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 获取本地动漫的所有评分
    @Query("SELECT r FROM Rating r WHERE r.anime.id = :animeId ORDER BY r.createdAt DESC")
    Page<Rating> findByAnimeId(@Param("animeId") Long animeId, Pageable pageable);

    // 获取外部动漫的所有评分
    @Query("SELECT r FROM Rating r WHERE r.externalAnimeId = :externalAnimeId ORDER BY r.createdAt DESC")
    Page<Rating> findByExternalAnimeId(@Param("externalAnimeId") String externalAnimeId, Pageable pageable);
}

