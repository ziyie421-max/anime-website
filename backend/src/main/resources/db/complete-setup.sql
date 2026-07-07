-- 完整的数据库设置脚本（包含创建表和插入数据）
-- 请在MySQL中执行此脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS anime_website CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE anime_website;

-- 删除已存在的表（按依赖关系顺序）
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS user_watch_history;
DROP TABLE IF EXISTS user_favorites;
DROP TABLE IF EXISTS episodes;
DROP TABLE IF EXISTS anime_categories;
DROP TABLE IF EXISTS anime;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    nickname VARCHAR(100) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户表';

-- 创建分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    image VARCHAR(500) COMMENT '分类图片',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '分类表';

-- 创建动漫表
CREATE TABLE anime (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '动漫标题',
    original_title VARCHAR(200) COMMENT '原始标题',
    description TEXT COMMENT '动漫描述',
    poster VARCHAR(500) COMMENT '海报图片URL',
    banner VARCHAR(500) COMMENT '横幅图片URL',
    year INT COMMENT '年份',
    season ENUM('SPRING', 'SUMMER', 'AUTUMN', 'WINTER') COMMENT '季节',
    status ENUM('ONGOING', 'COMPLETED', 'UPCOMING') DEFAULT 'UPCOMING' COMMENT '状态',
    total_episodes INT DEFAULT 0 COMMENT '总集数',
    current_episodes INT DEFAULT 0 COMMENT '当前集数',
    rating DECIMAL(3,1) DEFAULT 0.0 COMMENT '评分',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    view_count BIGINT DEFAULT 0 COMMENT '观看次数',
    is_featured BOOLEAN DEFAULT FALSE COMMENT '是否推荐',
    studio VARCHAR(100) COMMENT '制作公司',
    director VARCHAR(100) COMMENT '导演',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_title (title),
    INDEX idx_year (year),
    INDEX idx_status (status),
    INDEX idx_rating (rating),
    INDEX idx_view_count (view_count)
) COMMENT '动漫表';

-- 创建动漫分类关联表
CREATE TABLE anime_categories (
    anime_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (anime_id, category_id),
    FOREIGN KEY (anime_id) REFERENCES anime(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) COMMENT '动漫分类关联表';

-- 创建剧集表
CREATE TABLE episodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    anime_id BIGINT NOT NULL COMMENT '动漫ID',
    episode_number INT NOT NULL COMMENT '集数',
    title VARCHAR(200) COMMENT '集标题',
    description TEXT COMMENT '集描述',
    thumbnail VARCHAR(500) COMMENT '缩略图',
    video_url VARCHAR(500) COMMENT '视频URL',
    duration INT COMMENT '时长（秒）',
    air_date DATE COMMENT '播出日期',
    view_count BIGINT DEFAULT 0 COMMENT '观看次数',
    is_published BOOLEAN DEFAULT FALSE COMMENT '是否发布',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (anime_id) REFERENCES anime(id) ON DELETE CASCADE,
    UNIQUE KEY uk_anime_episode (anime_id, episode_number),
    INDEX idx_anime_id (anime_id),
    INDEX idx_episode_number (episode_number)
) COMMENT '剧集表';

-- 创建用户收藏表
CREATE TABLE user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    anime_id BIGINT NOT NULL COMMENT '动漫ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (anime_id) REFERENCES anime(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_anime (user_id, anime_id)
) COMMENT '用户收藏表';

-- 创建观看历史表
CREATE TABLE user_watch_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    anime_id BIGINT NOT NULL COMMENT '动漫ID',
    episode_id BIGINT COMMENT '剧集ID',
    watch_progress INT DEFAULT 0 COMMENT '观看进度（秒）',
    last_watch_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (anime_id) REFERENCES anime(id) ON DELETE CASCADE,
    FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_episode (user_id, episode_id),
    INDEX idx_user_id (user_id),
    INDEX idx_last_watch_time (last_watch_time)
) COMMENT '观看历史表';

-- 创建评论表
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    anime_id BIGINT NOT NULL COMMENT '动漫ID',
    episode_id BIGINT COMMENT '剧集ID（可选）',
    parent_id BIGINT COMMENT '父评论ID（回复）',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status ENUM('ACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE' COMMENT '评论状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (anime_id) REFERENCES anime(id) ON DELETE CASCADE,
    FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_anime_id (anime_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT '评论表';

-- ========================================
-- 数据插入部分
-- ========================================

-- 插入分类数据
INSERT INTO categories (name, description, image, sort_order) VALUES
('动作', '充满战斗和冒险的动漫作品', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 1),
('冒险', '探索未知世界的精彩旅程', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 2),
('喜剧', '轻松幽默的搞笑作品', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 3),
('剧情', '深度剧情和人物刻画', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 4),
('奇幻', '魔法与幻想的奇妙世界', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 5),
('恋爱', '浪漫温馨的爱情故事', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 6),
('科幻', '未来科技与太空探索', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 7),
('悬疑', '扣人心弦的推理故事', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 8);

-- 插入用户数据（密码为123456的BCrypt加密结果）
INSERT INTO users (username, email, password, nickname, avatar, role, status) VALUES
('admin', 'admin@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '管理员', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop&crop=face', 'ADMIN', 'ACTIVE'),
('user1', 'user1@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '动漫爱好者1', 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=100&h=100&fit=crop&crop=face', 'USER', 'ACTIVE'),
('user2', 'user2@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '动漫爱好者2', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face', 'USER', 'ACTIVE'),
('user3', 'user3@anime.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '动漫爱好者3', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face', 'USER', 'ACTIVE');

-- 插入动漫数据
INSERT INTO anime (title, original_title, description, poster, banner, year, season, status, total_episodes, current_episodes, rating, rating_count, view_count, is_featured, studio, director) VALUES
('进击的巨人 最终季', 'Shingeki no Kyojin: The Final Season', '《进击的巨人》是一部由諫山創创作的日本漫画作品。故事描述了人类在被巨人支配的世界中，为了生存而进行的战斗。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'WINTER', 'COMPLETED', 24, 24, 4.8, 15420, 2580000, TRUE, 'WIT Studio', '荒木哲郎'),

('鬼灭之刃 刀匠村篇', 'Kimetsu no Yaiba: Katanakaji no Sato-hen', '炭治郎前往刀匠村修复日轮刀，在那里遇到了新的挑战和强敌。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'SPRING', 'COMPLETED', 11, 11, 4.7, 12350, 1890000, TRUE, 'Ufotable', '外崎春雄'),

('咒术回战 第二季', 'Jujutsu Kaisen Season 2', '虎杖悠仁等人继续在咒术高专学习，面对更加强大的咒灵和复杂的咒术世界。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'SUMMER', 'ONGOING', 24, 18, 4.6, 9870, 1560000, TRUE, 'MAPPA', '朴性厚'),

('间谍过家家 第二季', 'SPY×FAMILY Season 2', '黄昏、约儿和阿尼亚组成的伪装家庭继续他们的日常生活。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'AUTUMN', 'ONGOING', 12, 8, 4.5, 8760, 1340000, FALSE, 'WIT Studio', '古橋一浩'),

('葬送的芙莉莲', 'Sousou no Frieren', '勇者一行在打败魔王后各自踏上了不同的人生道路，而精灵法师芙莉莲开始了她漫长的旅程。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=400&fit=crop', 2023, 'AUTUMN', 'ONGOING', 28, 16, 4.9, 11200, 980000, TRUE, 'Madhouse', '斎藤圭一郎');

-- 插入动漫分类关联数据
INSERT INTO anime_categories (anime_id, category_id) VALUES
-- 进击的巨人：动作、剧情、奇幻
(1, 1), (1, 4), (1, 5),
-- 鬼灭之刃：动作、冒险、奇幻
(2, 1), (2, 2), (2, 5),
-- 咒术回战：动作、奇幻、悬疑
(3, 1), (3, 5), (3, 8),
-- 间谍过家家：喜剧、动作、剧情
(4, 3), (4, 1), (4, 4),
-- 葬送的芙莉莲：奇幻、冒险、剧情
(5, 5), (5, 2), (5, 4);

-- 插入剧集数据
INSERT INTO episodes (anime_id, episode_number, title, description, thumbnail, video_url, duration, air_date, view_count, is_published, published_at) VALUES
-- 进击的巨人 剧集
(1, 1, '审判', '艾伦等人面临军事法庭的审判，真相逐渐浮出水面。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/aot_s4_ep1.mp4', 1440, '2023-01-09', 580000, TRUE, '2023-01-09 21:00:00'),
(1, 2, '恶魔之子', '艾伦的真实身份被揭露，引发了巨大的争议。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/aot_s4_ep2.mp4', 1440, '2023-01-16', 520000, TRUE, '2023-01-16 21:00:00'),
(1, 3, '两个兄弟', '艾伦和吉克的过去被揭示，兄弟之间的复杂关系。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/aot_s4_ep3.mp4', 1440, '2023-01-23', 490000, TRUE, '2023-01-23 21:00:00'),

-- 鬼灭之刃 剧集
(2, 1, '有人的村子', '炭治郎来到刀匠村，遇到了新的伙伴和挑战。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/kny_s3_ep1.mp4', 1440, '2023-04-09', 450000, TRUE, '2023-04-09 21:00:00'),
(2, 2, '缘壱零式', '炭治郎发现了传说中的训练用人偶。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/kny_s3_ep2.mp4', 1440, '2023-04-16', 420000, TRUE, '2023-04-16 21:00:00'),
(2, 3, '300年前的刀', '古老的日轮刀中隐藏着重要的秘密。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/kny_s3_ep3.mp4', 1440, '2023-04-23', 410000, TRUE, '2023-04-23 21:00:00'),

-- 咒术回战 剧集
(3, 1, '懐玉', '五条悟和夏油杰的学生时代故事开始。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/jjk_s2_ep1.mp4', 1440, '2023-07-06', 380000, TRUE, '2023-07-06 21:00:00'),
(3, 2, '隐藏的库存', '过去篇继续，展现年轻时期的强者们。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/jjk_s2_ep2.mp4', 1440, '2023-07-13', 360000, TRUE, '2023-07-13 21:00:00'),
(3, 3, '星浆体', '天内理子的故事，星浆体的秘密。', 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop', 'https://example.com/video/jjk_s2_ep3.mp4', 1440, '2023-07-20', 340000, TRUE, '2023-07-20 21:00:00');

-- 插入用户收藏数据
INSERT INTO user_favorites (user_id, anime_id) VALUES
(2, 1), (2, 2), (2, 5),  -- user1收藏
(3, 1), (3, 3), (3, 4),  -- user2收藏
(4, 2), (4, 5);          -- user3收藏

-- 插入观看历史数据
INSERT INTO user_watch_history (user_id, anime_id, episode_id, watch_progress) VALUES
(2, 1, 1, 1200),  -- user1观看进度
(2, 1, 2, 800),
(2, 2, 4, 1440),  -- 看完整集
(3, 1, 1, 1440),  -- user2观看进度
(3, 3, 7, 600),
(4, 2, 4, 900);   -- user3观看进度

-- 插入评论数据
INSERT INTO comments (user_id, anime_id, episode_id, content, like_count) VALUES
(2, 1, 1, '进击的巨人真的太精彩了！每一集都让人欲罢不能！', 15),
(3, 1, 1, '艾伦的成长真的让人感动，期待后续剧情发展。', 8),
(4, 1, NULL, '这部动漫的制作质量真的很高，推荐大家观看！', 12),
(2, 2, 4, '鬼灭之刃的战斗场面太燃了！', 20),
(3, 2, NULL, 'Ufotable的制作真的是业界良心！', 18),
(4, 3, 7, '咒术回战的设定很有创意，角色也很有魅力。', 10);

-- 验证数据插入结果
SELECT '=== 数据插入完成 ===' AS message;
SELECT
    '分类' AS table_name, COUNT(*) AS count FROM categories
UNION ALL
SELECT
    '用户' AS table_name, COUNT(*) AS count FROM users
UNION ALL
SELECT
    '动漫' AS table_name, COUNT(*) AS count FROM anime
UNION ALL
SELECT
    '剧集' AS table_name, COUNT(*) AS count FROM episodes
UNION ALL
SELECT
    '收藏' AS table_name, COUNT(*) AS count FROM user_favorites
UNION ALL
SELECT
    '观看历史' AS table_name, COUNT(*) AS count FROM user_watch_history
UNION ALL
SELECT
    '评论' AS table_name, COUNT(*) AS count FROM comments;
