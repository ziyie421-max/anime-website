package com.anime.website.repository;

import com.anime.website.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问层 - 处理分类相关的数据库操作
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据名称查找分类
     */
    Optional<Category> findByName(String name);

    /**
     * 获取所有分类（按排序字段升序）
     */
    List<Category> findAllByOrderBySortOrderAsc();

    /**
     * 根据排序字段范围查询分类
     */
    List<Category> findBySortOrderBetweenOrderBySortOrderAsc(Integer startOrder, Integer endOrder);

    /**
     * 检查名称是否存在（忽略大小写）
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * 获取热门分类（根据关联的动漫数量）
     */
    @Query("SELECT c FROM Category c LEFT JOIN c.animes a GROUP BY c ORDER BY COUNT(a) DESC")
    List<Category> findCategoriesOrderByAnimeCountDesc();

    /**
     * 获取有动漫的分类
     */
    @Query("SELECT DISTINCT c FROM Category c JOIN c.animes a")
    List<Category> findCategoriesWithAnime();
}
