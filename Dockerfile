# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --uid 1001 spring

COPY --from=builder /app/build/libs/hearo-admin-backend.jar /app/app.jar

USER 1001

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
