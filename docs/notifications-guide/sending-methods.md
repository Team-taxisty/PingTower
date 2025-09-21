# Способы отправки уведомлений

## Обзор

PingTower поддерживает несколько способов отправки уведомлений:

1. **Прямое обращение к Python боту**
2. **Через Java бэкенд**
3. **Через REST API**
4. **Через WebSocket (планируется)**

## Способ 1: Прямое обращение к Python боту

### Endpoint
```
POST http://localhost:5000/send_notification
```

### Тело запроса
```json
{
    "username": "testuser",
    "service_name": "My Web Server",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Сервер не отвечает"
}
```

### Параметры

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `username` | string | Да | Имя пользователя в системе |
| `service_name` | string | Да | Название сервиса |
| `service_url` | string | Да | URL сервиса |
| `status` | string | Да | Статус сервиса (up/down/degraded) |
| `message` | string | Да | Сообщение об ошибке |

### Пример с curl
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

### Пример с Python
```python
import requests
import json

def send_notification_via_python_bot(username, service_name, service_url, status, message):
    url = "http://localhost:5000/send_notification"
    
    payload = {
        "username": username,
        "service_name": service_name,
        "service_url": service_url,
        "status": status,
        "message": message
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Ошибка отправки уведомления: {e}")
        return None

# Использование
result = send_notification_via_python_bot(
    username="testuser",
    service_name="My Web Server",
    service_url="https://example.com",
    status="down",
    message="Сервер не отвечает"
)
```

### Пример с JavaScript
```javascript
async function sendNotificationViaPythonBot(username, serviceName, serviceUrl, status, message) {
    const url = 'http://localhost:5000/send_notification';
    
    const payload = {
        username,
        service_name: serviceName,
        service_url: serviceUrl,
        status,
        message
    };
    
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Ошибка отправки уведомления:', error);
        return null;
    }
}

// Использование
sendNotificationViaPythonBot(
    'testuser',
    'My Web Server',
    'https://example.com',
    'down',
    'Сервер не отвечает'
);
```

## Способ 2: Через Java бэкенд

### Endpoint
```
POST http://localhost:8080/api/v1/notifications/send
```

### Тело запроса
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

### Параметры

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `username` | string | Да | Имя пользователя в системе |
| `serviceName` | string | Да | Название сервиса |
| `serviceUrl` | string | Да | URL сервиса |
| `status` | string | Да | Статус сервиса (UP/DOWN/DEGRADED) |
| `severity` | string | Нет | Уровень серьезности (INFO/WARNING/ERROR/CRITICAL) |
| `message` | string | Да | Сообщение об ошибке |

### Пример с curl
```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "testuser",
    "serviceName": "My Web Server",
    "serviceUrl": "https://example.com",
    "status": "down",
    "severity": "ERROR",
    "message": "Сервер не отвечает"
  }'
```

### Пример с Python
```python
import requests
import json

def send_notification_via_java_backend(username, service_name, service_url, status, severity, message, jwt_token):
    url = "http://localhost:8080/api/v1/notifications/send"
    
    payload = {
        "username": username,
        "serviceName": service_name,
        "serviceUrl": service_url,
        "status": status,
        "severity": severity,
        "message": message
    }
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {jwt_token}"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Ошибка отправки уведомления: {e}")
        return None

# Использование
result = send_notification_via_java_backend(
    username="testuser",
    service_name="My Web Server",
    service_url="https://example.com",
    status="DOWN",
    severity="ERROR",
    message="Сервер не отвечает",
    jwt_token="your_jwt_token_here"
)
```

### Пример с JavaScript
```javascript
async function sendNotificationViaJavaBackend(username, serviceName, serviceUrl, status, severity, message, jwtToken) {
    const url = 'http://localhost:8080/api/v1/notifications/send';
    
    const payload = {
        username,
        serviceName,
        serviceUrl,
        status,
        severity,
        message
    };
    
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwtToken}`
            },
            body: JSON.stringify(payload)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Ошибка отправки уведомления:', error);
        return null;
    }
}

// Использование
sendNotificationViaJavaBackend(
    'testuser',
    'My Web Server',
    'https://example.com',
    'DOWN',
    'ERROR',
    'Сервер не отвечает',
    'your_jwt_token_here'
);
```

## Способ 3: Через REST API с выбором канала

### Endpoint
```
POST http://localhost:8080/api/v1/notifications/send
```

### Тело запроса с указанием канала
```json
{
    "username": "testuser",
    "serviceName": "My Web Server",
    "serviceUrl": "https://example.com",
    "status": "down",
    "severity": "ERROR",
    "message": "Сервер не отвечает",
    "channelId": 1
}
```

### Дополнительные параметры

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `channelId` | integer | Нет | ID канала уведомлений |
| `channelType` | string | Нет | Тип канала (PYTHON_BOT/TELEGRAM/EMAIL/SLACK/WEBHOOK) |

### Пример с указанием канала
```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "testuser",
    "serviceName": "My Web Server",
    "serviceUrl": "https://example.com",
    "status": "down",
    "severity": "ERROR",
    "message": "Сервер не отвечает",
    "channelId": 1
  }'
```

## Способ 4: Batch отправка уведомлений

### Endpoint
```
POST http://localhost:8080/api/v1/notifications/send/batch
```

### Тело запроса
```json
{
    "notifications": [
        {
            "username": "user1",
            "serviceName": "Service 1",
            "serviceUrl": "https://service1.com",
            "status": "down",
            "severity": "ERROR",
            "message": "Service 1 is down"
        },
        {
            "username": "user2",
            "serviceName": "Service 2",
            "serviceUrl": "https://service2.com",
            "status": "degraded",
            "severity": "WARNING",
            "message": "Service 2 is slow"
        }
    ]
}
```

### Пример с Python
```python
def send_batch_notifications(notifications, jwt_token):
    url = "http://localhost:8080/api/v1/notifications/send/batch"
    
    payload = {
        "notifications": notifications
    }
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {jwt_token}"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Ошибка batch отправки: {e}")
        return None

# Использование
notifications = [
    {
        "username": "user1",
        "serviceName": "Service 1",
        "serviceUrl": "https://service1.com",
        "status": "DOWN",
        "severity": "ERROR",
        "message": "Service 1 is down"
    },
    {
        "username": "user2",
        "serviceName": "Service 2",
        "serviceUrl": "https://service2.com",
        "status": "DEGRADED",
        "severity": "WARNING",
        "message": "Service 2 is slow"
    }
]

result = send_batch_notifications(notifications, "your_jwt_token_here")
```

## Ответы API

### Успешный ответ
```json
{
    "success": true,
    "message": "Notification sent successfully",
    "deliveryId": "12345",
    "timestamp": "2024-01-15T10:30:00Z"
}
```

### Ответ с ошибкой
```json
{
    "success": false,
    "error": "User not found",
    "code": "USER_NOT_FOUND",
    "timestamp": "2024-01-15T10:30:00Z"
}
```

## Коды ошибок

| Код | Описание |
|-----|----------|
| `USER_NOT_FOUND` | Пользователь не найден |
| `CHANNEL_NOT_FOUND` | Канал уведомлений не найден |
| `CHANNEL_DISABLED` | Канал уведомлений отключен |
| `INVALID_STATUS` | Неверный статус сервиса |
| `INVALID_SEVERITY` | Неверный уровень серьезности |
| `BOT_UNAVAILABLE` | Python бот недоступен |
| `TELEGRAM_ERROR` | Ошибка отправки в Telegram |
| `EMAIL_ERROR` | Ошибка отправки email |
| `SLACK_ERROR` | Ошибка отправки в Slack |
| `WEBHOOK_ERROR` | Ошибка webhook |

## Рекомендации по использованию

### Выбор способа отправки

1. **Прямое обращение к Python боту** - для простых случаев и быстрого тестирования
2. **Через Java бэкенд** - для интеграции с системой мониторинга
3. **Batch отправка** - для множественных уведомлений

### Обработка ошибок

```python
def send_notification_with_retry(username, service_name, service_url, status, message, max_retries=3):
    for attempt in range(max_retries):
        try:
            result = send_notification_via_python_bot(
                username, service_name, service_url, status, message
            )
            if result and result.get('success'):
                return result
        except Exception as e:
            print(f"Попытка {attempt + 1} неудачна: {e}")
            if attempt < max_retries - 1:
                time.sleep(2 ** attempt)  # Экспоненциальная задержка
    
    return None
```

### Мониторинг доставки

```python
def check_delivery_status(delivery_id, jwt_token):
    url = f"http://localhost:8080/api/v1/notifications/deliveries/{delivery_id}"
    
    headers = {
        "Authorization": f"Bearer {jwt_token}"
    }
    
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Ошибка проверки статуса: {e}")
        return None
```
