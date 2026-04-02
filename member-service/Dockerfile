# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM gradle:9.0.0-jdk21-alpine AS build
WORKDIR /app

COPY . .
RUN chmod +x ./gradlew && ./gradlew :member-service:bootJar -x test --no-daemon

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/member-service/build/libs/member-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-jar", "app.jar"]
