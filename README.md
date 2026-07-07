# 动漫网站项目

一个基于Vue 3 + Spring Boot的现代化动漫网站，支持视频播放、用户管理、管理员后台等功能。

## 技术栈

### 前端
- Vue 3 + Composition API
- Vite（构建工具）
- Element Plus（UI组件库）
- Vue Router（路由管理）
- Pinia（状态管理）
- Axios（HTTP客户端）
- Video.js（视频播放器）

### 后端
- Spring Boot 2.7.18
- Spring Security（认证授权）
- Spring Data JPA（数据访问）
- MySQL 8.0（数据库）
- Redis（缓存）
- JWT（token认证）

## 项目结构

```
Anime/
├── frontend/          # Vue前端项目
│   ├── src/
│   │   ├── views/     # 页面组件
│   │   ├── components/# 通用组件
│   │   ├── router/    # 路由配置
│   │   ├── store/     # 状态管理
│   │   ├── api/       # API接口
│   │   └── utils/     # 工具函数
├── backend/           # Spring Boot后端项目
│   ├── src/main/java/com/anime/website/
│   │   ├── entity/    # 实体类
│   │   ├── repository/# 数据访问层
│   │   ├── service/   # 业务逻辑层
│   │   ├── controller/# 控制器层
│   │   ├── config/    # 配置类
│   │   └── util/      # 工具类
└── docs/              # 项目文档
```

## 快速开始

### 1. 环境要求
- Node.js 16+
- Java 8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE anime_website CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p anime_website < docs/init-database.sql
```

3. 修改后端配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    username: 你的MySQL用户名
    password: 你的MySQL密码
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:5173 启动

### 5. 访问应用

- 前端页面：http://localhost:5173
- 后端API：http://localhost:8080/api
- API健康检查：http://localhost:8080/api/test/health

## 默认账户

- 管理员账户：admin / admin123

## 开发说明

### 后端API测试
```bash
# 健康检查
curl http://localhost:8080/api/test/health

# 版本信息
curl http://localhost:8080/api/test/version
```

### 前端开发
- 开发服务器会自动热重载
- 使用Element Plus组件库
- 遵循Vue 3 Composition API规范

### 后端开发
- 使用Spring Boot 2.7.18
- 数据库自动建表（ddl-auto: update）
- 支持跨域请求
- 开发阶段禁用Spring Security认证

## 功能特性

### 用户端功能
- [x] 首页展示
- [x] 动漫列表浏览
- [ ] 动漫详情查看
- [ ] 视频播放
- [ ] 用户注册登录
- [ ] 收藏功能
- [ ] 观看历史
- [ ] 评分评论

### 管理员功能
- [ ] 动漫管理
- [ ] 用户管理
- [ ] 内容审核
- [ ] 数据统计

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License
