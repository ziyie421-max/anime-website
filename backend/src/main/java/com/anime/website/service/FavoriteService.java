package com.anime.website.service;

import com.anime.website.dto.favorite.AddFavoriteRequest;
import com.anime.website.dto.favorite.FavoriteDTO;
import com.anime.website.entity.Anime;
import com.anime.website.entity.User;
import com.anime.website.entity.UserFavorite;
import com.anime.website.repository.AnimeRepository;
import com.anime.website.repository.UserFavoriteRepository;
import com.anime.website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务 - 处理用户收藏相关业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AnimeRepository animeRepository;

    /**
     * 添加收藏
     */
    @Transactional
    public FavoriteDTO addFavorite(Long userId, AddFavoriteRequest request) {
        log.info("添加收藏 - 用户ID: {}, 外部动漫ID: {}, 本地动漫ID: {}", 
                userId, request.getExternalAnimeId(), request.getAnimeId());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserFavorite favorite;
        
        if (request.isExternalAnime()) {
            // 检查是否已收藏外部动漫
            if (favoriteRepository.existsByUserIdAndExternalAnimeId(userId, request.getExternalAnimeId())) {
                throw new RuntimeException("该动漫已在收藏列表中");
            }
            
            // 创建外部动漫收藏
            favorite = UserFavorite.builder()
                    .user(user)
                    .externalAnimeId(request.getExternalAnimeId())
                    .externalAnimeTitle(request.getExternalAnimeTitle())
                    .externalAnimeCover(request.getExternalAnimeCover())
                    .externalAnimeDesc(request.getExternalAnimeDesc())
                    .sourceKey(request.getSourceKey())
                    .sourceName(request.getSourceName())
                    .build();
        } else {
            // 检查是否已收藏本地动漫
            if (favoriteRepository.existsByUserIdAndAnimeId(userId, request.getAnimeId())) {
                throw new RuntimeException("该动漫已在收藏列表中");
            }
            
            Anime anime = animeRepository.findById(request.getAnimeId())
                    .orElseThrow(() -> new RuntimeException("动漫不存在"));
            
            // 创建本地动漫收藏
            favorite = UserFavorite.builder()
                    .user(user)
                    .anime(anime)
                    .build();
        }
        
        favorite = favoriteRepository.save(favorite);
        log.info("收藏添加成功 - 收藏ID: {}", favorite.getId());
        
        return FavoriteDTO.fromEntity(favorite);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, String externalAnimeId, Long animeId) {
        log.info("取消收藏 - 用户ID: {}, 外部动漫ID: {}, 本地动漫ID: {}", userId, externalAnimeId, animeId);
        
        if (externalAnimeId != null && !externalAnimeId.isEmpty()) {
            // 取消外部动漫收藏
            favoriteRepository.deleteByUserIdAndExternalAnimeId(userId, externalAnimeId);
        } else if (animeId != null) {
            // 取消本地动漫收藏
            favoriteRepository.deleteByUserIdAndAnimeId(userId, animeId);
        } else {
            throw new RuntimeException("请提供动漫ID");
        }
        
        log.info("收藏已取消");
    }

    /**
     * 获取用户收藏列表（分页）
     */
    public Page<FavoriteDTO> getFavorites(Long userId, int page, int size) {
        log.info("获取用户收藏列表 - 用户ID: {}, 页码: {}, 大小: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return favorites.map(FavoriteDTO::fromEntity);
    }

    /**
     * 获取用户全部收藏列表
     */
    public List<FavoriteDTO> getAllFavorites(Long userId) {
        log.info("获取用户全部收藏列表 - 用户ID: {}", userId);
        
        List<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(FavoriteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long userId, String externalAnimeId, Long animeId) {
        if (externalAnimeId != null && !externalAnimeId.isEmpty()) {
            return favoriteRepository.existsByUserIdAndExternalAnimeId(userId, externalAnimeId);
        } else if (animeId != null) {
            return favoriteRepository.existsByUserIdAndAnimeId(userId, animeId);
        }
        return false;
    }

    /**
     * 批量检查是否已收藏（外部动漫）
     */
    public List<String> checkFavoritedExternalAnimeIds(Long userId, List<String> externalAnimeIds) {
        return favoriteRepository.findExternalAnimeIdsByUserIdAndExternalAnimeIdIn(userId, externalAnimeIds);
    }

    /**
     * 获取收藏数量
     */
    public long getFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    /**
     * 清空用户所有收藏
     */
    @Transactional
    public void clearAllFavorites(Long userId) {
        log.info("清空用户所有收藏 - 用户ID: {}", userId);
        favoriteRepository.deleteByUserId(userId);
        log.info("用户收藏已清空");
    }
}

