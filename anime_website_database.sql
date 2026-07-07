-- ============================================================
-- 动漫网站系统 - 完整数据库初始化脚本
-- ============================================================
-- 数据库: anime_website (MySQL 8.0+)
-- 字符集: utf8mb4 (支持完整Unicode，包括emoji)
-- 排序规则: utf8mb4_unicode_ci
--
-- 使用方法:
--   1. 确保MySQL服务已启动
--   2. 在MySQL命令行或Navicat/DBeaver等工具中执行此脚本
--   3. 执行命令示例: mysql -u root -p < anime_website_database.sql
--
-- 默认账户信息:
--   - 管理员: admin / admin123
--   - 普通用户: user1-user3 / 123456
-- ============================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `anime_website`
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- 使用数据库
USE `anime_website`;

-- ============================================================
-- 第一部分：删除已存在的表（按依赖关系倒序）
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `email_verification_codes`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `user_watch_history`;
DROP TABLE IF EXISTS `user_favorites`;
DROP TABLE IF EXISTS `episodes`;
DROP TABLE IF EXISTS `anime_categories`;
DROP TABLE IF EXISTS `anime`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 第二部分：创建数据表
-- ============================================================

-- ----------------------------------------------------------
-- 2.1 用户表 (users)
-- 存储系统所有用户的账号信息
-- 支持普通用户和管理员两种角色
-- ----------------------------------------------------------
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID（主键，自增）',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（唯一）',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱地址（唯一，用于登录和找回密码）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密存储）',
    `nickname` VARCHAR(100) COMMENT '昵称（显示名称）',
    `avatar` VARCHAR(500) COMMENT '头像图片URL',
    `role` ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色：USER-普通用户，ADMIN-管理员',
    `status` ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '账户状态：ACTIVE-正常，INACTIVE-未激活，BANNED-封禁',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------------------------------------
-- 2.2 分类表 (categories)
-- 存储动漫的分类信息（如动作、冒险、喜剧等）
-- ----------------------------------------------------------
CREATE TABLE `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID（主键，自增）',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称（唯一）',
    `description` TEXT COMMENT '分类描述',
    `image` VARCHAR(500) COMMENT '分类封面图片URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号（数值越小越靠前）',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动漫分类表';

-- ----------------------------------------------------------
-- 2.3 动漫表 (anime)
-- 存储所有动漫作品的基本信息
-- 包含标题、描述、评分、观看数等核心字段
-- ----------------------------------------------------------
CREATE TABLE `anime` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '动漫ID（主键，自增）',
    `title` VARCHAR(200) NOT NULL COMMENT '动漫标题（中文或翻译后）',
    `original_title` VARCHAR(200) COMMENT '原始标题（日文原名）',
    `description` TEXT COMMENT '动漫简介/描述',
    `poster` VARCHAR(500) COMMENT '海报图片URL',
    `banner` VARCHAR(500) COMMENT '横幅/轮播图URL',
    `year` INT COMMENT '播出年份',
    `season` ENUM('SPRING', 'SUMMER', 'AUTUMN', 'WINTER') COMMENT '播出季节',
    `status` ENUM('ONGOING', 'COMPLETED', 'UPCOMING') DEFAULT 'UPCOMING' COMMENT '连载状态：ONGOING-连载中，COMPLETED-已完结，UPCOMING-即将播出',
    `total_episodes` INT DEFAULT 0 COMMENT '总计划集数',
    `current_episodes` INT DEFAULT 0 COMMENT '当前已播出集数',
    `rating` DECIMAL(3,1) DEFAULT 0.0 COMMENT '平均评分（0.0-10.0）',
    `rating_count` INT DEFAULT 0 COMMENT '参与评分的人数',
    `view_count` BIGINT DEFAULT 0 COMMENT '累计观看次数',
    `is_featured` BOOLEAN DEFAULT FALSE COMMENT '是否为推荐/精选动漫',
    `studio` VARCHAR(100) COMMENT '动画制作公司/工作室',
    `director` VARCHAR(100) COMMENT '导演',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_anime_title` (`title`),
    INDEX `idx_anime_year` (`year`),
    INDEX `idx_anime_status` (`status`),
    INDEX `idx_anime_rating` (`rating`),
    INDEX `idx_anime_view_count` (`view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动漫表';

-- ----------------------------------------------------------
-- 2.4 动漫-分类关联表 (anime_categories)
-- 多对多关系表，实现动漫与分类的关联
-- 一部动漫可以属于多个分类
-- ----------------------------------------------------------
CREATE TABLE `anime_categories` (
    `anime_id` BIGINT NOT NULL COMMENT '动漫ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    PRIMARY KEY (`anime_id`, `category_id`),
    CONSTRAINT `fk_ac_anime` FOREIGN KEY (`anime_id`) REFERENCES `anime`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ac_category` FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动漫分类关联表（多对多）';

-- ----------------------------------------------------------
-- 2.5 剧集表 (episodes)
-- 存储每部动漫的具体剧集信息
-- 包括视频链接、时长、发布状态等
-- ----------------------------------------------------------
CREATE TABLE `episodes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '剧集ID（主键，自增）',
    `anime_id` BIGINT NOT NULL COMMENT '所属动漫ID',
    `episode_number` INT NOT NULL COMMENT '集数（第几集）',
    `title` VARCHAR(200) COMMENT '剧集标题',
    `description` TEXT COMMENT '剧集简介',
    `thumbnail` VARCHAR(500) COMMENT '剧集缩略图URL',
    `video_url` VARCHAR(500) COMMENT '视频播放地址URL',
    `duration` INT COMMENT '时长（单位：秒）',
    `air_date` DATE COMMENT '首播日期',
    `view_count` BIGINT DEFAULT 0 COMMENT '观看次数',
    `is_published` BOOLEAN DEFAULT FALSE COMMENT '是否已发布/可观看',
    `published_at` TIMESTAMP NULL COMMENT '发布时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_episodes_anime` FOREIGN KEY (`anime_id`) REFERENCES `anime`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_anime_episode` (`anime_id`, `episode_number`),
    INDEX `idx_episodes_anime_id` (`anime_id`),
    INDEX `idx_episode_number` (`episode_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧集表';

-- ----------------------------------------------------------
-- 2.6 用户收藏表 (user_favorites)
-- 记录用户收藏的动漫（多对多关系）
-- ----------------------------------------------------------
CREATE TABLE `user_favorites` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `anime_id` BIGINT NOT NULL COMMENT '动漫ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_uf_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_uf_anime` FOREIGN KEY (`anime_id`) REFERENCES `anime`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_anime` (`user_id`, `anime_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ----------------------------------------------------------
-- 2.7 观看历史表 (user_watch_history)
-- 记录用户的观看进度和历史
-- 支持断点续播功能
-- ----------------------------------------------------------
CREATE TABLE `user_watch_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `anime_id` BIGINT NOT NULL COMMENT '动漫ID',
    `episode_id` BIGINT COMMENT '当前观看的剧集ID',
    `watch_progress` INT DEFAULT 0 COMMENT '观看进度（单位：秒）',
    `last_watch_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '首次观看时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_wh_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_wh_anime` FOREIGN KEY (`anime_id`) REFERENCES `anime`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_wh_episode` FOREIGN KEY (`episode_id`) REFERENCES `episodes`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_episode` (`user_id`, `episode_id`),
    INDEX `idx_wh_user_id` (`user_id`),
    INDEX `idx_wh_last_watch_time` (`last_watch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观看历史表';

-- ----------------------------------------------------------
-- 2.8 评论表 (comments)
-- 存储用户对动漫/剧集的评论
-- 支持评论回复（通过parent_id实现嵌套）
-- ----------------------------------------------------------
CREATE TABLE `comments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '评论者用户ID',
    `anime_id` BIGINT NOT NULL COMMENT '被评论的动漫ID',
    `episode_id` BIGINT COMMENT '被评论的剧集ID（可选，NULL表示对整部动漫评论）',
    `parent_id` BIGINT COMMENT '父评论ID（用于回复功能，NULL表示顶层评论）',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数量',
    `status` ENUM('ACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常显示，HIDDEN-隐藏，DELETED-已删除',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_c_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_c_anime` FOREIGN KEY (`anime_id`) REFERENCES `anime`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_c_episode` FOREIGN KEY (`episode_id`) REFERENCES `episodes`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_c_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments`(`id`) ON DELETE CASCADE,
    INDEX `idx_comments_anime_id` (`anime_id`),
    INDEX `idx_comments_user_id` (`user_id`),
    INDEX `idx_comments_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ----------------------------------------------------------
-- 2.9 邮箱验证码表 (email_verification_codes)
-- 用于邮箱验证和密码重置功能的验证码存储
-- ----------------------------------------------------------
CREATE TABLE `email_verification_codes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID（主键）',
    `email` VARCHAR(100) NOT NULL COMMENT '目标邮箱地址',
    `code` VARCHAR(6) NOT NULL COMMENT '6位验证码',
    `type` ENUM('REGISTER', 'RESET_PASSWORD', 'CHANGE_EMAIL') NOT NULL COMMENT '验证码类型：REGISTER-注册，RESET_PASSWORD-重置密码，CHANGE_EMAIL-更换邮箱',
    `expires_at` TIMESTAMP NOT NULL COMMENT '过期时间',
    `used` BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    `used_at` TIMESTAMP NULL COMMENT '使用时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_evc_email` (`email`),
    INDEX `idx_evc_code` (`code`),
    INDEX `idx_evc_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- ============================================================
-- 第三部分：插入基础数据
-- ============================================================

-- ----------------------------------------------------------
-- 3.1 插入分类数据（8个主流动漫分类）
-- ----------------------------------------------------------
INSERT INTO `categories` (`name`, `description`, `image`, `sort_order`) VALUES
('动作', '充满战斗和冒险的热血动漫作品', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 1),
('冒险', '探索未知世界的精彩旅程', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 2),
('喜剧', '轻松幽默的搞笑动漫', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 3),
('剧情', '注重深度剧情和人物刻画的优秀作品', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 4),
('奇幻', '魔法与幻想交织的奇妙世界', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 5),
('恋爱', '浪漫温馨的爱情故事', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 6),
('科幻', '未来科技与太空探索题材', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 7),
('悬疑', '扣人心弦的推理悬疑故事', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 8);

-- ----------------------------------------------------------
-- 3.2 插入用户数据
-- 密码均为123456的BCrypt加密结果
-- 包含1个管理员 + 3个普通用户
-- ----------------------------------------------------------
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `avatar`, `role`, `status`) VALUES
('admin', 'admin@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '管理员', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop&crop=face', 'ADMIN', 'ACTIVE'),
('user1', 'user1@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '动漫爱好者1', 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=100&h=100&fit=crop&crop=face', 'USER', 'ACTIVE'),
('user2', 'user2@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '动漫爱好者2', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face', 'USER', 'ACTIVE'),
('user3', 'user3@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '动漫爱好者3', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face', 'USER', 'ACTIVE');

-- ----------------------------------------------------------
-- 3.3 插入动漫数据（5部热门动漫）
-- ----------------------------------------------------------
INSERT INTO `anime` (`title`, `original_title`, `description`, `poster`, `banner`, `year`, `season`, `status`, `total_episodes`, `current_episodes`, `rating`, `rating_count`, `view_count`, `is_featured`, `studio`, `director`) VALUES
('进击的巨人 最终季', 'Shingeki no Kyojin: The Final Season', '《进击的巨人》是由諫山創创作的日本漫画改编作品。故事描述了人类在被巨人支配的世界中，为了生存而进行的殊死战斗。最终季揭开了所有谜团的真相。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'WINTER', 'COMPLETED', 24, 24, 4.8, 15420, 2580000, TRUE, 'WIT Studio', '荒木哲郎'),
('鬼灭之刃 刀匠村篇', 'Kimetsu no Yaiba: Katanakaji no Sato-hen', '炭治郎前往刀匠村修复日轮刀，在那里遇到了霞柱·時透無一郎和恋柱·甘露寺蜜璃，并面临新的强敌——上弦之伍·玉壶和上弦之肆·半天狗。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'SPRING', 'COMPLETED', 11, 11, 4.7, 12350, 1890000, TRUE, 'Ufotable', '外崎春雄'),
('咒术回战 第二季', 'Jujutsu Kaisen Season 2', '虎杖悠仁等人继续在咒术高专学习。第二季分为"懷玉·玉折"篇和"涩谷事变"篇，讲述五条悟学生时代的往事以及东京咒术界面临的重大危机。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'SUMMER', 'ONGOING', 24, 18, 4.6, 9870, 1560000, TRUE, 'MAPPA', '朴性厚'),
('间谍过家家 第二季', 'SPY×FAMILY Season 2', '黄昏、约儿和阿尼亚组成的伪装家庭继续他们的日常生活。在保持各自身份秘密的同时，他们必须面对新的挑战和任务。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'AUTUMN', 'ONGOING', 12, 8, 4.5, 8760, 1340000, FALSE, 'WIT Studio', '古橋一浩'),
('葬送的芙莉莲', 'Sousou no Frieren', '勇者一行在打败魔王后各自踏上了不同的人生道路。长寿的精灵法师芙莉莲开始了她理解人类情感的漫长旅程，重新走遍曾经冒险过的地方。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'AUTUMN', 'ONGOING', 28, 16, 4.9, 11200, 980000, TRUE, 'Madhouse', '斎藤圭一郎');

-- ----------------------------------------------------------
-- 3.4 插入动漫-分类关联数据
-- 每部动漫关联多个分类标签
-- ----------------------------------------------------------
INSERT INTO `anime_categories` (`anime_id`, `category_id`) VALUES
-- 进击的巨人：动作 + 剧情 + 奇幻
(1, 1), (1, 4), (1, 5),
-- 鬼灭之刃：动作 + 冒险 + 奇幻
(2, 1), (2, 2), (2, 5),
-- 咒术回战：动作 + 奇幻 + 悬疑
(3, 1), (3, 5), (3, 8),
-- 间谍过家家：喜剧 + 动作 + 剧情
(4, 3), (4, 1), (4, 4),
-- 葬送的芙莉莲：奇幻 + 冒险 + 剧情
(5, 5), (5, 2), (5, 4);

-- ----------------------------------------------------------
-- 3.5 插入剧集数据（每部动漫3集示例）
-- ----------------------------------------------------------
INSERT INTO `episodes` (`anime_id`, `episode_number`, `title`, `description`, `thumbnail`, `video_url`, `duration`, `air_date`, `view_count`, `is_published`, `published_at`) VALUES
-- 进击的巨人 最终季 剧集
(1, 1, '审判', '艾伦等人面临军事法庭的审判，关于巨人的真相逐渐浮出水面。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/aot_s4_ep1.mp4', 1440, '2023-01-09', 580000, TRUE, '2023-01-09 21:00:00'),
(1, 2, '恶魔之子', '艾伦的真实身份被揭露，引发了调查兵团内部的巨大争议。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/aot_s4_ep2.mp4', 1440, '2023-01-16', 520000, TRUE, '2023-01-16 21:00:00'),
(1, 3, '两个兄弟', '艾伦和吉克的过去被揭示，两兄弟之间复杂的关系和各自的信念。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/aot_s4_ep3.mp4', 1440, '2023-01-23', 490000, TRUE, '2023-01-23 21:00:00'),

-- 鬼灭之刃 刀匠村篇 剧集
(2, 1, '有人的村子', '炭治郎来到刀匠村，遇到了新的伙伴霞柱和恋柱，村子里隐藏着秘密。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/kny_s3_ep1.mp4', 1440, '2023-04-09', 450000, TRUE, '2023-04-09 21:00:00'),
(2, 2, '缘壱零式', '炭治郎发现了传说中的训练用人偶缘壹零式，开始特训以提升实力。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/kny_s3_ep2.mp4', 1440, '2023-04-16', 420000, TRUE, '2023-04-16 21:00:00'),
(2, 3, '300年前的刀', '炭治郎的日轮刀发生了变化，古老的刀中似乎隐藏着重要的秘密。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/kny_s3_ep3.mp4', 1440, '2023-04-23', 410000, TRUE, '2023-04-23 21:00:00'),

-- 咒术回战 第二季 剧集
(3, 1, '懐玉', '时间回到过去，五条悟和夏油杰还是咒术高专的学生，两人执行保护星浆体的任务。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/jjk_s2_ep1.mp4', 1440, '2023-07-06', 380000, TRUE, '2023-07-06 21:00:00'),
(3, 2, '隐藏的库存', '过去篇继续展开，展现年轻时期的最强咒术师们如何面对强敌。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/jjk_s2_ep2.mp4', 1440, '2023-07-13', 360000, TRUE, '2023-07-13 21:00:00'),
(3, 3, '星浆体', '天内理子的故事，关于星浆体的秘密和保护任务背后的真相。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/jjk_s3_ep3.mp4', 1440, '2023-07-20', 340000, TRUE, '2023-07-20 21:00:00');

-- ----------------------------------------------------------
-- 3.6 插入用户收藏数据
-- 展示用户间的不同收藏偏好
-- ----------------------------------------------------------
INSERT INTO `user_favorites` (`user_id`, `anime_id`) VALUES
-- user1 的收藏
(2, 1), (2, 2), (2, 5),
-- user2 的收藏
(3, 1), (3, 3), (3, 4),
-- user3 的收藏
(4, 2), (4, 5);

-- ----------------------------------------------------------
-- 3.7 插入观看历史数据
-- 记录用户的观看进度（支持断点续播）
-- ----------------------------------------------------------
INSERT INTO `user_watch_history` (`user_id`, `anime_id`, `episode_id`, `watch_progress`) VALUES
-- user1 的观看进度
(2, 1, 1, 1200),  -- 进击的巨人第1集看了20分钟
(2, 1, 2, 800),   -- 第2集看了13分钟
(2, 2, 4, 1440),  -- 鬼灭第1集看完（24分钟）
-- user2 的观看进度
(3, 1, 1, 1440),  -- 进击的巨人第1集看完
(3, 3, 7, 600),   -- 咒术回战第1集看了10分钟
-- user3 的观看进度
(4, 2, 4, 900);   -- 鬼灭第1集看了15分钟

-- ----------------------------------------------------------
-- 3.8 插入评论数据
-- 包含动漫评论和剧集评论，展示评论功能
-- ----------------------------------------------------------
INSERT INTO `comments` (`user_id`, `anime_id`, `episode_id`, `content`, `like_count`) VALUES
-- 对进击的巨人的评论
(2, 1, 1, '进击的巨人真的太精彩了！每一集都让人欲罢不能，剧情紧凑不拖沓！', 15),
(3, 1, 1, '艾伦的成长轨迹真的让人感动，从单纯少年到背负一切的战士，期待后续发展。', 8),
(4, 1, NULL, '这部动漫的制作质量真的很高，无论是作画还是配乐都是业界顶尖水平，强烈推荐大家观看！', 12),
-- 对鬼灭之刃的评论
(2, 2, 4, '鬼灭之刃的战斗场面太燃了！Ufotable的制作真的是视觉盛宴！', 20),
(3, 2, NULL, '炭治郎的性格真的很讨喜，温柔又坚强。', 18),
-- 对咒术回战的评论
(4, 3, 7, '咒术回战的设定很有创意，每个角色都很有魅力，特别是五条老师的过去篇太感人了。', 10);

-- ============================================================
-- 第四部分：数据验证
-- ============================================================

-- 显示各表的记录数量，确认数据插入成功
SELECT '====================================' AS '';
SELECT '     数据库初始化完成！' AS message;
SELECT '====================================' AS '';

SELECT
    '分类' AS 表名,
    COUNT(*) AS 记录数
FROM categories
UNION ALL SELECT '用户', COUNT(*) FROM users
UNION ALL SELECT '动漫', COUNT(*) FROM anime
UNION ALL SELECT '关联', COUNT(*) FROM anime_categories
UNION ALL SELECT '剧集', COUNT(*) FROM episodes
UNION ALL SELECT '收藏', COUNT(*) FROM user_favorites
UNION ALL SELECT '观看历史', COUNT(*) FROM user_watch_history
UNION ALL SELECT '评论', COUNT(*) FROM comments;

-- 显示默认账户信息
SELECT '------------------------------------' AS '';
SELECT '默认登录账户:' AS 提示;
SELECT '  管理员: admin / admin123' AS 账户;
SELECT '  普通用户: user1~user3 / 123456' AS 账户;
SELECT '------------------------------------' AS '';

COMMIT;
