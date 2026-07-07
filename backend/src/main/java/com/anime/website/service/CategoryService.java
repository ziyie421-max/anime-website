package com.anime.website.service;

import com.anime.website.entity.Category;
import com.anime.website.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类服务类 - 处理动漫分类相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有分类（按排序字段排序）
     */
    public List<Category> getAllCategories() {
        log.debug("获取所有分类");
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 根据ID获取分类
     */
    public Optional<Category> getCategoryById(Long id) {
        log.debug("获取分类详情，ID: {}", id);
        return categoryRepository.findById(id);
    }

    /**
     * 根据名称获取分类
     */
    public Optional<Category> getCategoryByName(String name) {
        log.debug("根据名称获取分类: {}", name);
        return categoryRepository.findByName(name);
    }

    /**
     * 创建新分类（管理员功能）
     */
    @Transactional
    public Category createCategory(Category category) {
        log.info("创建新分类: {}", category.getName());
        
        // 检查分类名称是否已存在
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("分类名称已存在: " + category.getName());
        }
        
        return categoryRepository.save(category);
    }

    /**
     * 更新分类信息（管理员功能）
     */
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        log.info("更新分类信息，ID: {}", id);
        
        return categoryRepository.findById(id)
            .map(category -> {
                // 检查新名称是否与其他分类冲突
                Optional<Category> existingCategory = categoryRepository.findByName(categoryDetails.getName());
                if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                    throw new RuntimeException("分类名称已存在: " + categoryDetails.getName());
                }
                
                category.setName(categoryDetails.getName());
                category.setDescription(categoryDetails.getDescription());
                category.setImage(categoryDetails.getImage());
                category.setSortOrder(categoryDetails.getSortOrder());
                
                return categoryRepository.save(category);
            })
            .orElseThrow(() -> new RuntimeException("分类不存在，ID: " + id));
    }

    /**
     * 删除分类（管理员功能）
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("删除分类，ID: {}", id);
        
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("分类不存在，ID: " + id);
        }
        
        // 检查是否有动漫使用此分类
        // 这里可以添加检查逻辑，防止删除正在使用的分类
        
        categoryRepository.deleteById(id);
    }

    /**
     * 获取分类总数
     */
    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }

    /**
     * 检查分类名称是否存在
     */
    public boolean existsByName(String name) {
        return categoryRepository.findByName(name).isPresent();
    }

    /**
     * 获取热门分类（根据关联的动漫数量）
     */
    public List<Category> getPopularCategories(int limit) {
        log.debug("获取热门分类，限制数量: {}", limit);
        // 这里可以实现根据动漫数量排序的逻辑
        // 暂时返回按排序字段排序的前N个分类
        return categoryRepository.findAllByOrderBySortOrderAsc()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}
