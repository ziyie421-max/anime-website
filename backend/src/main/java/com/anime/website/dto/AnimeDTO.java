package com.anime.website.dto;

import com.anime.website.entity.Anime;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动漫数据传输对象 - 用于API响应，避免懒加载问题
 */
@Data
@NoArgsConstructor
public class AnimeDTO {
    
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String poster;
    private String banner;
    private Integer year;
    private String season;
    private String status;
    private Integer totalEpisodes;
    private Integer currentEpisodes;
    private BigDecimal rating;
    private Integer ratingCount;
    private Long viewCount;
    private Boolean isFeatured;
    private String studio;
    private String director;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 分类信息（简化）
    private List<CategoryDTO> categories;

    /**
     * 从实体转换为DTO
     */
    public static AnimeDTO fromEntity(Anime anime) {
        AnimeDTO dto = new AnimeDTO();
        dto.setId(anime.getId());
        dto.setTitle(anime.getTitle());
        dto.setOriginalTitle(anime.getOriginalTitle());
        dto.setDescription(anime.getDescription());
        dto.setPoster(anime.getPoster());
        dto.setBanner(anime.getBanner());
        dto.setYear(anime.getYear());
        dto.setSeason(anime.getSeason() != null ? anime.getSeason().name() : null);
        dto.setStatus(anime.getStatus() != null ? anime.getStatus().name() : null);
        dto.setTotalEpisodes(anime.getTotalEpisodes());
        dto.setCurrentEpisodes(anime.getCurrentEpisodes());
        dto.setRating(anime.getRating());
        dto.setRatingCount(anime.getRatingCount());
        dto.setViewCount(anime.getViewCount());
        dto.setIsFeatured(anime.getIsFeatured());
        dto.setStudio(anime.getStudio());
        dto.setDirector(anime.getDirector());
        dto.setCreatedAt(anime.getCreatedAt());
        dto.setUpdatedAt(anime.getUpdatedAt());
        
        return dto;
    }

    /**
     * 从实体转换为DTO（包含分类信息）
     */
    public static AnimeDTO fromEntityWithCategories(Anime anime) {
        AnimeDTO dto = fromEntity(anime);
        
        // 只有在categories已经加载的情况下才设置
        try {
            if (anime.getCategories() != null) {
                dto.setCategories(
                    anime.getCategories().stream()
                        .map(CategoryDTO::fromEntity)
                        .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            // 忽略懒加载异常，不设置categories
            dto.setCategories(null);
        }
        
        return dto;
    }
}
