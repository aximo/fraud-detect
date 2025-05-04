# 使用 Eclipse Temurin 提供的 Java 11 JDK 镜像
FROM eclipse-temurin:11-jdk-jammy

# 可选：添加 JVM 启动参数
ENV JAVA_OPTS=""

# 拷贝 Spring Boot 可执行 JAR
COPY target/fraud-detector-*.jar app.jar

# 设置容器入口
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]