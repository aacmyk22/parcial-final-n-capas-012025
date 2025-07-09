# ───────────────────── etapa de build ─────────────────────
FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# ──────────────────── etapa de runtime ────────────────────
FROM eclipse-temurin:21-jre
RUN useradd -ms /bin/bash spring
USER spring
WORKDIR /home/spring

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
