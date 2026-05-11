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

# Sertifika PDF üretimi için Unicode font desteği (Turkish chars: ş, ğ, ı, ç, ö, ü)
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Builder'dan jar dosyasını kopyala
COPY --from=builder /app/target/*.jar app.jar

# Health check — verify port 8080 is accepting connections
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD bash -c 'echo > /dev/tcp/localhost/8080' || exit 1

# Port
EXPOSE 8080

# Çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]
