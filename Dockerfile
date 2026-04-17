# Multi-stage build
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

# pom.xml ve dependencies'leri kopyala
COPY pom.xml .
RUN mvn dependency:resolve -DskipTests

# Kaynak kodunu kopyala
COPY src ./src

# Build et
RUN mvn clean package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Builder'dan jar dosyasını kopyala
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.launch.JarLauncher \
  || exit 1

# Port
EXPOSE 8080

# Çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]
