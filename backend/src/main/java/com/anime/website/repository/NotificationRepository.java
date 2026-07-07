package com.anime.website.repository;

import com.anime.website.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 通知数据访问层
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 获取用户的所有通知（分页，按时间降序）
    // 使用LEFT JOIN FETCH加载sender信息，避免懒加载问题；countQuery用于分页计数
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.sender WHERE n.user.id = :userId ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 获取用户的未读通知
    // 使用LEFT JOIN FETCH加载sender信息，避免懒加载问题；countQuery用于分页计数
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.sender WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Page<Notification> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);

    // 统计用户未读通知数量
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    // 标记用户所有通知为已读
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    // 标记单个通知为已读
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    // 删除用户的所有通知
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}

