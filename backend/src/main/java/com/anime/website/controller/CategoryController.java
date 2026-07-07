package com.anime.website.controller;

import com.anime.website.dto.CategoryDTO;
import com.anime.website.entity.Category;
import com.anime.website.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类控制器 - 处理动漫分类相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域请求
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.info("获取所有分类");
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
            .map(CategoryDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }

    /**
     * 根据ID获取分类详情
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        log.info("获取分类详情 - ID: {}", id);

        return categoryService.getCategoryById(id)
            .map(category -> ResponseEntity.ok(CategoryDTO.fromEntity(category)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据名称获取分类
     * GET /api/categories/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        log.info("根据名称获取分类: {}", name);
        
        return categoryService.getCategoryByName(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取热门分类
     * GET /api/categories/popular?limit=8
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Category>> getPopularCategories(
            @RequestParam(defaultValue = "8") int limit) {
        
        log.info("获取热门分类 - 限制: {}", limit);
        List<Category> popularCategories = categoryService.getPopularCategories(limit);
        return ResponseEntity.ok(popularCategories);
    }

    /**
     * 创建新分类（管理员功能）
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        log.info("创建新分类: {}", category.getName());
        
        try {
            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.ok(createdCategory);
        } catch (RuntimeException e) {
            log.warn("创建分类失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新分类信息（管理员功能）
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id, 
            @RequestBody Category categoryDetails) {
        
        log.info("更新分类信息 - ID: {}", id);
        
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            log.warn("更新分类失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除分类（管理员功能）
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        log.info("删除分类 - ID: {}", id);
        
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("删除分类失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 检查分类名称是否存在
     * GET /api/categories/check-name?name=动作
     */
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkCategoryName(@RequestParam String name) {
        log.info("检查分类名称是否存在: {}", name);
        
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    /**
     * 获取分类统计信息
     * GET /api/categories/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Long> getCategoryStats() {
        log.info("获取分类统计信息");
        
        long totalCount = categoryService.getTotalCategoryCount();
        return ResponseEntity.ok(totalCount);
    }
}
