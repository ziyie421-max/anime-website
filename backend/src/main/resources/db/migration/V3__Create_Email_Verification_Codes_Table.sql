-- 创建邮箱验证码表
CREATE TABLE email_verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    email VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    type ENUM('REGISTER', 'RESET_PASSWORD', 'CHANGE_EMAIL', 'LOGIN') NOT NULL COMMENT '验证码类型',
    used BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已使用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    used_at TIMESTAMP NULL COMMENT '使用时间',
    ip_address VARCHAR(45) COMMENT '请求IP地址',
    
    INDEX idx_email_type (email, type),
    INDEX idx_email_code_type (email, code, type),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_at (created_at),
    INDEX idx_ip_address (ip_address)
) COMMENT '邮箱验证码表';

-- 创建定时清理过期验证码的事件（可选）
-- DELIMITER ;;
-- CREATE EVENT IF NOT EXISTS cleanup_expired_verification_codes
-- ON SCHEDULE EVERY 1 HOUR
-- DO
-- BEGIN
--     DELETE FROM email_verification_codes 
--     WHERE expires_at < NOW();
-- END;;
-- DELIMITER ;
