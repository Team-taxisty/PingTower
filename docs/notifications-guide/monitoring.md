# Мониторинг системы уведомлений

## Обзор

Мониторинг системы уведомлений PingTower включает проверку здоровья компонентов, отслеживание доставки уведомлений и анализ производительности.

## Проверка здоровья компонентов

### Python Bot

#### Проверка статуса

```bash
curl http://localhost:5000/health
```

**Ожидаемый ответ:**
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00",
  "version": "1.0.0",
  "uptime": "2h 30m 15s"
}
```

#### Детальная информация

```bash
curl http://localhost:5000/health/detailed
```

**Ответ:**
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00",
  "version": "1.0.0",
  "uptime": "2h 30m 15s",
  "database": {
    "status": "connected",
    "size": "2.5MB"
  },
  "telegram": {
    "status": "connected",
    "bot_username": "PingTower_tax_bot"
  },
  "metrics": {
    "total_notifications": 1250,
    "successful_deliveries": 1180,
    "failed_deliveries": 70,
    "success_rate": 94.4
  }
}
```

### Java Backend

#### Проверка статуса

```bash
curl http://localhost:8080/actuator/health
```

**Ожидаемый ответ:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 100000000000,
        "free": 50000000000,
        "threshold": 10485760
      }
    }
  }
}
```

#### Детальная информация

```bash
curl http://localhost:8080/actuator/health/detailed
```

## Мониторинг каналов уведомлений

### Список каналов

```bash
curl http://localhost:8080/api/v1/notifications/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Ответ:**
```json
[
  {
    "id": 1,
    "name": "Python Bot Channel",
    "type": "PYTHON_BOT",
    "enabled": true,
    "lastUsed": "2024-01-15T10:25:00",
    "successRate": 94.4,
    "totalDeliveries": 1250,
    "failedDeliveries": 70
  }
]
```

### Статус канала

```bash
curl http://localhost:8080/api/v1/notifications/channels/1/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Ответ:**
```json
{
  "id": 1,
  "name": "Python Bot Channel",
  "type": "PYTHON_BOT",
  "status": "HEALTHY",
  "lastCheck": "2024-01-15T10:30:00",
  "responseTime": 150,
  "errorRate": 5.6,
  "metrics": {
    "last24h": {
      "total": 120,
      "successful": 115,
      "failed": 5
    },
    "last7d": {
      "total": 840,
      "successful": 790,
      "failed": 50
    }
  }
}
```

## Мониторинг доставки уведомлений

### История доставки

```bash
curl http://localhost:8080/api/v1/notifications/deliveries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 1,
      "channelId": 1,
      "channelName": "Python Bot Channel",
      "username": "testuser",
      "serviceName": "My Service",
      "status": "SENT",
      "sentAt": "2024-01-15T10:30:00",
      "responseTime": 150,
      "errorMessage": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1250,
  "totalPages": 63
}
```

### Статистика доставки

```bash
curl http://localhost:8080/api/v1/notifications/deliveries/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Ответ:**
```json
{
  "totalDeliveries": 1250,
  "successfulDeliveries": 1180,
  "failedDeliveries": 70,
  "successRate": 94.4,
  "averageResponseTime": 245,
  "last24h": {
    "total": 120,
    "successful": 115,
    "failed": 5,
    "successRate": 95.8
  },
  "last7d": {
    "total": 840,
    "successful": 790,
    "failed": 50,
    "successRate": 94.0
  },
  "byChannel": [
    {
      "channelId": 1,
      "channelName": "Python Bot Channel",
      "total": 1000,
      "successful": 950,
      "failed": 50,
      "successRate": 95.0
    }
  ]
}
```

### Фильтрация по времени

```bash
curl "http://localhost:8080/api/v1/notifications/deliveries?since=2024-01-15T00:00:00&until=2024-01-15T23:59:59" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Фильтрация по статусу

```bash
curl "http://localhost:8080/api/v1/notifications/deliveries?status=FAILED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Метрики производительности

### Prometheus метрики

```bash
curl http://localhost:8080/actuator/prometheus
```

**Пример метрик:**
```
# HELP notifications_sent_total Total number of notifications sent
# TYPE notifications_sent_total counter
notifications_sent_total{channel="python_bot",status="success"} 1180
notifications_sent_total{channel="python_bot",status="failed"} 70

# HELP notification_delivery_duration_seconds Time taken to deliver notification
# TYPE notification_delivery_duration_seconds histogram
notification_delivery_duration_seconds_bucket{channel="python_bot",le="0.1"} 500
notification_delivery_duration_seconds_bucket{channel="python_bot",le="0.5"} 1000
notification_delivery_duration_seconds_bucket{channel="python_bot",le="1.0"} 1150
notification_delivery_duration_seconds_bucket{channel="python_bot",le="+Inf"} 1250
```

### Grafana дашборд

Создайте дашборд с следующими панелями:

1. **Общая статистика доставки**
   - Успешные доставки
   - Неудачные доставки
   - Процент успешности

2. **Время отклика каналов**
   - Среднее время отклика
   - 95-й перцентиль
   - Максимальное время

3. **Ошибки по типам**
   - Ошибки сети
   - Ошибки аутентификации
   - Ошибки валидации

4. **Нагрузка по времени**
   - Количество уведомлений в час
   - Пиковые нагрузки
   - Тренды

## Алерты и уведомления

### Настройка алертов

#### Алерт на высокий процент ошибок

```yaml
# alertmanager.yml
groups:
- name: notification-alerts
  rules:
  - alert: HighNotificationFailureRate
    expr: (notifications_sent_total{status="failed"} / notifications_sent_total) * 100 > 10
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High notification failure rate"
      description: "Notification failure rate is {{ $value }}% for channel {{ $labels.channel }}"
```

#### Алерт на недоступность канала

```yaml
- alert: NotificationChannelDown
  expr: up{job="notification-channel"} == 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "Notification channel is down"
    description: "Channel {{ $labels.channel }} is not responding"
```

### Webhook для алертов

```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "admin",
    "serviceName": "Notification System",
    "serviceUrl": "http://localhost:8080",
    "status": "DOWN",
    "severity": "CRITICAL",
    "message": "High notification failure rate detected"
  }'
```

## Логирование

### Настройка логирования

#### Python Bot

```python
# logging_config.py
import logging
import logging.handlers

def setup_logging():
    # Настройка root logger
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.handlers.RotatingFileHandler(
                'bot.log',
                maxBytes=10*1024*1024,  # 10MB
                backupCount=5
            ),
            logging.StreamHandler()
        ]
    )
    
    # Настройка логгера для уведомлений
    notification_logger = logging.getLogger('notifications')
    notification_logger.setLevel(logging.INFO)
    
    handler = logging.handlers.RotatingFileHandler(
        'notifications.log',
        maxBytes=10*1024*1024,
        backupCount=5
    )
    formatter = logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s'
    )
    handler.setFormatter(formatter)
    notification_logger.addHandler(handler)
```

#### Java Backend

```yaml
# application.yaml
logging:
  level:
    taxisty.pingtower.backend.notifications: INFO
    org.springframework.web: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/notifications.log
    max-size: 10MB
    max-history: 30
```

### Структурированное логирование

```python
import json
import logging

def log_notification_delivery(username, service_name, status, response_time, error=None):
    """Логирование доставки уведомления"""
    log_data = {
        "event": "notification_delivery",
        "username": username,
        "service_name": service_name,
        "status": status,
        "response_time": response_time,
        "timestamp": datetime.now().isoformat(),
        "error": error
    }
    
    logger = logging.getLogger('notifications')
    logger.info(json.dumps(log_data))
```

## Мониторинг в реальном времени

### WebSocket для real-time обновлений

```javascript
// Подключение к WebSocket
const ws = new WebSocket('ws://localhost:8080/ws/notifications');

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    
    switch (data.type) {
        case 'NOTIFICATION_SENT':
            updateNotificationStats(data.payload);
            break;
        case 'NOTIFICATION_FAILED':
            showErrorAlert(data.payload);
            break;
        case 'CHANNEL_STATUS_CHANGED':
            updateChannelStatus(data.payload);
            break;
    }
};

function updateNotificationStats(payload) {
    // Обновление статистики в реальном времени
    const statsElement = document.getElementById('notification-stats');
    statsElement.textContent = `Delivered: ${payload.totalDelivered}`;
}
```

### Dashboard в реальном времени

```html
<!DOCTYPE html>
<html>
<head>
    <title>Notification System Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="dashboard">
        <div class="stats">
            <div class="stat-card">
                <h3>Total Delivered</h3>
                <span id="total-delivered">0</span>
            </div>
            <div class="stat-card">
                <h3>Success Rate</h3>
                <span id="success-rate">0%</span>
            </div>
            <div class="stat-card">
                <h3>Failed</h3>
                <span id="failed-count">0</span>
            </div>
        </div>
        
        <div class="chart-container">
            <canvas id="delivery-chart"></canvas>
        </div>
    </div>
    
    <script>
        // Инициализация графика
        const ctx = document.getElementById('delivery-chart').getContext('2d');
        const chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Delivered',
                    data: [],
                    borderColor: 'rgb(75, 192, 192)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
        
        // WebSocket подключение
        const ws = new WebSocket('ws://localhost:8080/ws/notifications');
        
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            updateDashboard(data);
        };
        
        function updateDashboard(data) {
            // Обновление статистики
            document.getElementById('total-delivered').textContent = data.totalDelivered;
            document.getElementById('success-rate').textContent = data.successRate + '%';
            document.getElementById('failed-count').textContent = data.failed;
            
            // Обновление графика
            const now = new Date().toLocaleTimeString();
            chart.data.labels.push(now);
            chart.data.datasets[0].data.push(data.totalDelivered);
            
            // Ограничение количества точек
            if (chart.data.labels.length > 20) {
                chart.data.labels.shift();
                chart.data.datasets[0].data.shift();
            }
            
            chart.update();
        }
    </script>
</body>
</html>
```

## Автоматизация мониторинга

### Скрипт мониторинга

```python
#!/usr/bin/env python3
"""
Скрипт для мониторинга системы уведомлений
"""

import requests
import time
import json
import logging
from datetime import datetime

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class NotificationMonitor:
    def __init__(self, python_bot_url, java_backend_url, jwt_token):
        self.python_bot_url = python_bot_url
        self.java_backend_url = java_backend_url
        self.jwt_token = jwt_token
        self.headers = {
            "Authorization": f"Bearer {jwt_token}",
            "Content-Type": "application/json"
        }
    
    def check_python_bot_health(self):
        """Проверка здоровья Python бота"""
        try:
            response = requests.get(f"{self.python_bot_url}/health", timeout=5)
            if response.status_code == 200:
                data = response.json()
                logger.info(f"Python Bot: {data['status']}")
                return data['status'] == 'healthy'
            else:
                logger.error(f"Python Bot health check failed: {response.status_code}")
                return False
        except Exception as e:
            logger.error(f"Python Bot health check error: {e}")
            return False
    
    def check_java_backend_health(self):
        """Проверка здоровья Java бэкенда"""
        try:
            response = requests.get(f"{self.java_backend_url}/actuator/health", timeout=5)
            if response.status_code == 200:
                data = response.json()
                logger.info(f"Java Backend: {data['status']}")
                return data['status'] == 'UP'
            else:
                logger.error(f"Java Backend health check failed: {response.status_code}")
                return False
        except Exception as e:
            logger.error(f"Java Backend health check error: {e}")
            return False
    
    def get_delivery_stats(self):
        """Получение статистики доставки"""
        try:
            response = requests.get(
                f"{self.java_backend_url}/api/v1/notifications/deliveries/stats",
                headers=self.headers,
                timeout=10
            )
            if response.status_code == 200:
                return response.json()
            else:
                logger.error(f"Failed to get delivery stats: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Error getting delivery stats: {e}")
            return None
    
    def check_success_rate(self, stats):
        """Проверка процента успешности"""
        if not stats:
            return False
        
        success_rate = stats.get('successRate', 0)
        if success_rate < 90:  # Порог 90%
            logger.warning(f"Low success rate: {success_rate}%")
            return False
        
        return True
    
    def send_alert(self, message):
        """Отправка алерта"""
        try:
            payload = {
                "username": "monitor",
                "serviceName": "Notification Monitor",
                "serviceUrl": "http://localhost:8080",
                "status": "DOWN",
                "severity": "WARNING",
                "message": message
            }
            
            response = requests.post(
                f"{self.java_backend_url}/api/v1/notifications/send",
                json=payload,
                headers=self.headers,
                timeout=10
            )
            
            if response.status_code == 200:
                logger.info("Alert sent successfully")
            else:
                logger.error(f"Failed to send alert: {response.status_code}")
        except Exception as e:
            logger.error(f"Error sending alert: {e}")
    
    def run_monitoring_cycle(self):
        """Выполнение цикла мониторинга"""
        logger.info("Starting monitoring cycle")
        
        # Проверка здоровья компонентов
        python_bot_healthy = self.check_python_bot_health()
        java_backend_healthy = self.check_java_backend_health()
        
        if not python_bot_healthy:
            self.send_alert("Python Bot is not healthy")
        
        if not java_backend_healthy:
            self.send_alert("Java Backend is not healthy")
        
        # Проверка статистики доставки
        if java_backend_healthy:
            stats = self.get_delivery_stats()
            if not self.check_success_rate(stats):
                self.send_alert(f"Low notification success rate: {stats.get('successRate', 0)}%")
        
        logger.info("Monitoring cycle completed")
    
    def start_monitoring(self, interval=300):  # 5 минут
        """Запуск мониторинга"""
        logger.info(f"Starting notification system monitoring (interval: {interval}s)")
        
        while True:
            try:
                self.run_monitoring_cycle()
                time.sleep(interval)
            except KeyboardInterrupt:
                logger.info("Monitoring stopped by user")
                break
            except Exception as e:
                logger.error(f"Monitoring error: {e}")
                time.sleep(60)  # Пауза при ошибке

if __name__ == "__main__":
    monitor = NotificationMonitor(
        python_bot_url="http://localhost:5000",
        java_backend_url="http://localhost:8080",
        jwt_token="your_jwt_token_here"
    )
    
    monitor.start_monitoring(interval=300)  # 5 минут
```

## Рекомендации

### Лучшие практики

1. **Мониторьте все компоненты** - Python Bot, Java Backend, база данных
2. **Настройте алерты** на критические метрики
3. **Используйте структурированное логирование** для анализа
4. **Регулярно проверяйте** статистику доставки
5. **Настройте автоматическое восстановление** при сбоях

### Критические метрики

1. **Процент успешной доставки** - должен быть > 95%
2. **Время отклика каналов** - должно быть < 5 секунд
3. **Доступность компонентов** - должна быть > 99%
4. **Количество ошибок** - должно быть минимальным
5. **Использование ресурсов** - CPU, память, диск
