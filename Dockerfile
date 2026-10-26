# ============================================================
# 二手集市（ersho）Spring Boot 应用镜像
# 两阶段构建：builder 里编译出 jar，runner 里只放 JRE + jar
# ============================================================

# ---------- 阶段 1：编译 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只拷 pom 预热依赖缓存：mvn 在没源码时 packaging=pom 的 pom 也能把依赖拉到本地仓库。
# 这样以后只改 Java 代码时，这一层还能命中缓存，不用重新下依赖（第一次构建仍然要联网）。
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline || true

# 再拷源码编译（这一步 mvn 用的是容器里自己的中央仓库，不依赖 settings.xml 的阿里云镜像）
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---------- 阶段 2：运行 ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 时区 + curl（给 HEALTHCHECK 用）
# 注意：不用 tzdata 的交互式配置，直接设 TZ 环境变量最省事
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl tzdata \
    && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Shanghai \
    SERVER_PORT=8080 \
    SERVER_CONTEXT_PATH=/api \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8"

# 只把 jar 拷进来，不拷源码
COPY --from=builder /build/target/*.jar app.jar

# 说明：容器里不写死数据库地址，全部通过环境变量注入
#   MYSQL_URL / MYSQL_USERNAME / MYSQL_PASSWORD
# 见 docker-compose.yml

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
    CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}${SERVER_CONTEXT_PATH}/" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
