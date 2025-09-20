# 📋 Требования для Java Backend

## ☕ Java Runtime

**Требуемая версия:** Java 17 LTS или выше

**Рекомендуемая:** Java 17 LTS (Eclipse Temurin)

### Установка Java 17 LTS

1. **Скачайте Java 17 LTS:**
   - Перейдите на: https://adoptium.net/temurin/releases/
   - Выберите **Java 17 LTS**
   - Выберите **Windows x64**
   - Скачайте **JDK** (не JRE)

2. **Установите Java:**
   - Запустите скачанный installer
   - Следуйте инструкциям установщика
   - Запомните путь установки

3. **Настройте переменные окружения:**
   ```
   JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot
   PATH=%JAVA_HOME%\bin;%PATH%
   ```

4. **Проверьте установку:**
   ```bash
   java -version
   javac -version
   ```

## 🏗️ Build Tool

**Gradle** - уже настроен в проекте

**Проверка:**
```bash
./gradlew --version
```

## 🗄️ База данных

**PostgreSQL** - для продакшена
**H2** - для разработки (встроенная)

## 🐳 Docker (опционально)

**Docker** и **Docker Compose** для контейнеризации

## 📦 Зависимости

Все зависимости управляются через `build.gradle`:

- **Spring Boot 3.5.6**
- **Spring Security**
- **Spring Data JPA**
- **Spring WebFlux** (для HTTP клиентов)
- **Spring Mail** (для email)
- **PostgreSQL Driver**
- **ClickHouse Driver**
- **OpenAPI/Swagger**

## 🚀 Запуск

```bash
# Установка зависимостей
./gradlew build

# Запуск приложения
./gradlew bootRun

# Или через JAR
./gradlew bootJar
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

## 🔧 Проблемы и решения

### Ошибка: "Dependency requires at least JVM runtime version 17"

**Решение:**
1. Установите Java 17+ 
2. Обновите переменную JAVA_HOME
3. Перезапустите терминал
4. Проверьте: `java -version`

### Ошибка: "Could not resolve all artifacts"

**Решение:**
```bash
./gradlew clean build
```

### Ошибка: "Port 8080 already in use"

**Решение:**
```bash
# Измените порт в application.yaml
server.port: 8081
```

## 📚 Полезные ссылки

- [Java 17 LTS Download](https://adoptium.net/temurin/releases/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Gradle Documentation](https://gradle.org/docs/)
