package com.anime.website.repository;

import com.anime.website.entity.Comment;
import com.anime.website.entity.Comment.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论数据访问层
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 根据本地动漫ID获取顶级评论（无父评论）
    @Query("SELECT c FROM Comment c WHERE c.anime.id = :animeId AND c.parent IS NULL AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelByAnimeId(@Param("animeId") Long animeId, @Param("status") CommentStatus status, Pageable pageable);

    // 根据外部动漫ID获取顶级评论（JOIN FETCH加载用户信息避免懒加载问题）
    // 分页查询需要单独指定countQuery，因为count查询不能包含JOIN FETCH
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.externalAnimeId = :externalAnimeId AND c.parent IS NULL AND c.status = :status ORDER BY c.createdAt DESC",
           countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.externalAnimeId = :externalAnimeId AND c.parent IS NULL AND c.status = :status")
    Page<Comment> findTopLevelByExternalAnimeId(@Param("externalAnimeId") String externalAnimeId, @Param("status") CommentStatus status, Pageable pageable);

    // 获取评论的回复列表（JOIN FETCH加载用户信息避免懒加载问题）
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parent.id = :parentId AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId, @Param("status") CommentStatus status);

    // 统计本地动漫的评论数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.anime.id = :animeId AND c.status = :status")
    long countByAnimeId(@Param("animeId") Long animeId, @Param("status") CommentStatus status);

    // 统计外部动漫的评论数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.externalAnimeId = :externalAnimeId AND c.status = :status")
    long countByExternalAnimeId(@Param("externalAnimeId") String externalAnimeId, @Param("status") CommentStatus status);

    // 获取用户的所有评论
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findByUserId(@Param("userId") Long userId, @Param("status") CommentStatus status, Pageable pageable);

    // 获取最新评论
    @Query("SELECT c FROM Comment c WHERE c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findLatestComments(@Param("status") CommentStatus status, Pageable pageable);
}

