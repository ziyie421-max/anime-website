package com.anime.website.repository;

import com.anime.website.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 邮箱验证码数据访问层 - 处理验证码相关的数据库操作
 */
@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    /**
     * 根据邮箱和验证码类型查找最新的有效验证码
     */
    @Query("SELECT e FROM EmailVerificationCode e WHERE e.email = :email AND e.type = :type " +
           "AND e.used = false AND e.expiresAt > :now ORDER BY e.createdAt DESC")
    Optional<EmailVerificationCode> findLatestValidCode(@Param("email") String email, 
                                                       @Param("type") EmailVerificationCode.CodeType type,
                                                       @Param("now") LocalDateTime now);

    /**
     * 根据邮箱、验证码和类型查找验证码
     */
    @Query("SELECT e FROM EmailVerificationCode e WHERE e.email = :email AND e.code = :code " +
           "AND e.type = :type AND e.used = false AND e.expiresAt > :now")
    Optional<EmailVerificationCode> findValidCode(@Param("email") String email,
                                                 @Param("code") String code,
                                                 @Param("type") EmailVerificationCode.CodeType type,
                                                 @Param("now") LocalDateTime now);

    /**
     * 统计指定邮箱在指定时间内发送的验证码数量（防止频繁发送）
     */
    @Query("SELECT COUNT(e) FROM EmailVerificationCode e WHERE e.email = :email " +
           "AND e.type = :type AND e.createdAt > :since")
    long countByEmailAndTypeAndCreatedAtAfter(@Param("email") String email,
                                             @Param("type") EmailVerificationCode.CodeType type,
                                             @Param("since") LocalDateTime since);

    /**
     * 统计指定IP在指定时间内发送的验证码数量（防止恶意请求）
     */
    @Query("SELECT COUNT(e) FROM EmailVerificationCode e WHERE e.ipAddress = :ipAddress " +
           "AND e.createdAt > :since")
    long countByIpAddressAndCreatedAtAfter(@Param("ipAddress") String ipAddress,
                                          @Param("since") LocalDateTime since);

    /**
     * 将指定邮箱的所有未使用验证码标记为已使用（当用户成功验证后）
     */
    @Modifying
    @Query("UPDATE EmailVerificationCode e SET e.used = true, e.usedAt = :now " +
           "WHERE e.email = :email AND e.type = :type AND e.used = false")
    void markAllAsUsedByEmailAndType(@Param("email") String email,
                                    @Param("type") EmailVerificationCode.CodeType type,
                                    @Param("now") LocalDateTime now);

    /**
     * 删除过期的验证码（定时清理任务使用）
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationCode e WHERE e.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    /**
     * 根据邮箱和类型查找所有验证码（按创建时间倒序）
     */
    List<EmailVerificationCode> findByEmailAndTypeOrderByCreatedAtDesc(String email, EmailVerificationCode.CodeType type);

    /**
     * 查找指定时间之前创建的验证码（用于清理）
     */
    List<EmailVerificationCode> findByCreatedAtBefore(LocalDateTime before);
}
