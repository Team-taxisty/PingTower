# 📱 Руководство по системе уведомлений PingTower

## 🏗️ Архитектура

Система уведомлений PingTower состоит из двух основных компонентов:

1. **Python Bot** (`bot/monitoring_bot.py`) - Telegram бот для получения уведомлений
2. **Java Backend** - REST API для отправки уведомлений

## 🚀 Быстрый старт

### 1. Запуск Python бота

```bash
cd bot
python monitoring_bot.py
```

Бот будет доступен на:
- **Telegram**: https://t.me/PingTower_tax_bot
- **API**: http://localhost:5000/send_notification

### 2. Запуск Java бэкенда

```bash
cd backend
./gradlew bootRun
```

API будет доступен на: http://localhost:8080

## 📡 Способы отправки уведомлений

### Способ 1: Прямое обращение к Python боту

**Endpoint**: `POST http://localhost:5000/send_notification`

**Тело запроса**:
```json
{
    "username": "testuser",
    "service_name": "My Web Server",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Сервер не отвечает"
}
```

**Пример с curl**:
```bash
curl -X POST http://localhost:5000/send_notification \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "service_name": "My Web Server",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Сервер не отвечает"
  }'
```

### Способ 2: Через Java бэкенд

**Endpoint**: `POST http://localhost:8080/notifications/send`

**Тело запроса**:
```json
{
    "username": "testuser",
    "serviceName": "My Web Server",
    "serviceUrl": "https://example.com",
    "status": "down",
    "severity": "ERROR",
    "message": "Сервер не отвечает"
}
```

**Пример с curl**:
```bash
curl -X POST http://localhost:8080/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "serviceName": "My Web Server",
    "serviceUrl": "https://example.com",
    "status": "down",
    "severity": "ERROR",
    "message": "Сервер не отвечает"
  }'
```

## 🧪 Тестирование

### Автоматическое тестирование

Запустите тестовый скрипт:

```bash
cd bot
python test_notification.py
```

Скрипт предложит выбрать способ тестирования:
1. Прямое обращение к Python боту
2. Через Java бэкенд
3. Оба способа

### Ручное тестирование

1. **Регистрация в боте**:
   - Отправьте `/start` боту в Telegram
   - Пройдите регистрацию с логином и паролем

2. **Отправка тестового уведомления**:
   - Используйте один из способов выше
   - Проверьте получение уведомления в Telegram

## 🔧 Настройка каналов уведомлений

### Создание канала для Python бота

```bash
curl -X POST http://localhost:8080/notifications/channels \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PYTHON_BOT",
    "name": "Python Bot Channel",
    "configuration": "{\"botUrl\": \"http://localhost:5000\"}",
    "isDefault": true
  }'
```

### Создание канала для Telegram

```bash
curl -X POST http://localhost:8080/notifications/channels \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TELEGRAM",
    "name": "Telegram Channel",
    "configuration": "{\"botToken\": \"YOUR_BOT_TOKEN\", \"chatId\": \"YOUR_CHAT_ID\"}",
    "isDefault": false
  }'
```

## 📊 Мониторинг

### Проверка здоровья Python бота

```bash
curl http://localhost:5000/health
```

### Проверка каналов уведомлений

```bash
curl http://localhost:8080/notifications/channels
```

### Проверка доставки уведомлений

```bash
curl http://localhost:8080/notifications/deliveries
```

## 🔐 Безопасность

1. **Регистрация пользователей**: Только зарегистрированные пользователи получают уведомления
2. **Валидация данных**: Все входящие данные проверяются
3. **Логирование**: Все операции логируются для аудита

## 🐛 Устранение неполадок

### Проблема: Бот не отвечает

**Решение**:
1. Проверьте, что бот запущен: `curl http://localhost:5000/health`
2. Проверьте токен бота в `.env` файле
3. Убедитесь, что пользователь зарегистрирован в боте

### Проблема: Java бэкенд не отправляет уведомления

**Решение**:
1. Проверьте, что канал уведомлений создан
2. Убедитесь, что Python бот доступен с Java бэкенда
3. Проверьте логи Java приложения

### Проблема: Уведомления не доходят в Telegram

**Решение**:
1. Убедитесь, что пользователь зарегистрирован в боте
2. Проверьте правильность username в запросе
3. Убедитесь, что бот не заблокирован пользователем

## 📝 Примеры интеграции

### Python скрипт для отправки уведомлений

```python
import requests

def send_notification(username, service_name, status, message):
    url = "http://localhost:5000/send_notification"
    data = {
        "username": username,
        "service_name": service_name,
        "status": status,
        "message": message
    }
    
    try:
        response = requests.post(url, json=data)
        return response.status_code == 200
    except Exception as e:
        print(f"Ошибка отправки уведомления: {e}")
        return False

# Использование
send_notification("testuser", "My API", "down", "Сервер недоступен")
```

### Bash скрипт для мониторинга

```bash
#!/bin/bash

SERVICE_URL="https://example.com"
USERNAME="testuser"
SERVICE_NAME="Example Service"

# Проверка доступности
if curl -f -s "$SERVICE_URL" > /dev/null; then
    STATUS="up"
    MESSAGE="Сервис восстановлен"
else
    STATUS="down"
    MESSAGE="Сервис недоступен"
fi

# Отправка уведомления
curl -X POST http://localhost:5000/send_notification \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"service_name\": \"$SERVICE_NAME\",
    \"service_url\": \"$SERVICE_URL\",
    \"status\": \"$STATUS\",
    \"message\": \"$MESSAGE\"
  }"
```

## 📚 Дополнительные ресурсы

- [Telegram Bot API](https://core.telegram.org/bots/api)
- [Spring Boot REST API](https://spring.io/guides/gs/rest-service/)
- [Python Flask](https://flask.palletsprojects.com/)

## 🤝 Поддержка

При возникновении проблем:
1. Проверьте логи приложений
2. Убедитесь в правильности конфигурации
3. Обратитесь к администратору системы
