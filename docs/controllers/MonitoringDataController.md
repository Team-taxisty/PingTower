# MonitoringDataController

REST API контроллер для получения данных мониторинга, аналитики и метрик в системе PingTower.

## Обзор

`MonitoringDataController` предоставляет API эндпоинты для:
- Получения результатов проверок сервисов
- Анализа метрик производительности
- Мониторинга общего состояния системы
- Получения данных для дашборда

## Базовый URL

```
/api/v1/monitoring
```

## Эндпоинты для результатов проверок

### GET /results
Получить результаты проверок с возможностью фильтрации.

**Параметры запроса:**
- `page` (int, optional) - номер страницы (по умолчанию 0)
- `size` (int, optional) - размер страницы (по умолчанию 20)
- `serviceId` (Long, optional) - ID сервиса для фильтрации
- `successful` (Boolean, optional) - фильтр по успешности проверки
- `since` (String, optional) - дата начала фильтрации в формате ISO 8601

**Пример запроса:**
```http
GET /api/v1/monitoring/results?serviceId=1&successful=false&since=2024-01-15T00:00:00
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 1,
      "serviceId": 1,
      "serviceName": "API Gateway",
      "checkTime": "2024-01-15T10:30:00",
      "successful": false,
      "responseCode": 500,
      "responseTimeMs": 5000,
      "errorMessage": "Connection timeout"
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

### GET /services/{serviceId}/results
Получить результаты проверок для конкретного сервиса.

**Параметры пути:**
- `serviceId` (Long) - ID сервиса

**Параметры запроса:**
- `page` (int, optional) - номер страницы
- `size` (int, optional) - размер страницы
- `since` (String, optional) - дата начала (по умолчанию 7 дней назад)
- `until` (String, optional) - дата окончания (по умолчанию сейчас)

**Пример запроса:**
```http
GET /api/v1/monitoring/services/1/results?since=2024-01-01T00:00:00&until=2024-01-15T23:59:59
```

**Ответ:** (аналогично GET /results)

## Эндпоинты для метрик

### GET /services/{serviceId}/metrics
Получить метрики производительности для сервиса.

**Параметры пути:**
- `serviceId` (Long) - ID сервиса

**Параметры запроса:**
- `since` (String, optional) - дата начала анализа (по умолчанию 30 дней назад)

**Пример запроса:**
```http
GET /api/v1/monitoring/services/1/metrics?since=2024-01-01T00:00:00
```

**Ответ:**
```json
{
  "serviceId": 1,
  "serviceName": "API Gateway",
  "serviceUrl": "https://api.example.com",
  "totalChecks": 1440,
  "successfulChecks": 1380,
  "failedChecks": 60,
  "averageResponseTimeMs": 245.5,
  "minResponseTimeMs": 120,
  "maxResponseTimeMs": 1200,
  "uptimePercentage": 95.83,
  "periodStart": "2024-01-01T00:00:00",
  "periodEnd": "2024-01-15T23:59:59"
}
```

## Эндпоинты для системного мониторинга

### GET /health
Получить общее состояние системы мониторинга.

**Ответ:**
```json
{
  "totalServices": 10,
  "activeServices": 8,
  "recentChecks": 240,
  "recentFailures": 12,
  "successRate": 95.0,
  "timestamp": "2024-01-15T12:00:00"
}
```

### GET /dashboard
Получить данные для дашборда - статус всех активных сервисов.

**Ответ:**
```json
[
  {
    "serviceId": 1,
    "serviceName": "API Gateway",
    "serviceUrl": "https://api.example.com",
    "status": "UP",
    "responseCode": 200,
    "responseTimeMs": 245,
    "lastChecked": "2024-01-15T12:00:00",
    "enabled": true
  },
  {
    "serviceId": 2,
    "serviceName": "Database",
    "serviceUrl": "postgresql://db.example.com:5432/app",
    "status": "DOWN",
    "responseCode": null,
    "responseTimeMs": null,
    "lastChecked": "2024-01-15T11:58:00",
    "enabled": true
  }
]
```

## Статусы сервисов

- **UP** - сервис работает нормально
- **DOWN** - сервис недоступен
- **DEGRADED** - сервис работает с проблемами
- **UNKNOWN** - статус неизвестен (нет данных)
- **ERROR** - ошибка при проверке

## Коды ответов HTTP

- `200 OK` - Успешный запрос
- `404 Not Found` - Сервис не найден
- `500 Internal Server Error` - Внутренняя ошибка сервера

## Примеры использования

### Получение последних неудачных проверок
```bash
curl -X GET "http://localhost:8080/api/v1/monitoring/results?successful=false&since=2024-01-15T00:00:00" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Получение метрик сервиса за последний месяц
```bash
curl -X GET "http://localhost:8080/api/v1/monitoring/services/1/metrics?since=2023-12-15T00:00:00" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Проверка общего состояния системы
```bash
curl -X GET "http://localhost:8080/api/v1/monitoring/health" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Получение данных для дашборда
```bash
curl -X GET "http://localhost:8080/api/v1/monitoring/dashboard" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Фильтрация по времени

Все эндпоинты поддерживают фильтрацию по времени с помощью параметра `since`. Формат даты: ISO 8601 (`YYYY-MM-DDTHH:mm:ss`).

**Примеры:**
- `since=2024-01-15T00:00:00` - с начала дня
- `since=2024-01-01T00:00:00` - с начала месяца
- `since=2023-12-15T00:00:00` - за последний месяц

## Пагинация

Эндпоинты с большим объемом данных поддерживают пагинацию:

**Параметры:**
- `page` - номер страницы (начиная с 0)
- `size` - количество элементов на странице (по умолчанию 20, максимум 100)

**Пример:**
```http
GET /api/v1/monitoring/results?page=0&size=50
```

## Метрики и аналитика

### Uptime Percentage
Процент времени доступности сервиса:
```
uptime = (successful_checks / total_checks) * 100
```

### Response Time Metrics
- **averageResponseTimeMs** - среднее время ответа
- **minResponseTimeMs** - минимальное время ответа
- **maxResponseTimeMs** - максимальное время ответа

### Success Rate
Процент успешных проверок за период:
```
success_rate = (successful_checks / total_checks) * 100
```

## Зависимости

Контроллер использует следующие компоненты:
- `MonitoringService` - для расчета метрик
- `CheckResultRepository` - для получения результатов проверок
- `MonitoredServiceRepository` - для работы с сервисами
- `CheckResult` и `MonitoredService` модели данных

## Производительность

- Результаты проверок кэшируются для быстрого доступа
- Метрики рассчитываются асинхронно
- Поддерживается пагинация для больших объемов данных
- Временные фильтры оптимизированы с помощью индексов БД

## Безопасность

Все эндпоинты требуют JWT аутентификации. Токен должен быть передан в заголовке `Authorization: Bearer <token>`.

## Мониторинг и логирование

- Все запросы логируются
- Медленные запросы (>1 сек) помечаются в логах
- Ошибки записываются в систему мониторинга
- Метрики производительности API доступны через `/actuator/metrics`
