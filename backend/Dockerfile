# Multi-stage build для оптимизации размера образа
FROM gradle:8.5-jdk21-alpine AS build

# Установка рабочей директории
WORKDIR /app

# Копирование файлов конфигурации Gradle
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# Копирование исходного кода
COPY src/ src/

# Сборка приложения
RUN gradle build -x test --no-daemon

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Установка необходимых пакетов
RUN apk add --no-cache wget

# Создание пользователя для безопасности
RUN addgroup -g 1000 pingtower && adduser -D -s /bin/sh -u 1000 -G pingtower pingtower

# Установка рабочей директории
WORKDIR /app

# Копирование JAR файла из build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Изменение владельца файлов
RUN chown -R pingtower:pingtower /app

# Переключение на непривилегированного пользователя
USER pingtower

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/v1/actuator/health || exit 1

# Открытие порта
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
