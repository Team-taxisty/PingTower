# PingTower Documentation

Документация проекта и спецификации API системы мониторинга PingTower.

## Структура документации

### 📚 Основные руководства

- **[API Guide](./api-guide/README.md)** - Полное руководство по REST API
- **[Notifications Guide](./notifications-guide/README.md)** - Руководство по системе уведомлений

### 🎮 API Controllers

- **[AlertController](./controllers/AlertController.md)** - Управление алертами и уведомлениями
- **[MonitoringDataController](./controllers/MonitoringDataController.md)** - Данные мониторинга и аналитика
- **[RunsController](./controllers/RunsController.md)** - История запусков проверок

### 📖 Детальная документация

#### API Guide
- [Аутентификация](./api-guide/authentication.md) - Регистрация, вход, управление пользователями
- [Основные эндпоинты](./api-guide/endpoints.md) - Дашборды, мониторинг, метрики
- [Примеры использования](./api-guide/examples.md) - Практические примеры интеграции
- [Обработка ошибок](./api-guide/error-handling.md) - Коды ошибок и их обработка
- [Производительность](./api-guide/performance.md) - Рекомендации по оптимизации
- [Безопасность](./api-guide/security.md) - Лучшие практики безопасности

#### Notifications Guide
- [Архитектура](./notifications-guide/architecture.md) - Обзор системы уведомлений
- [Быстрый старт](./notifications-guide/quick-start.md) - Запуск и настройка
- [Способы отправки](./notifications-guide/sending-methods.md) - Различные методы отправки уведомлений
- [Тестирование](./notifications-guide/testing.md) - Тестирование системы уведомлений
- [Настройка каналов](./notifications-guide/channel-setup.md) - Настройка каналов уведомлений
- [Мониторинг](./notifications-guide/monitoring.md) - Мониторинг работы системы
- [Устранение неполадок](./notifications-guide/troubleshooting.md) - Решение проблем
- [Примеры интеграции](./notifications-guide/integration-examples.md) - Практические примеры

## Быстрый старт

### 1. Запуск системы

```bash
# Python Bot
cd bot
python monitoring_bot.py

# Java Backend
cd backend
./gradlew bootRun
```

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

## Основные компоненты

### 🐍 Python Bot
- Telegram бот для получения уведомлений
- API для отправки уведомлений
- Управление пользователями
- Хранение данных в SQLite

### ☕ Java Backend
- REST API для мониторинга
- Управление сервисами и алертами
- Интеграция с Python ботом
- Хранение данных в PostgreSQL

### 📱 Telegram Integration
- Уведомления в реальном времени
- Управление пользователями
- Статус сервисов
- История алертов

## Поддерживаемые каналы уведомлений

- **Python Bot** - Интеграция с Telegram ботом
- **Telegram** - Прямая интеграция с Telegram Bot API
- **Email** - SMTP уведомления
- **Slack** - Webhook интеграция
- **Webhook** - HTTP webhook уведомления

## Мониторинг и аналитика

- **Дашборды** - Обзор состояния всех сервисов
- **Метрики** - Uptime, время отклика, статистика
- **Алерты** - Уведомления о проблемах
- **Отчеты** - Аналитика и тренды

## Безопасность

- **JWT аутентификация** - Безопасный доступ к API
- **Валидация данных** - Проверка всех входных данных
- **Логирование** - Аудит всех операций
- **CORS настройки** - Защита от межсайтовых запросов

### Локальная разработка
```bash
# Python Bot
cd bot && python monitoring_bot.py

# Java Backend
cd backend && ./gradlew bootRun
```
