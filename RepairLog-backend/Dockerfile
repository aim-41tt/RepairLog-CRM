# ════════════════════════════════════════════════════════════════════════════
# CRM RepairLog Backend — Multi-stage Dockerfile
# Author: aim-41tt
# ════════════════════════════════════════════════════════════════════════════

# ---------------------------------------------------------------------------
# Stage 1: Build — собираем JAR через Maven
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Сначала копируем только pom.xml — Maven закеширует зависимости
# При изменении исходников этот слой НЕ пересобирается
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------------------------------------------------------------------------
# Stage 2: Runtime — минимальный образ только с JRE
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="aim-41tt"
LABEL description="CRM RepairLog Backend"
LABEL version="1.0.0"

# Непривилегированный пользователь — не запускаем от root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Забираем JAR из build-стадии
COPY --from=build /app/target/*.jar app.jar

# Директория для логов (prod-профиль пишет сюда)
RUN mkdir -p /app/logs /var/log/repairlog \
    && chown -R spring:spring /app /var/log/repairlog

USER spring:spring

EXPOSE 8080

# Проверка здоровья — compose и оркестраторы ориентируются на этот endpoint
HEALTHCHECK --interval=60s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# G1GC + ограничение RAM = предсказуемое поведение в контейнере
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
