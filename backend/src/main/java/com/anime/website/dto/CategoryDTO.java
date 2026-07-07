package com.anime.website.dto;

import com.anime.website.entity.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类数据传输对象 - 用于API响应，避免懒加载问题
 */
@Data
@NoArgsConstructor
public class CategoryDTO {
    
    private Long id;
    private String name;
    private String description;
    private String image;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为DTO
     */
    public static CategoryDTO fromEntity(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImage(category.getImage());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        return dto;
    }
}
