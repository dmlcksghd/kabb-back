# Multi-stage build를 사용하여 최종 이미지 크기 최적화
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Gradle wrapper와 build 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 빌드 실행
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# 런타임 이미지
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

