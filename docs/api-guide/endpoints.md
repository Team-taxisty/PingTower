# Основные эндпоинты для дашбордов

## Обзор

Основные эндпоинты для получения данных мониторинга, метрик и аналитики для построения дашбордов.

## 1. Общий дашборд системы

**GET** `/api/v1/monitoring/dashboard`

Возвращает статус всех активных сервисов для главного дашборда.

**Параметры:** Нет

**Ответ:**
```json
[
  {
    "serviceId": 1,
    "serviceName": "My API",
    "serviceUrl": "https://api.example.com/health",
    "status": "UP",
    "responseCode": 200,
    "responseTimeMs": 150,
    "lastChecked": "2024-01-15T10:30:00",
    "enabled": true
  },
  {
    "serviceId": 2,
    "serviceName": "Database Service",
    "serviceUrl": "https://db.example.com/ping",
    "status": "DOWN",
    "responseCode": 500,
    "responseTimeMs": 5000,
    "lastChecked": "2024-01-15T10:29:45",
    "enabled": true
  }
]
```

**Статусы сервисов:**
- `UP` - Сервис работает нормально
- `DOWN` - Сервис недоступен
- `DEGRADED` - Сервис работает с проблемами
- `UNKNOWN` - Статус неизвестен (нет данных)
- `ERROR` - Ошибка при проверке

## 2. Здоровье системы

**GET** `/api/v1/monitoring/health`

Возвращает общую статистику системы мониторинга.

**Параметры:** Нет

**Ответ:**
```json
{
  "totalServices": 15,
  "activeServices": 12,
  "recentChecks": 1440,
  "recentFailures": 23,
  "successRate": 98.4,
  "timestamp": "2024-01-15T10:30:00"
}
```

## 3. Результаты проверок

**GET** `/api/v1/monitoring/results`

Возвращает последние результаты проверок с фильтрацией.

**Параметры:**
- `page` (int, optional) - Номер страницы (по умолчанию 0)
- `size` (int, optional) - Размер страницы (по умолчанию 20)
- `serviceId` (long, optional) - ID конкретного сервиса
- `successful` (boolean, optional) - Фильтр по успешности
- `since` (string, optional) - Дата начала в ISO формате

**Пример запроса:**
```http
GET /api/v1/monitoring/results?page=0&size=50&successful=false&since=2024-01-15T00:00:00
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 12345,
      "serviceId": 1,
      "serviceName": "My API",
      "checkTime": "2024-01-15T10:30:00",
      "successful": false,
      "responseCode": 500,
      "responseTimeMs": 5000,
      "errorMessage": "Connection timeout"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalElements": 1,
  "totalPages": 1
}
```

## 4. Результаты конкретного сервиса

**GET** `/api/v1/monitoring/services/{serviceId}/results`

Возвращает результаты проверок для конкретного сервиса.

**Параметры:**
- `serviceId` (path) - ID сервиса
- `page` (int, optional) - Номер страницы
- `size` (int, optional) - Размер страницы
- `since` (string, optional) - Дата начала
- `until` (string, optional) - Дата окончания

**Пример запроса:**
```http
GET /api/v1/monitoring/services/1/results?since=2024-01-15T00:00:00&until=2024-01-15T23:59:59&size=100
```

**Ответ:** Аналогичен предыдущему эндпоинту

## 5. Метрики сервиса

**GET** `/api/v1/monitoring/services/{serviceId}/metrics`

Возвращает агрегированные метрики сервиса за период.

**Параметры:**
- `serviceId` (path) - ID сервиса
- `since` (string, optional) - Дата начала (по умолчанию 30 дней назад)

**Пример запроса:**
```http
GET /api/v1/monitoring/services/1/metrics?since=2024-01-01T00:00:00
```

**Ответ:**
```json
{
  "serviceId": 1,
  "serviceName": "My API",
  "serviceUrl": "https://api.example.com/health",
  "totalChecks": 1440,
  "successfulChecks": 1417,
  "failedChecks": 23,
  "averageResponseTimeMs": 245.5,
  "minResponseTimeMs": 89,
  "maxResponseTimeMs": 2100,
  "uptimePercentage": 98.4,
  "periodStart": "2024-01-01T00:00:00",
  "periodEnd": "2024-01-15T10:30:00"
}
```

## 6. Список сервисов

**GET** `/api/v1/services`

Возвращает список всех сервисов пользователя.

**Параметры:**
- `page` (int, optional) - Номер страницы
- `size` (int, optional) - Размер страницы
- `enabled` (boolean, optional) - Фильтр по активности
- `search` (string, optional) - Поиск по имени

**Пример запроса:**
```http
GET /api/v1/services?page=0&size=20&enabled=true&search=api
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "My API",
      "url": "https://api.example.com/health",
      "enabled": true,
      "checkInterval": 60,
      "timeout": 30,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-15T10:30:00"
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

## 7. Алерты

**GET** `/api/v1/alerts`

Возвращает список алертов с фильтрацией.

**Параметры:**
- `page` (int, optional) - Номер страницы
- `size` (int, optional) - Размер страницы
- `serviceId` (long, optional) - ID сервиса
- `resolved` (boolean, optional) - Статус разрешения
- `severity` (string, optional) - Уровень серьезности
- `since` (string, optional) - Дата начала

**Пример запроса:**
```http
GET /api/v1/alerts?serviceId=1&resolved=false&severity=ERROR&since=2024-01-15T00:00:00
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

## 8. История запусков проверок

**GET** `/runs`

Возвращает историю запусков проверок.

**Параметры:**
- `check_id` (UUID, optional) - ID проверки
- `from` (DateTime, optional) - Начальная дата
- `to` (DateTime, optional) - Конечная дата
- `limit` (Integer, optional) - Максимальное количество записей

**Пример запроса:**
```http
GET /runs?check_id=550e8400-e29b-41d4-a716-446655440000&from=2024-01-15T00:00:00Z&limit=100
```

**Ответ:**
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "check_id": "550e8400-e29b-41d4-a716-446655440000",
      "started_at": "2024-01-15T10:30:00Z",
      "finished_at": "2024-01-15T10:30:01Z",
      "latency_ms": 1200,
      "status": "UP"
    }
  ]
}
```

## Примеры использования

### Получение данных для главного дашборда

```javascript
const fetchDashboardData = async () => {
  try {
    const response = await apiRequest('/api/v1/monitoring/dashboard');
    if (response.ok) {
      const services = await response.json();
      return services;
    }
  } catch (error) {
    console.error('Ошибка получения данных дашборда:', error);
  }
};
```

### Получение метрик сервиса

```javascript
const fetchServiceMetrics = async (serviceId, since) => {
  try {
    const url = `/api/v1/monitoring/services/${serviceId}/metrics?since=${since}`;
    const response = await apiRequest(url);
    if (response.ok) {
      return await response.json();
    }
  } catch (error) {
    console.error('Ошибка получения метрик:', error);
  }
};
```

### Получение истории проверок

```javascript
const fetchCheckHistory = async (serviceId, since, until) => {
  try {
    const url = `/api/v1/monitoring/services/${serviceId}/results?since=${since}&until=${until}`;
    const response = await apiRequest(url);
    if (response.ok) {
      return await response.json();
    }
  } catch (error) {
    console.error('Ошибка получения истории:', error);
  }
};
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
