# 数据库初始化说明

## 📁 文件说明

### 1. `init.sql` - 数据库结构初始化
- 创建所有数据库表
- 设置外键约束
- 创建索引
- **用途**：首次创建数据库时使用

### 2. `manual-insert.sql` - 完整测试数据
- 包含完整的测试数据集
- 8个分类、4个用户、8部动漫、12集剧集
- 包含收藏、观看历史、评论等关联数据
- **用途**：需要完整测试数据时使用

### 3. `quick-insert.sql` - 快速测试数据
- 包含基础测试数据
- 使用 `INSERT IGNORE` 避免重复插入
- 数据量较少，适合快速测试
- **用途**：快速验证功能时使用

## 🚀 使用方法

### 方法一：手动执行SQL文件

1. **打开MySQL客户端**（如MySQL Workbench、Navicat等）

2. **连接到数据库**
   ```sql
   -- 确保数据库存在
   CREATE DATABASE IF NOT EXISTS anime_website CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE anime_website;
   ```

3. **选择执行的SQL文件**：
   
   **完整数据插入**（推荐）：
   ```bash
   # 执行 manual-insert.sql 文件
   # 包含所有测试数据，适合开发和测试
   ```
   
   **快速数据插入**：
   ```bash
   # 执行 quick-insert.sql 文件
   # 基础数据，适合快速验证
   ```

### 方法二：命令行执行

```bash
# 进入项目目录
cd backend/src/main/resources/db

# 执行完整数据插入
mysql -u root -p anime_website < manual-insert.sql

# 或执行快速数据插入
mysql -u root -p anime_website < quick-insert.sql
```

## 📊 测试数据说明

### 用户账号
| 用户名 | 密码 | 角色 | 邮箱 |
|--------|------|------|------|
| admin | 123456 | 管理员 | admin@anime.com |
| user1 | 123456 | 普通用户 | user1@anime.com |
| user2 | 123456 | 普通用户 | user2@anime.com |
| user3 | 123456 | 普通用户 | user3@anime.com |

### 动漫数据
- **进击的巨人 最终季** - 已完结，24集
- **鬼灭之刃 刀匠村篇** - 已完结，11集
- **咒术回战 第二季** - 连载中，18/24集
- **间谍过家家 第二季** - 连载中，8/12集
- **葬送的芙莉莲** - 连载中，16/28集
- **药屋少女的呢喃** - 连载中，12/24集
- **转生史莱姆 第三季** - 即将播出

### 分类数据
- 动作、冒险、喜剧、剧情、奇幻、恋爱、科幻、悬疑

## ⚠️ 注意事项

1. **数据库配置**：确保 `application.yml` 中的数据库连接信息正确
2. **字符编码**：数据库和表使用 `utf8mb4` 编码
3. **外键约束**：删除数据时注意外键依赖关系
4. **密码加密**：用户密码使用 BCrypt 加密，原始密码为 `123456`

## 🔧 故障排除

### 问题1：外键约束错误
```sql
-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;
-- 执行SQL语句
-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
```

### 问题2：重复数据错误
- 使用 `quick-insert.sql`，它包含 `INSERT IGNORE` 语句
- 或者先清空相关表再插入数据

### 问题3：字符编码问题
```sql
-- 检查数据库编码
SHOW CREATE DATABASE anime_website;

-- 修改数据库编码
ALTER DATABASE anime_website CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 📝 验证数据插入

执行以下SQL验证数据是否插入成功：

```sql
-- 检查各表数据量
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
```

预期结果（完整数据）：
- 分类：8条
- 用户：4条
- 动漫：8条
- 剧集：12条
- 收藏：9条
- 观看历史：9条
- 评论：12条
