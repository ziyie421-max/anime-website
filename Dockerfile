# 多阶段构建：前端打进后端 jar，单镜像单域名部署（Render / 任意 Docker 环境）
#   - 阶段1：构建 Vue 前端 -> dist
#   - 阶段2：把 dist 烘焙进 Spring Boot 的 resources/static，再 mvn 打包成可执行 jar
#   - 阶段3：精简 JRE 运行时

# ---------- 阶段1：前端构建 ----------
FROM node:18-alpine AS frontend-builder
WORKDIR /app/frontend
# 先拷 package 文件，利用 Docker 层缓存加速依赖安装
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------- 阶段2：后端构建（前端 dist 打进静态资源） ----------
FROM maven:3.8-eclipse-temurin-8 AS backend-builder
WORKDIR /app/backend
# 先拷 pom，缓存依赖
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
# 拷贝后端源码
COPY backend/src ./src
# 把前端构建产物烘焙进 jar 的静态资源目录（Spring Boot 会在根路径 / 提供 index.html 与 /assets/**）
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static
# 打包（跳过测试以加速；本地已验证）
RUN mvn -B -DskipTests package

# ---------- 阶段3：运行时 ----------
FROM eclipse-temurin:8-jre AS runtime
WORKDIR /app
# 默认启用 prod profile；可在运行时用环境变量覆盖
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xmx512m"
COPY --from=backend-builder /app/backend/target/*.jar app.jar
# Render 通过 $PORT 注入端口；application-prod.yml 里 server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
