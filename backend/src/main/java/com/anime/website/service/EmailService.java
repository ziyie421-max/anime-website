package com.anime.website.service;

import com.anime.website.entity.EmailVerificationCode;
import com.anime.website.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 邮件服务 - 处理邮件发送和验证码管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationCodeRepository codeRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送注册验证码
     */
    @Transactional
    public void sendRegisterVerificationCode(String email, String ipAddress) {
        // 检查发送频率限制
        checkSendingLimits(email, ipAddress, EmailVerificationCode.CodeType.REGISTER);
        
        // 生成验证码
        String code = generateVerificationCode();
        
        // 保存验证码到数据库
        EmailVerificationCode verificationCode = EmailVerificationCode.create(
            email, code, EmailVerificationCode.CodeType.REGISTER, 10, ipAddress // 10分钟有效期
        );
        codeRepository.save(verificationCode);
        
        // 发送邮件
        sendVerificationEmail(email, code, "注册验证", "register");
        
        log.info("注册验证码已发送到邮箱: {}", email);
    }

    /**
     * 发送重置密码验证码
     */
    @Transactional
    public void sendResetPasswordVerificationCode(String email, String ipAddress) {
        // 检查发送频率限制
        checkSendingLimits(email, ipAddress, EmailVerificationCode.CodeType.RESET_PASSWORD);
        
        // 生成验证码
        String code = generateVerificationCode();
        
        // 保存验证码到数据库
        EmailVerificationCode verificationCode = EmailVerificationCode.create(
            email, code, EmailVerificationCode.CodeType.RESET_PASSWORD, 15, ipAddress // 15分钟有效期
        );
        codeRepository.save(verificationCode);
        
        // 发送邮件
        sendVerificationEmail(email, code, "重置密码", "reset-password");
        
        log.info("重置密码验证码已发送到邮箱: {}", email);
    }

    /**
     * 验证验证码
     */
    @Transactional
    public boolean verifyCode(String email, String code, EmailVerificationCode.CodeType type) {
        Optional<EmailVerificationCode> codeOptional = codeRepository.findValidCode(email, code, type, LocalDateTime.now());
        
        if (codeOptional.isPresent()) {
            EmailVerificationCode verificationCode = codeOptional.get();
            
            // 标记验证码为已使用
            verificationCode.markAsUsed();
            codeRepository.save(verificationCode);
            
            // 将该邮箱该类型的其他未使用验证码也标记为已使用
            codeRepository.markAllAsUsedByEmailAndType(email, type, LocalDateTime.now());
            
            log.info("验证码验证成功: {} - {}", email, type);
            return true;
        }
        
        log.warn("验证码验证失败: {} - {} - {}", email, code, type);
        return false;
    }

    /**
     * 检查发送频率限制
     */
    private void checkSendingLimits(String email, String ipAddress, EmailVerificationCode.CodeType type) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // 检查1分钟内是否已发送（防止频繁点击）
        long recentCount = codeRepository.countByEmailAndTypeAndCreatedAtAfter(email, type, oneMinuteAgo);
        if (recentCount > 0) {
            throw new RuntimeException("发送过于频繁，请1分钟后再试");
        }
        
        // 检查1小时内发送次数（防止恶意请求）
        long hourlyCount = codeRepository.countByEmailAndTypeAndCreatedAtAfter(email, type, oneHourAgo);
        if (hourlyCount >= 5) {
            throw new RuntimeException("发送次数过多，请1小时后再试");
        }
        
        // 检查IP地址1小时内发送次数
        long ipHourlyCount = codeRepository.countByIpAddressAndCreatedAtAfter(ipAddress, oneHourAgo);
        if (ipHourlyCount >= 10) {
            throw new RuntimeException("该IP发送次数过多，请稍后再试");
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 发送验证邮件
     */
    private void sendVerificationEmail(String email, String code, String purpose, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("【动漫网站】" + purpose + "验证码");
            
            String htmlContent = buildEmailContent(code, purpose, type);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            log.error("发送邮件失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String purpose, String type) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>").append(purpose).append("验证码</title>");
        html.append("</head>");
        html.append("<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        html.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");

        // 头部
        html.append("<div style=\"background: linear-gradient(135deg, #4EABE6 0%, #3A8BB8 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">");
        html.append("<h1 style=\"color: white; margin: 0; font-size: 28px;\">动漫网站</h1>");
        html.append("<p style=\"color: white; margin: 10px 0 0 0; opacity: 0.9;\">AnimeS~ - 精彩动漫世界</p>");
        html.append("</div>");

        // 内容区域
        html.append("<div style=\"background: #f8f9fa; padding: 40px; border-radius: 0 0 10px 10px; border: 1px solid #e9ecef;\">");
        html.append("<h2 style=\"color: #4EABE6; margin-top: 0;\">").append(purpose).append("验证码</h2>");
        html.append("<p>您好！</p>");
        html.append("<p>您正在进行").append(purpose).append("操作，请使用以下验证码完成验证：</p>");

        // 验证码区域
        html.append("<div style=\"background: #fff; border: 2px dashed #4EABE6; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;\">");
        html.append("<span style=\"font-size: 32px; font-weight: bold; color: #4EABE6; letter-spacing: 5px;\">").append(code).append("</span>");
        html.append("</div>");

        // 提示信息
        html.append("<div style=\"background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;\">");
        html.append("<p style=\"margin: 0; color: #856404;\"><strong>重要提示：</strong></p>");
        html.append("<ul style=\"margin: 10px 0 0 0; color: #856404;\">");
        html.append("<li>验证码有效期为10分钟，请及时使用</li>");
        html.append("<li>请勿将验证码告诉他人</li>");
        html.append("<li>如非本人操作，请忽略此邮件</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<hr style=\"border: none; border-top: 1px solid #e9ecef; margin: 30px 0;\">");

        // 底部信息
        html.append("<p style=\"color: #6c757d; font-size: 14px; margin: 0;\">");
        html.append("此邮件由系统自动发送，请勿回复<br>");
        html.append("发送时间：").append(currentTime).append("<br>");
        html.append("如有疑问，请联系客服");
        html.append("</p>");

        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 清理过期验证码（定时任务调用）
     */
    @Transactional
    public void cleanupExpiredCodes() {
        codeRepository.deleteExpiredCodes(LocalDateTime.now());
        log.info("已清理过期验证码");
    }
}
