# syntax=docker/dockerfile:1

# Build context: repository root
#   docker build -f deployment/Dockerfile -t crediya-solicitudes .

# ---------- Build stage ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY . .
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    chmod +x gradlew && \
    ./gradlew :app-service:bootJar -x test --no-daemon --console=plain

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S -G appgroup appuser

COPY --from=build --chown=appuser:appgroup /workspace/applications/app-service/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

USER appuser
EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- "http://localhost:${SERVER_PORT:-8081}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
