# 🔧 Настройка каналов уведомлений

## Обзор

PingTower поддерживает различные типы каналов уведомлений. Данный раздел описывает настройку каждого типа канала.

## Типы каналов

### 1. Python Bot (PYTHON_BOT)

Интеграция с Python Telegram ботом.

#### Создание канала

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

#### Конфигурация

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `botUrl` | string | Да | URL Python бота |

#### Пример конфигурации

```json
{
  "botUrl": "http://localhost:5000"
}
```

### 2. Telegram (TELEGRAM)

Прямая интеграция с Telegram Bot API.

#### Создание канала

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Telegram Channel",
    "type": "TELEGRAM",
    "configuration": {
      "botToken": "123456789:ABCdefGHIjklMNOpqrsTUVwxyz",
      "chatId": "-1001234567890"
    },
    "enabled": true
  }'
```

#### Конфигурация

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `botToken` | string | Да | Токен Telegram бота |
| `chatId` | string | Да | ID чата для отправки |

#### Получение Bot Token

1. Найдите [@BotFather](https://t.me/botfather) в Telegram
2. Отправьте команду `/newbot`
3. Следуйте инструкциям для создания бота
4. Скопируйте полученный токен

#### Получение Chat ID

1. Добавьте бота в группу или канал
2. Отправьте сообщение боту
3. Используйте API для получения chat ID:

```bash
curl "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates"
```

### 3. Email (EMAIL)

Отправка email уведомлений.

#### Создание канала

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Email Channel",
    "type": "EMAIL",
    "configuration": {
      "smtpHost": "smtp.gmail.com",
      "smtpPort": "587",
      "username": "your-email@gmail.com",
      "password": "your-app-password",
      "fromEmail": "alerts@yourcompany.com",
      "useTls": true
    },
    "enabled": true
  }'
```

#### Конфигурация

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `smtpHost` | string | Да | SMTP сервер |
| `smtpPort` | integer | Да | Порт SMTP |
| `username` | string | Да | Имя пользователя |
| `password` | string | Да | Пароль или App Password |
| `fromEmail` | string | Да | Email отправителя |
| `useTls` | boolean | Нет | Использовать TLS |

#### Настройка Gmail

1. Включите 2FA в Google аккаунте
2. Создайте App Password:
   - Перейдите в настройки Google аккаунта
   - Безопасность → Пароли приложений
   - Создайте новый пароль для приложения
3. Используйте App Password вместо обычного пароля

#### Настройка других SMTP серверов

**Outlook/Hotmail:**
```json
{
  "smtpHost": "smtp-mail.outlook.com",
  "smtpPort": 587,
  "useTls": true
}
```

**Yahoo:**
```json
{
  "smtpHost": "smtp.mail.yahoo.com",
  "smtpPort": 587,
  "useTls": true
}
```

**Custom SMTP:**
```json
{
  "smtpHost": "mail.yourcompany.com",
  "smtpPort": 25,
  "useTls": false
}
```

### 4. Slack (SLACK)

Интеграция со Slack через webhook.

#### Создание канала

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Slack Channel",
    "type": "SLACK",
    "configuration": {
      "webhookUrl": "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX",
      "channel": "#alerts",
      "username": "PingTower Bot"
    },
    "enabled": true
  }'
```

#### Конфигурация

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `webhookUrl` | string | Да | Slack webhook URL |
| `channel` | string | Нет | Канал для отправки |
| `username` | string | Нет | Имя бота |

#### Создание Slack Webhook

1. Перейдите в [Slack API](https://api.slack.com/apps)
2. Создайте новое приложение
3. В разделе "Incoming Webhooks" включите webhooks
4. Добавьте новый webhook в нужный канал
5. Скопируйте webhook URL

### 5. Webhook (WEBHOOK)

HTTP webhook уведомления.

#### Создание канала

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Custom Webhook",
    "type": "WEBHOOK",
    "configuration": {
      "url": "https://your-service.com/webhook",
      "method": "POST",
      "headers": {
        "Authorization": "Bearer your-token",
        "Content-Type": "application/json"
      },
      "timeout": 30
    },
    "enabled": true
  }'
```

#### Конфигурация

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `url` | string | Да | URL webhook |
| `method` | string | Нет | HTTP метод (POST/PUT) |
| `headers` | object | Нет | HTTP заголовки |
| `timeout` | integer | Нет | Таймаут в секундах |

#### Формат webhook payload

```json
{
  "username": "testuser",
  "serviceName": "My Service",
  "serviceUrl": "https://example.com",
  "status": "DOWN",
  "severity": "ERROR",
  "message": "Service is down",
  "timestamp": "2024-01-15T10:30:00Z",
  "metadata": {
    "responseCode": 500,
    "responseTime": 5000
  }
}
```

## Управление каналами

### Получение списка каналов

```bash
curl http://localhost:8080/api/v1/notifications/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Получение канала по ID

```bash
curl http://localhost:8080/api/v1/notifications/channels/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Обновление канала

```bash
curl -X PUT http://localhost:8080/api/v1/notifications/channels/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Updated Channel Name",
    "enabled": false
  }'
```

### Удаление канала

```bash
curl -X DELETE http://localhost:8080/api/v1/notifications/channels/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Тестирование каналов

### Тест канала

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels/1/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Тест с кастомным сообщением

```bash
curl -X POST http://localhost:8080/api/v1/notifications/channels/1/test \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "Custom test message",
    "username": "testuser"
  }'
```

## Настройка по умолчанию

### Установка канала по умолчанию

```bash
curl -X PUT http://localhost:8080/api/v1/notifications/channels/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "isDefault": true
  }'
```

### Получение канала по умолчанию

```bash
curl http://localhost:8080/api/v1/notifications/channels/default \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Примеры конфигураций

### Полная конфигурация для всех типов

```json
{
  "pythonBot": {
    "name": "Python Bot Channel",
    "type": "PYTHON_BOT",
    "configuration": {
      "botUrl": "http://localhost:5000"
    },
    "enabled": true
  },
  "telegram": {
    "name": "Telegram Channel",
    "type": "TELEGRAM",
    "configuration": {
      "botToken": "123456789:ABCdefGHIjklMNOpqrsTUVwxyz",
      "chatId": "-1001234567890"
    },
    "enabled": true
  },
  "email": {
    "name": "Email Channel",
    "type": "EMAIL",
    "configuration": {
      "smtpHost": "smtp.gmail.com",
      "smtpPort": 587,
      "username": "alerts@yourcompany.com",
      "password": "your-app-password",
      "fromEmail": "alerts@yourcompany.com",
      "useTls": true
    },
    "enabled": true
  },
  "slack": {
    "name": "Slack Channel",
    "type": "SLACK",
    "configuration": {
      "webhookUrl": "https://hooks.slack.com/services/...",
      "channel": "#alerts",
      "username": "PingTower Bot"
    },
    "enabled": true
  },
  "webhook": {
    "name": "Custom Webhook",
    "type": "WEBHOOK",
    "configuration": {
      "url": "https://your-service.com/webhook",
      "method": "POST",
      "headers": {
        "Authorization": "Bearer your-token"
      },
      "timeout": 30
    },
    "enabled": true
  }
}
```

## Автоматизация настройки

### Скрипт для создания каналов

```python
#!/usr/bin/env python3
"""
Скрипт для автоматического создания каналов уведомлений
"""

import requests
import json
import sys

# Конфигурация
BASE_URL = "http://localhost:8080"
JWT_TOKEN = "your_jwt_token_here"

def create_channel(channel_config):
    """Создание канала уведомлений"""
    url = f"{BASE_URL}/api/v1/notifications/channels"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {JWT_TOKEN}"
    }
    
    try:
        response = requests.post(url, json=channel_config, headers=headers)
        response.raise_for_status()
        
        result = response.json()
        print(f"✅ Канал '{channel_config['name']}' создан с ID: {result['id']}")
        return result['id']
    except requests.exceptions.RequestException as e:
        print(f"❌ Ошибка создания канала '{channel_config['name']}': {e}")
        return None

def test_channel(channel_id):
    """Тестирование канала"""
    url = f"{BASE_URL}/api/v1/notifications/channels/{channel_id}/test"
    headers = {
        "Authorization": f"Bearer {JWT_TOKEN}"
    }
    
    try:
        response = requests.post(url, headers=headers)
        response.raise_for_status()
        
        result = response.json()
        print(f"✅ Тест канала {channel_id} успешен: {result}")
        return True
    except requests.exceptions.RequestException as e:
        print(f"❌ Ошибка тестирования канала {channel_id}: {e}")
        return False

def main():
    """Основная функция"""
    print("🚀 Создание каналов уведомлений")
    
    # Конфигурации каналов
    channels = [
        {
            "name": "Python Bot Channel",
            "type": "PYTHON_BOT",
            "configuration": {
                "botUrl": "http://localhost:5000"
            },
            "enabled": True
        },
        {
            "name": "Email Channel",
            "type": "EMAIL",
            "configuration": {
                "smtpHost": "smtp.gmail.com",
                "smtpPort": 587,
                "username": "alerts@yourcompany.com",
                "password": "your-app-password",
                "fromEmail": "alerts@yourcompany.com",
                "useTls": True
            },
            "enabled": True
        }
    ]
    
    created_channels = []
    
    # Создание каналов
    for channel in channels:
        channel_id = create_channel(channel)
        if channel_id:
            created_channels.append(channel_id)
    
    print(f"\n📊 Создано каналов: {len(created_channels)}")
    
    # Тестирование каналов
    print("\n🧪 Тестирование каналов...")
    for channel_id in created_channels:
        test_channel(channel_id)
    
    print("\n🎉 Настройка каналов завершена!")

if __name__ == "__main__":
    main()
```

## Рекомендации

### Безопасность

1. **Не храните пароли в открытом виде** - используйте переменные окружения
2. **Используйте App Passwords** для email сервисов
3. **Ограничьте доступ** к webhook URL
4. **Регулярно обновляйте токены**

### Производительность

1. **Используйте connection pooling** для SMTP
2. **Настройте таймауты** для webhook
3. **Мониторьте доставку** уведомлений
4. **Используйте batch отправку** для множественных уведомлений

### Мониторинг

1. **Логируйте все операции** с каналами
2. **Мониторьте успешность доставки**
3. **Настройте алерты** на ошибки каналов
4. **Регулярно тестируйте** каналы
