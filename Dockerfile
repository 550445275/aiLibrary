# 构建在宿主机/CI 完成：npm run build、Maven package 后，本镜像只复制 JAR。
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/library-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
