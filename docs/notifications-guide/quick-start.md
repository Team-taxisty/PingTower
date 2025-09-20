# 🚀 Быстрый старт

## Предварительные требования

- Python 3.8+
- Java 17+
- Telegram Bot Token
- Docker (опционально)

## 1. Запуск Python бота

### Установка зависимостей

```bash
cd bot
pip install -r requirements.txt
```

### Настройка окружения

Создайте файл `.env` в директории `bot/`:

```env
TELEGRAM_BOT_TOKEN=your_bot_token_here
DATABASE_URL=sqlite:///monitoring_bot.db
BOT_URL=http://localhost:5000
```

### Запуск бота

```bash
cd bot
python monitoring_bot.py
```

Бот будет доступен на:
- **Telegram**: https://t.me/PingTower_tax_bot
- **API**: http://localhost:5000/send_notification

### Проверка работы

```bash
curl http://localhost:5000/health
```

Ожидаемый ответ:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00"
}
```

## 2. Запуск Java бэкенда

### Установка зависимостей

```bash
cd backend
./gradlew build
```

### Настройка конфигурации

Отредактируйте `src/main/resources/application.yaml`:

```yaml
notifications:
  python-bot:
    url: http://localhost:5000
  channels:
    default-type: PYTHON_BOT

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pingtower
    username: pingtower
    password: password
```

### Запуск приложения

```bash
cd backend
./gradlew bootRun
```

API будет доступен на: http://localhost:8080

### Проверка работы

```bash
curl http://localhost:8080/actuator/health
```

## 3. Регистрация в Telegram боте

### Шаг 1: Найти бота

1. Откройте Telegram
2. Найдите бота: `@PingTower_tax_bot`
3. Нажмите "Start"

### Шаг 2: Регистрация

1. Отправьте команду `/start`
2. Введите ваш логин (username)
3. Введите пароль
4. Подтвердите регистрацию

### Шаг 3: Проверка регистрации

```bash
curl http://localhost:5000/users
```

## 4. Создание канала уведомлений

### Создание канала для Python бота

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Python Bot Channel",
    "type": "PYTHON_BOT",
    "configuration": {
      "botUrl": "http://localhost:5000"
    },
    "enabled": true
  }'
```

### Создание канала для Telegram

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Telegram Channel",
    "type": "TELEGRAM",
    "configuration": {
      "botToken": "YOUR_BOT_TOKEN",
      "chatId": "YOUR_CHAT_ID"
    },
    "enabled": true
  }'
```

## 5. Тестирование системы

### Автоматическое тестирование

```bash
cd bot
python test_notification.py
```

Скрипт предложит выбрать способ тестирования:
1. Прямое обращение к Python боту
2. Через Java бэкенд
3. Оба способа

### Ручное тестирование

#### Способ 1: Прямое обращение к Python боту

```bash
curl -X POST http://localhost:5000/send_notification \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "service_name": "Test Service",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Сервер не отвечает"
  }'
```

#### Способ 2: Через Java бэкенд

```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "your_username",
    "serviceName": "Test Service",
    "serviceUrl": "https://example.com",
    "status": "down",
    "severity": "ERROR",
    "message": "Сервер не отвечает"
  }'
```

## 6. Проверка доставки

### Проверка истории доставки

```bash
curl http://localhost:8080/api/v1/notifications/deliveries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Проверка каналов уведомлений

```bash
curl http://localhost:8080/api/v1/notifications/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 7. Настройка мониторинга

### Создание сервиса для мониторинга

```bash
curl -X POST http://localhost:8080/api/v1/services \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "My Web Service",
    "url": "https://example.com/health",
    "checkInterval": 60,
    "timeout": 30,
    "enabled": true
  }'
```

### Настройка алертов

```bash
curl -X POST http://localhost:8080/api/v1/alerts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "serviceId": 1,
    "message": "Service is down",
    "severity": "ERROR",
    "enabled": true
  }'
```

## 8. Docker развертывание

### Запуск через Docker Compose

```bash
# Создайте docker-compose.yml
version: '3.8'
services:
  python-bot:
    build: ./bot
    ports:
      - "5000:5000"
    environment:
      - TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
    volumes:
      - ./bot/monitoring_bot.db:/app/monitoring_bot.db
  
  java-backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/pingtower
      - SPRING_DATASOURCE_USERNAME=pingtower
      - SPRING_DATASOURCE_PASSWORD=password
    depends_on:
      - python-bot
      - postgres
  
  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=pingtower
      - POSTGRES_USER=pingtower
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Запуск

```bash
docker-compose up -d
```

## 9. Проверка работоспособности

### Проверка всех компонентов

```bash
# Python Bot
curl http://localhost:5000/health

# Java Backend
curl http://localhost:8080/actuator/health

# PostgreSQL
docker exec -it pingtower_postgres_1 psql -U pingtower -d pingtower -c "SELECT 1;"
```

### Проверка интеграции

```bash
# Отправка тестового уведомления
curl -X POST http://localhost:5000/send_notification \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "service_name": "Test Service",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Test notification"
  }'
```

## 10. Следующие шаги

1. **Настройте мониторинг сервисов** - добавьте ваши сервисы для мониторинга
2. **Настройте алерты** - создайте правила для отправки уведомлений
3. **Настройте каналы** - добавьте дополнительные каналы уведомлений
4. **Мониторинг системы** - настройте мониторинг самой системы уведомлений
5. **Безопасность** - настройте аутентификацию и авторизацию

## Устранение проблем

### Бот не запускается
- Проверьте токен бота в `.env` файле
- Убедитесь, что порт 5000 свободен
- Проверьте логи: `python monitoring_bot.py`

### Java Backend не запускается
- Проверьте подключение к базе данных
- Убедитесь, что порт 8080 свободен
- Проверьте логи: `./gradlew bootRun`

### Уведомления не доходят
- Проверьте регистрацию пользователя в боте
- Убедитесь, что канал уведомлений создан
- Проверьте логи Python бота и Java Backend
