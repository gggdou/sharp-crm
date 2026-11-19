# ===== 后端镜像：多阶段构建 =====
# 构建上下文为仓库根目录，Dockerfile 位于 ops/ 下
# 阶段1 构建：Maven 编译打包
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY backend/pom.xml backend/
COPY backend/sharp-crm-common/pom.xml backend/sharp-crm-common/
COPY backend/sharp-crm-entity/pom.xml backend/sharp-crm-entity/
COPY backend/sharp-crm-management/pom.xml backend/sharp-crm-management/
RUN mvn -B -q -f backend/pom.xml dependency:go-offline -pl sharp-crm-management -am || true
COPY backend/ backend/
RUN mvn -B -q -f backend/pom.xml clean package -DskipTests -pl sharp-crm-management -am

# 阶段2 运行：轻量 JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/backend/sharp-crm-management/target/sharp-crm-management-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
