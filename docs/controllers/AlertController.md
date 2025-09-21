# AlertController

REST API контроллер для управления алертами и каналами уведомлений в системе мониторинга PingTower.

## Обзор

`AlertController` предоставляет полный набор API эндпоинтов для:
- Управления алертами (просмотр, разрешение, удаление)
- Управления каналами уведомлений (создание, обновление, удаление, тестирование)
- Фильтрации и поиска алертов по различным критериям

## Базовый URL

```
/api/v1
```

## Эндпоинты для работы с алертами

### GET /alerts
Получить список алертов с возможностью фильтрации.

**Параметры запроса:**
- `page` (int, optional) - номер страницы (по умолчанию 0)
- `size` (int, optional) - размер страницы (по умолчанию 20)
- `serviceId` (Long, optional) - ID сервиса для фильтрации
- `resolved` (Boolean, optional) - статус разрешения алерта
- `severity` (String, optional) - уровень серьезности (INFO, WARNING, ERROR, CRITICAL)
- `since` (String, optional) - дата начала фильтрации в формате ISO 8601

**Пример запроса:**
```http
GET /api/v1/alerts?serviceId=1&resolved=false&severity=ERROR&since=2024-01-01T00:00:00
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 1,
      "serviceId": 1,
      "message": "Service is down",
      "severity": "ERROR",
      "resolved": false,
      "triggeredAt": "2024-01-15T10:30:00",
      "resolvedAt": null,
      "metadata": {
        "responseCode": 500,
        "errorMessage": "Connection timeout"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### GET /alerts/{id}
Получить конкретный алерт по ID.

**Параметры пути:**
- `id` (Long) - ID алерта

**Ответ:**
```json
{
  "id": 1,
  "serviceId": 1,
  "message": "Service is down",
  "severity": "ERROR",
  "resolved": false,
  "triggeredAt": "2024-01-15T10:30:00",
  "resolvedAt": null,
  "metadata": {
    "responseCode": 500,
    "errorMessage": "Connection timeout"
  }
}
```

### PUT /alerts/{id}/resolve
Отметить алерт как разрешенный.

**Параметры пути:**
- `id` (Long) - ID алерта

**Ответ:**
```json
{
  "id": 1,
  "serviceId": 1,
  "message": "Service is down",
  "severity": "ERROR",
  "resolved": true,
  "triggeredAt": "2024-01-15T10:30:00",
  "resolvedAt": "2024-01-15T11:00:00",
  "metadata": {
    "responseCode": 500,
    "errorMessage": "Connection timeout"
  }
}
```

### DELETE /alerts/{id}
Удалить алерт.

**Параметры пути:**
- `id` (Long) - ID алерта

**Ответ:** 204 No Content

## Эндпоинты для работы с каналами уведомлений

### GET /notifications/channels
Получить список всех каналов уведомлений.

**Ответ:**
```json
[
  {
    "id": 1,
    "name": "Email Notifications",
    "type": "EMAIL",
    "configuration": {
      "smtpHost": "smtp.gmail.com",
      "smtpPort": "587",
      "username": "alerts@company.com",
      "password": "encrypted_password"
    },
    "enabled": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

### POST /notifications/channels
Создать новый канал уведомлений.

**Тело запроса:**
```json
{
  "name": "Slack Alerts",
  "type": "SLACK",
  "configuration": {
    "webhookUrl": "https://hooks.slack.com/services/...",
    "channel": "#alerts"
  },
  "enabled": true
}
```

**Ответ:** 201 Created
```json
{
  "id": 2,
  "name": "Slack Alerts",
  "type": "SLACK",
  "configuration": {
    "webhookUrl": "https://hooks.slack.com/services/...",
    "channel": "#alerts"
  },
  "enabled": true,
  "createdAt": "2024-01-15T12:00:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

### PUT /notifications/channels/{id}
Обновить существующий канал уведомлений.

**Параметры пути:**
- `id` (Long) - ID канала

**Тело запроса:** (аналогично POST)

**Ответ:**
```json
{
  "id": 2,
  "name": "Slack Alerts Updated",
  "type": "SLACK",
  "configuration": {
    "webhookUrl": "https://hooks.slack.com/services/...",
    "channel": "#critical-alerts"
  },
  "enabled": true,
  "createdAt": "2024-01-15T12:00:00",
  "updatedAt": "2024-01-15T12:30:00"
}
```

### DELETE /notifications/channels/{id}
Удалить канал уведомлений.

**Параметры пути:**
- `id` (Long) - ID канала

**Ответ:** 204 No Content

### POST /notifications/channels/{id}/test
Отправить тестовое уведомление через канал.

**Параметры пути:**
- `id` (Long) - ID канала

**Ответ:**
```json
"Test notification sent successfully to Slack Alerts"
```

**В случае ошибки:**
```json
"Test notification failed: Connection timeout"
```

## Типы каналов уведомлений

Система поддерживает следующие типы каналов:

- **EMAIL** - Email уведомления
- **SLACK** - Slack webhook
- **TELEGRAM** - Telegram Bot API
- **WEBHOOK** - HTTP webhook
- **DISCORD** - Discord webhook

## Коды ошибок

- `400 Bad Request` - Некорректные данные запроса
- `404 Not Found` - Ресурс не найден
- `500 Internal Server Error` - Внутренняя ошибка сервера

## Примеры использования

### Получение неразрешенных алертов за последние 24 часа
```bash
curl -X GET "http://localhost:8080/api/v1/alerts?resolved=false&since=2024-01-14T12:00:00" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Создание канала Telegram уведомлений
```bash
curl -X POST "http://localhost:8080/api/v1/notifications/channels" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Telegram Alerts",
    "type": "TELEGRAM",
    "configuration": {
      "botToken": "123456789:ABCdefGHIjklMNOpqrsTUVwxyz",
      "chatId": "-1001234567890"
    },
    "enabled": true
  }'
```

### Тестирование канала уведомлений
```bash
curl -X POST "http://localhost:8080/api/v1/notifications/channels/1/test" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Зависимости

Контроллер использует следующие компоненты:
- `AlertRepository` - для работы с алертами
- `NotificationChannelRepository` - для работы с каналами
- `NotificationService` - для отправки уведомлений
- `Alert` и `NotificationChannel` модели данных

## Безопасность

Все эндпоинты требуют JWT аутентификации. Токен должен быть передан в заголовке `Authorization: Bearer <token>`.
