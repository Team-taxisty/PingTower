# API Controllers Documentation

Документация для REST API контроллеров системы мониторинга PingTower.

## Обзор

Данная директория содержит подробную документацию для всех основных API контроллеров системы:

## Контроллеры

### 1. [AlertController](./AlertController.md)
**Управление алертами и уведомлениями**
- Управление алертами (просмотр, разрешение, удаление)
- Управление каналами уведомлений (создание, обновление, удаление, тестирование)
- Фильтрация алертов по различным критериям
- Поддержка множественных типов уведомлений (Email, Slack, Telegram, Webhook, Discord)

**Базовый URL:** `/api/v1`

### 2. [MonitoringDataController](./MonitoringDataController.md)
**Данные мониторинга и аналитика**
- Получение результатов проверок сервисов
- Метрики производительности и uptime
- Общее состояние системы
- Данные для дашборда
- Аналитика и отчетность

**Базовый URL:** `/api/v1/monitoring`

### 3. [RunsController](./RunsController.md)
**История запусков проверок**
- Получение истории выполнения проверок
- Фильтрация по времени и конкретным проверкам
- Анализ производительности и статуса
- Данные для графиков и отчетов

**Базовый URL:** `/runs`

## Общие принципы

### Аутентификация
Все API эндпоинты требуют JWT аутентификации. Токен должен быть передан в заголовке:
```
Authorization: Bearer <your_jwt_token>
```

### Формат данных
- **Входные данные**: JSON
- **Выходные данные**: JSON
- **Даты**: ISO 8601 формат (`YYYY-MM-DDTHH:mm:ss` или `YYYY-MM-DDTHH:mm:ssZ`)
- **UUID**: Стандартный формат UUID v4

### Коды ответов HTTP
- `200 OK` - Успешный запрос
- `201 Created` - Ресурс создан
- `204 No Content` - Успешное удаление
- `400 Bad Request` - Некорректные данные запроса
- `401 Unauthorized` - Отсутствует или недействительный токен
- `404 Not Found` - Ресурс не найден
- `500 Internal Server Error` - Внутренняя ошибка сервера

### Пагинация
Эндпоинты с большим объемом данных поддерживают пагинацию:
- `page` - номер страницы (начиная с 0)
- `size` - размер страницы (по умолчанию 20, максимум 100)

### Фильтрация
Многие эндпоинты поддерживают фильтрацию:
- По времени (`since`, `until`, `from`, `to`)
- По статусу (`resolved`, `successful`)
- По сервису (`serviceId`)
- По типу (`severity`, `status`)

## Примеры использования

### Базовый запрос с аутентификацией
```bash
curl -X GET "http://localhost:8080/api/v1/alerts" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Запрос с фильтрацией
```bash
curl -X GET "http://localhost:8080/api/v1/monitoring/results?serviceId=1&successful=false&since=2024-01-15T00:00:00" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### POST запрос с данными
```bash
curl -X POST "http://localhost:8080/api/v1/notifications/channels" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Slack Alerts",
    "type": "SLACK",
    "configuration": {
      "webhookUrl": "https://hooks.slack.com/services/...",
      "channel": "#alerts"
    },
    "enabled": true
  }'
```

## Интеграция с фронтендом

Все контроллеры активно используются в React фронтенде:

- **AlertController** → `Alerts.js` - управление уведомлениями
- **MonitoringDataController** → `CheckDetail.js` - детальная информация о сервисах
- **RunsController** → `CheckDetail.js`, `Reports.js` - история проверок и отчеты

## Мониторинг и производительность

- Все запросы логируются
- Медленные запросы помечаются в логах
- Метрики доступны через `/actuator/metrics`
- Поддержка кэширования для часто запрашиваемых данных

## Безопасность

- JWT токены с ограниченным временем жизни
- Валидация всех входных данных
- Защита от SQL инъекций через JPA
- CORS настройки для фронтенда

## Разработка и тестирование

### Локальная разработка
```bash
# Запуск приложения
./gradlew bootRun

# Запуск тестов
./gradlew test

# Проверка API документации
open http://localhost:8080/swagger-ui.html
```

### Тестирование API
```bash
# Получение JWT токена
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Использование токена в запросах
export JWT_TOKEN="your_token_here"
curl -X GET "http://localhost:8080/api/v1/alerts" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Дополнительные ресурсы

- [Общий API Guide](../API_GUIDE.md) - общая документация API
- [Notifications Guide](../NOTIFICATIONS_GUIDE.md) - руководство по уведомлениям
- [OpenAPI Specification](../../backend/src/main/java/taxisty/pingtower/backend/api/openapi.yml) - Swagger документация
