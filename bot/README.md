---

<h1 align="center">Telegram Bot Documentation</h1>

---

## Быстрый старт

### 1. Запуск системы

```bash
# Python Bot
cd bot
python monitoring_bot.py

### 2. Тестирование API

```bash
# Проверка здоровья
curl http://localhost:5000/health
curl http://localhost:8080/actuator/health

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

### 3. Регистрация в Telegram боте

1. Найдите бота: `@PingTower_tax_bot`
2. Отправьте `/start`
3. Зарегистрируйтесь с логином и паролем

### Локальная разработка
```bash
# Python Bot
cd bot && python monitoring_bot.py

### Docker
```bash
docker-compose up -d
```
