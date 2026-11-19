# ===== 后端镜像：多阶段构建 =====
# 阶段1 构建：Maven 编译打包
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml ./
COPY sharp-crm-common/pom.xml sharp-crm-common/
COPY sharp-crm-entity/pom.xml sharp-crm-entity/
COPY sharp-crm-management/pom.xml sharp-crm-management/
RUN mvn -B -q dependency:go-offline -pl sharp-crm-management -am || true
COPY sharp-crm-common/ sharp-crm-common/
COPY sharp-crm-entity/ sharp-crm-entity/
COPY sharp-crm-management/ sharp-crm-management/
RUN mvn -B -q clean package -DskipTests -pl sharp-crm-management -am

# 阶段2 运行：轻量 JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/sharp-crm-management/target/sharp-crm-management-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
