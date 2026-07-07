package com.anime.website.service;

import com.anime.website.dto.rating.AddRatingRequest;
import com.anime.website.dto.rating.RatingDTO;
import com.anime.website.dto.rating.RatingStatsDTO;
import com.anime.website.entity.Rating;
import com.anime.website.entity.User;
import com.anime.website.repository.RatingRepository;
import com.anime.website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 评分服务层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    /**
     * 添加或更新评分
     */
    @Transactional
    public RatingDTO addOrUpdateRating(Long userId, AddRatingRequest request) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Rating rating;
        
        // 检查是否已有评分
        if (request.getExternalAnimeId() != null && !request.getExternalAnimeId().isEmpty()) {
            // 外部动漫
            Optional<Rating> existingRating = ratingRepository.findByUserIdAndExternalAnimeId(
                    userId, request.getExternalAnimeId());
            
            if (existingRating.isPresent()) {
                // 更新现有评分
                rating = existingRating.get();
                rating.setScore(request.getScore());
                rating.setReview(request.getReview());
                log.info("用户 {} 更新评分: 外部动漫 {}, 分数 {}", userId, request.getExternalAnimeId(), request.getScore());
            } else {
                // 创建新评分
                rating = Rating.builder()
                        .user(user)
                        .externalAnimeId(request.getExternalAnimeId())
                        .externalAnimeTitle(request.getExternalAnimeTitle())
                        .score(request.getScore())
                        .review(request.getReview())
                        .build();
                log.info("用户 {} 新增评分: 外部动漫 {}, 分数 {}", userId, request.getExternalAnimeId(), request.getScore());
            }
        } else if (request.getAnimeId() != null) {
            // 本地动漫 - 暂不支持
            throw new RuntimeException("暂不支持本地动漫评分");
        } else {
            throw new RuntimeException("请提供动漫ID");
        }

        Rating savedRating = ratingRepository.save(rating);
        return RatingDTO.fromEntity(savedRating);
    }

    /**
     * 获取用户对某动漫的评分
     * 使用@Transactional(readOnly = true)确保懒加载在事务内完成
     */
    @Transactional(readOnly = true)
    public RatingDTO getUserRating(Long userId, String externalAnimeId) {
        Optional<Rating> rating = ratingRepository.findByUserIdAndExternalAnimeId(userId, externalAnimeId);
        return rating.map(RatingDTO::fromEntity).orElse(null);
    }

    /**
     * 获取动漫评分统计
     * 使用@Transactional(readOnly = true)确保懒加载在事务内完成
     */
    @Transactional(readOnly = true)
    public RatingStatsDTO getRatingStats(String externalAnimeId, Long userId) {
        Double avgScore = ratingRepository.getAverageScoreByExternalAnimeId(externalAnimeId);
        long count = ratingRepository.countByExternalAnimeId(externalAnimeId);
        
        RatingStatsDTO stats = RatingStatsDTO.builder()
                .externalAnimeId(externalAnimeId)
                .averageScore(avgScore != null ? avgScore / 10.0 : null)  // 转换为10分制
                .ratingCount(count)
                .build();

        // 如果用户已登录，获取用户的评分
        if (userId != null) {
            RatingDTO userRating = getUserRating(userId, externalAnimeId);
            stats.setUserRating(userRating);
        }

        return stats;
    }

    /**
     * 删除评分
     */
    @Transactional
    public void deleteRating(Long userId, String externalAnimeId) {
        Optional<Rating> rating = ratingRepository.findByUserIdAndExternalAnimeId(userId, externalAnimeId);
        if (rating.isPresent()) {
            ratingRepository.delete(rating.get());
            log.info("用户 {} 删除评分: 外部动漫 {}", userId, externalAnimeId);
        }
    }
}

