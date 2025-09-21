# Примеры интеграции

## Обзор

Данный раздел содержит практические примеры интеграции системы уведомлений PingTower с различными сервисами и приложениями.

## Python скрипт для отправки уведомлений

### Базовый скрипт

```python
#!/usr/bin/env python3
"""
Скрипт для отправки уведомлений через PingTower
"""

import requests
import json
import time
import sys
from datetime import datetime

class PingTowerNotifier:
    def __init__(self, python_bot_url="http://localhost:5000", 
                 java_backend_url="http://localhost:8080", 
                 jwt_token=None):
        self.python_bot_url = python_bot_url
        self.java_backend_url = java_backend_url
        self.jwt_token = jwt_token
    
    def send_via_python_bot(self, username, service_name, service_url, status, message):
        """Отправка уведомления через Python бот"""
        url = f"{self.python_bot_url}/send_notification"
        payload = {
            "username": username,
            "service_name": service_name,
            "service_url": service_url,
            "status": status,
            "message": message
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print(f"Ошибка отправки через Python бот: {e}")
            return None
    
    def send_via_java_backend(self, username, service_name, service_url, status, severity, message):
        """Отправка уведомления через Java бэкенд"""
        if not self.jwt_token:
            print("JWT токен не предоставлен")
            return None
        
        url = f"{self.java_backend_url}/api/v1/notifications/send"
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
            "Authorization": f"Bearer {self.jwt_token}"
        }
        
        try:
            response = requests.post(url, json=payload, headers=headers, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print(f"Ошибка отправки через Java бэкенд: {e}")
            return None
    
    def send_batch_notifications(self, notifications):
        """Отправка множественных уведомлений"""
        if not self.jwt_token:
            print("JWT токен не предоставлен")
            return None
        
        url = f"{self.java_backend_url}/api/v1/notifications/send/batch"
        payload = {"notifications": notifications}
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.jwt_token}"
        }
        
        try:
            response = requests.post(url, json=payload, headers=headers, timeout=30)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print(f"Ошибка batch отправки: {e}")
            return None

def main():
    """Основная функция"""
    print("🚀 PingTower Notification Sender")
    print("=" * 40)
    
    # Инициализация
    notifier = PingTowerNotifier(
        python_bot_url="http://localhost:5000",
        java_backend_url="http://localhost:8080",
        jwt_token="your_jwt_token_here"  # Замените на ваш токен
    )
    
    # Пример отправки уведомления
    result = notifier.send_via_python_bot(
        username="testuser",
        service_name="My Web Service",
        service_url="https://example.com",
        status="down",
        message="Сервер не отвечает на запросы"
    )
    
    if result:
        print(f"✅ Уведомление отправлено: {result}")
    else:
        print("❌ Ошибка отправки уведомления")

if __name__ == "__main__":
    main()
```

### Расширенный скрипт с мониторингом

```python
#!/usr/bin/env python3
"""
Расширенный скрипт для мониторинга и отправки уведомлений
"""

import requests
import json
import time
import threading
from datetime import datetime, timedelta
from typing import List, Dict, Optional

class ServiceMonitor:
    def __init__(self, notifier: PingTowerNotifier):
        self.notifier = notifier
        self.services = []
        self.monitoring = False
        self.threads = []
    
    def add_service(self, name: str, url: str, username: str, 
                   check_interval: int = 60, timeout: int = 30):
        """Добавление сервиса для мониторинга"""
        service = {
            "name": name,
            "url": url,
            "username": username,
            "check_interval": check_interval,
            "timeout": timeout,
            "last_check": None,
            "last_status": None,
            "consecutive_failures": 0
        }
        self.services.append(service)
        print(f"✅ Добавлен сервис: {name}")
    
    def check_service(self, service: Dict) -> bool:
        """Проверка доступности сервиса"""
        try:
            response = requests.get(
                service["url"], 
                timeout=service["timeout"],
                allow_redirects=True
            )
            
            is_healthy = response.status_code < 400
            service["last_check"] = datetime.now()
            service["last_status"] = "UP" if is_healthy else "DOWN"
            
            if is_healthy:
                service["consecutive_failures"] = 0
            else:
                service["consecutive_failures"] += 1
            
            return is_healthy
            
        except requests.exceptions.RequestException as e:
            service["last_check"] = datetime.now()
            service["last_status"] = "DOWN"
            service["consecutive_failures"] += 1
            print(f"❌ Ошибка проверки {service['name']}: {e}")
            return False
    
    def monitor_service(self, service: Dict):
        """Мониторинг одного сервиса"""
        while self.monitoring:
            try:
                is_healthy = self.check_service(service)
                
                # Отправка уведомления при изменении статуса
                if service["consecutive_failures"] == 1 and not is_healthy:
                    # Первый сбой
                    self.notifier.send_via_python_bot(
                        username=service["username"],
                        service_name=service["name"],
                        service_url=service["url"],
                        status="down",
                        message=f"Сервис {service['name']} недоступен"
                    )
                    print(f"🚨 Отправлено уведомление о сбое: {service['name']}")
                
                elif service["consecutive_failures"] == 0 and is_healthy:
                    # Восстановление
                    self.notifier.send_via_python_bot(
                        username=service["username"],
                        service_name=service["name"],
                        service_url=service["url"],
                        status="up",
                        message=f"Сервис {service['name']} восстановлен"
                    )
                    print(f"✅ Отправлено уведомление о восстановлении: {service['name']}")
                
                time.sleep(service["check_interval"])
                
            except Exception as e:
                print(f"❌ Ошибка мониторинга {service['name']}: {e}")
                time.sleep(service["check_interval"])
    
    def start_monitoring(self):
        """Запуск мониторинга всех сервисов"""
        if not self.services:
            print("❌ Нет сервисов для мониторинга")
            return
        
        self.monitoring = True
        print(f"🚀 Запуск мониторинга {len(self.services)} сервисов")
        
        for service in self.services:
            thread = threading.Thread(target=self.monitor_service, args=(service,))
            thread.daemon = True
            thread.start()
            self.threads.append(thread)
        
        try:
            while self.monitoring:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\n🛑 Остановка мониторинга...")
            self.stop_monitoring()
    
    def stop_monitoring(self):
        """Остановка мониторинга"""
        self.monitoring = False
        for thread in self.threads:
            thread.join(timeout=5)
        print("✅ Мониторинг остановлен")
    
    def get_status_report(self) -> Dict:
        """Получение отчета о статусе сервисов"""
        report = {
            "timestamp": datetime.now().isoformat(),
            "total_services": len(self.services),
            "healthy_services": 0,
            "unhealthy_services": 0,
            "services": []
        }
        
        for service in self.services:
            service_report = {
                "name": service["name"],
                "url": service["url"],
                "status": service["last_status"],
                "last_check": service["last_check"].isoformat() if service["last_check"] else None,
                "consecutive_failures": service["consecutive_failures"]
            }
            
            if service["last_status"] == "UP":
                report["healthy_services"] += 1
            else:
                report["unhealthy_services"] += 1
            
            report["services"].append(service_report)
        
        return report

def main():
    """Основная функция"""
    print("🔍 PingTower Service Monitor")
    print("=" * 40)
    
    # Инициализация
    notifier = PingTowerNotifier()
    monitor = ServiceMonitor(notifier)
    
    # Добавление сервисов для мониторинга
    monitor.add_service(
        name="API Gateway",
        url="https://api.example.com/health",
        username="admin",
        check_interval=60
    )
    
    monitor.add_service(
        name="Database",
        url="https://db.example.com/ping",
        username="admin",
        check_interval=30
    )
    
    monitor.add_service(
        name="Frontend",
        url="https://app.example.com",
        username="admin",
        check_interval=120
    )
    
    # Запуск мониторинга
    try:
        monitor.start_monitoring()
    except KeyboardInterrupt:
        print("\n🛑 Остановка...")
        monitor.stop_monitoring()

if __name__ == "__main__":
    main()
```

## Bash скрипт для мониторинга

### Простой мониторинг

```bash
#!/bin/bash
# monitor_services.sh

# Конфигурация
PYTHON_BOT_URL="http://localhost:5000"
SERVICES_FILE="services.txt"
LOG_FILE="monitor.log"

# Функция логирования
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# Функция проверки сервиса
check_service() {
    local name="$1"
    local url="$2"
    local username="$3"
    
    log "Проверка сервиса: $name"
    
    if curl -s --max-time 30 "$url" > /dev/null 2>&1; then
        log "✅ $name - UP"
        return 0
    else
        log "❌ $name - DOWN"
        
        # Отправка уведомления
        send_notification "$username" "$name" "$url" "down" "Сервис недоступен"
        return 1
    fi
}

# Функция отправки уведомления
send_notification() {
    local username="$1"
    local service_name="$2"
    local service_url="$3"
    local status="$4"
    local message="$5"
    
    local payload=$(cat <<EOF
{
    "username": "$username",
    "service_name": "$service_name",
    "service_url": "$service_url",
    "status": "$status",
    "message": "$message"
}
EOF
)
    
    if curl -s -X POST "$PYTHON_BOT_URL/send_notification" \
        -H "Content-Type: application/json" \
        -d "$payload" > /dev/null; then
        log "📱 Уведомление отправлено для $service_name"
    else
        log "❌ Ошибка отправки уведомления для $service_name"
    fi
}

# Основной цикл мониторинга
main() {
    log "🚀 Запуск мониторинга сервисов"
    
    if [ ! -f "$SERVICES_FILE" ]; then
        log "❌ Файл $SERVICES_FILE не найден"
        exit 1
    fi
    
    while true; do
        while IFS=',' read -r name url username; do
            # Пропуск пустых строк и комментариев
            [[ -z "$name" || "$name" =~ ^# ]] && continue
            
            check_service "$name" "$url" "$username"
        done < "$SERVICES_FILE"
        
        log "⏳ Ожидание 60 секунд..."
        sleep 60
    done
}

# Обработка сигналов
trap 'log "🛑 Остановка мониторинга"; exit 0' INT TERM

# Запуск
main
```

### Расширенный мониторинг с уведомлениями

```bash
#!/bin/bash
# advanced_monitor.sh

# Конфигурация
PYTHON_BOT_URL="http://localhost:5000"
JAVA_BACKEND_URL="http://localhost:8080"
JWT_TOKEN="your_jwt_token_here"
SERVICES_FILE="services.json"
LOG_FILE="advanced_monitor.log"
STATE_FILE="service_states.json"

# Инициализация файла состояний
init_state_file() {
    if [ ! -f "$STATE_FILE" ]; then
        echo '{}' > "$STATE_FILE"
    fi
}

# Функция логирования
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# Функция получения состояния сервиса
get_service_state() {
    local service_name="$1"
    jq -r ".$service_name // \"unknown\"" "$STATE_FILE"
}

# Функция установки состояния сервиса
set_service_state() {
    local service_name="$1"
    local state="$2"
    local temp_file=$(mktemp)
    jq ".$service_name = \"$state\"" "$STATE_FILE" > "$temp_file" && mv "$temp_file" "$STATE_FILE"
}

# Функция проверки сервиса
check_service() {
    local name="$1"
    local url="$2"
    local username="$3"
    local timeout="${4:-30}"
    
    local current_state=$(get_service_state "$name")
    local new_state="unknown"
    
    log "Проверка сервиса: $name"
    
    if curl -s --max-time "$timeout" "$url" > /dev/null 2>&1; then
        new_state="up"
        log "✅ $name - UP"
    else
        new_state="down"
        log "❌ $name - DOWN"
    fi
    
    # Отправка уведомления при изменении состояния
    if [ "$current_state" != "$new_state" ] && [ "$current_state" != "unknown" ]; then
        if [ "$new_state" = "down" ]; then
            send_notification "$username" "$name" "$url" "down" "Сервис недоступен"
        else
            send_notification "$username" "$name" "$url" "up" "Сервис восстановлен"
        fi
    fi
    
    set_service_state "$name" "$new_state"
    return $([ "$new_state" = "up" ] && echo 0 || echo 1)
}

# Функция отправки уведомления через Java бэкенд
send_notification() {
    local username="$1"
    local service_name="$2"
    local service_url="$3"
    local status="$4"
    local message="$5"
    
    local payload=$(cat <<EOF
{
    "username": "$username",
    "serviceName": "$service_name",
    "serviceUrl": "$service_url",
    "status": "$status",
    "severity": "ERROR",
    "message": "$message"
}
EOF
)
    
    if curl -s -X POST "$JAVA_BACKEND_URL/api/v1/notifications/send" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$payload" > /dev/null; then
        log "📱 Уведомление отправлено для $service_name"
    else
        log "❌ Ошибка отправки уведомления для $service_name"
    fi
}

# Функция получения статистики
get_stats() {
    local total_services=$(jq '. | length' "$SERVICES_FILE")
    local up_services=0
    local down_services=0
    
    while IFS= read -r service; do
        local name=$(echo "$service" | jq -r '.name')
        local state=$(get_service_state "$name")
        
        if [ "$state" = "up" ]; then
            ((up_services++))
        elif [ "$state" = "down" ]; then
            ((down_services++))
        fi
    done < <(jq -c '.[]' "$SERVICES_FILE")
    
    echo "Всего сервисов: $total_services, UP: $up_services, DOWN: $down_services"
}

# Функция отправки отчета
send_report() {
    local stats=$(get_stats)
    local report_message="Отчет мониторинга: $stats"
    
    send_notification "admin" "Monitoring System" "http://localhost:8080" "info" "$report_message"
    log "📊 Отчет отправлен: $stats"
}

# Основной цикл мониторинга
main() {
    log "🚀 Запуск расширенного мониторинга сервисов"
    
    if [ ! -f "$SERVICES_FILE" ]; then
        log "❌ Файл $SERVICES_FILE не найден"
        exit 1
    fi
    
    init_state_file
    
    local cycle_count=0
    
    while true; do
        local services_checked=0
        local services_up=0
        local services_down=0
        
        while IFS= read -r service; do
            local name=$(echo "$service" | jq -r '.name')
            local url=$(echo "$service" | jq -r '.url')
            local username=$(echo "$service" | jq -r '.username')
            local timeout=$(echo "$service" | jq -r '.timeout // 30')
            
            if check_service "$name" "$url" "$username" "$timeout"; then
                ((services_up++))
            else
                ((services_down++))
            fi
            
            ((services_checked++))
        done < <(jq -c '.[]' "$SERVICES_FILE")
        
        log "📊 Цикл $((++cycle_count)): проверено $services_checked, UP: $services_up, DOWN: $services_down"
        
        # Отправка отчета каждые 10 циклов
        if [ $((cycle_count % 10)) -eq 0 ]; then
            send_report
        fi
        
        log "⏳ Ожидание 60 секунд..."
        sleep 60
    done
}

# Обработка сигналов
trap 'log "🛑 Остановка мониторинга"; exit 0' INT TERM

# Запуск
main
```

### Файл конфигурации сервисов (services.json)

```json
[
    {
        "name": "API Gateway",
        "url": "https://api.example.com/health",
        "username": "admin",
        "timeout": 30
    },
    {
        "name": "Database",
        "url": "https://db.example.com/ping",
        "username": "admin",
        "timeout": 15
    },
    {
        "name": "Frontend",
        "url": "https://app.example.com",
        "username": "admin",
        "timeout": 45
    },
    {
        "name": "Payment Service",
        "url": "https://payments.example.com/status",
        "username": "admin",
        "timeout": 20
    }
]
```

## Node.js интеграция

### Express.js middleware

```javascript
// notification-middleware.js
const axios = require('axios');

class NotificationMiddleware {
    constructor(config) {
        this.pythonBotUrl = config.pythonBotUrl || 'http://localhost:5000';
        this.javaBackendUrl = config.javaBackendUrl || 'http://localhost:8080';
        this.jwtToken = config.jwtToken;
    }

    // Middleware для отправки уведомлений об ошибках
    errorNotification(req, res, next) {
        const originalSend = res.send;
        
        res.send = function(data) {
            // Отправка уведомления при ошибке 5xx
            if (res.statusCode >= 500) {
                this.sendErrorNotification(req, res, data);
            }
            
            originalSend.call(this, data);
        }.bind(this);
        
        next();
    }

    async sendErrorNotification(req, res, errorData) {
        try {
            const payload = {
                username: 'system',
                service_name: req.app.get('serviceName') || 'API Service',
                service_url: `${req.protocol}://${req.get('host')}${req.originalUrl}`,
                status: 'down',
                message: `HTTP ${res.statusCode} error: ${errorData}`
            };

            await axios.post(`${this.pythonBotUrl}/send_notification`, payload);
            console.log('Error notification sent');
        } catch (error) {
            console.error('Failed to send error notification:', error.message);
        }
    }

    // Метод для отправки кастомных уведомлений
    async sendNotification(username, serviceName, serviceUrl, status, message) {
        try {
            const payload = {
                username,
                service_name: serviceName,
                service_url: serviceUrl,
                status,
                message
            };

            const response = await axios.post(`${this.pythonBotUrl}/send_notification`, payload);
            return response.data;
        } catch (error) {
            console.error('Failed to send notification:', error.message);
            throw error;
        }
    }
}

module.exports = NotificationMiddleware;
```

### Использование в Express приложении

```javascript
// app.js
const express = require('express');
const NotificationMiddleware = require('./notification-middleware');

const app = express();
const notificationMiddleware = new NotificationMiddleware({
    pythonBotUrl: 'http://localhost:5000',
    serviceName: 'My API Service'
});

// Middleware для уведомлений об ошибках
app.use(notificationMiddleware.errorNotification.bind(notificationMiddleware));

// Роуты
app.get('/health', (req, res) => {
    res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

app.get('/api/data', async (req, res) => {
    try {
        // Симуляция ошибки
        if (Math.random() < 0.1) {
            throw new Error('Random error for testing');
        }
        
        res.json({ data: 'success' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Ручная отправка уведомления
app.post('/api/notify', async (req, res) => {
    try {
        const { username, serviceName, serviceUrl, status, message } = req.body;
        
        const result = await notificationMiddleware.sendNotification(
            username, serviceName, serviceUrl, status, message
        );
        
        res.json({ success: true, result });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
```

## Docker интеграция

### Docker Compose с мониторингом

```yaml
# docker-compose.yml
version: '3.8'

services:
  python-bot:
    build: ./bot
    ports:
      - "5000:5000"
    environment:
      - TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
    volumes:
      - ./bot/monitoring_bot.db:/app/monitoring_bot.db
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:5000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  java-backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/pingtower
      - SPRING_DATASOURCE_USERNAME=pingtower
      - SPRING_DATASOURCE_PASSWORD=password
    depends_on:
      - python-bot
      - postgres
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=pingtower
      - POSTGRES_USER=pingtower
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U pingtower"]
      interval: 30s
      timeout: 10s
      retries: 3

  monitor:
    build: ./monitor
    environment:
      - PYTHON_BOT_URL=http://python-bot:5000
      - JAVA_BACKEND_URL=http://java-backend:8080
      - JWT_TOKEN=${JWT_TOKEN}
    depends_on:
      - python-bot
      - java-backend
    volumes:
      - ./monitor/services.json:/app/services.json
      - ./monitor/logs:/app/logs

volumes:
  postgres_data:
```

### Dockerfile для мониторинга

```dockerfile
# monitor/Dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY monitor.py .
COPY services.json .

CMD ["python", "monitor.py"]
```

## Kubernetes интеграция

### Deployment для мониторинга

```yaml
# k8s/monitor-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pingtower-monitor
spec:
  replicas: 1
  selector:
    matchLabels:
      app: pingtower-monitor
  template:
    metadata:
      labels:
        app: pingtower-monitor
    spec:
      containers:
      - name: monitor
        image: pingtower/monitor:latest
        env:
        - name: PYTHON_BOT_URL
          value: "http://pingtower-python-bot:5000"
        - name: JAVA_BACKEND_URL
          value: "http://pingtower-java-backend:8080"
        - name: JWT_TOKEN
          valueFrom:
            secretKeyRef:
              name: pingtower-secrets
              key: jwt-token
        volumeMounts:
        - name: services-config
          mountPath: /app/services.json
          subPath: services.json
        - name: logs
          mountPath: /app/logs
      volumes:
      - name: services-config
        configMap:
          name: services-config
      - name: logs
        emptyDir: {}
```

### ConfigMap для сервисов

```yaml
# k8s/services-configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: services-config
data:
  services.json: |
    [
      {
        "name": "API Gateway",
        "url": "https://api.example.com/health",
        "username": "admin",
        "timeout": 30
      },
      {
        "name": "Database",
        "url": "https://db.example.com/ping",
        "username": "admin",
        "timeout": 15
      }
    ]
```

## CI/CD интеграция

### GitHub Actions

```yaml
# .github/workflows/notify-deployment.yml
name: Deployment Notification

on:
  deployment_status:
    types: [success, failure]

jobs:
  notify:
    runs-on: ubuntu-latest
    steps:
    - name: Send deployment notification
      run: |
        if [ "${{ github.event.deployment_status.state }}" = "success" ]; then
          STATUS="up"
          MESSAGE="Deployment successful"
        else
          STATUS="down"
          MESSAGE="Deployment failed"
        fi
        
        curl -X POST http://localhost:5000/send_notification \
          -H "Content-Type: application/json" \
          -d "{
            \"username\": \"ci-cd\",
            \"service_name\": \"${{ github.repository }}\",
            \"service_url\": \"https://github.com/${{ github.repository }}\",
            \"status\": \"$STATUS\",
            \"message\": \"$MESSAGE - ${{ github.event.deployment_status.description }}\"
          }"
```

### Jenkins Pipeline

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    stages {
        stage('Deploy') {
            steps {
                script {
                    try {
                        // Ваш процесс деплоя
                        sh 'kubectl apply -f k8s/'
                        
                        // Уведомление об успехе
                        sendNotification('admin', 'My Service', 'https://my-service.com', 'up', 'Deployment successful')
                    } catch (Exception e) {
                        // Уведомление об ошибке
                        sendNotification('admin', 'My Service', 'https://my-service.com', 'down', "Deployment failed: ${e.message}")
                        throw e
                    }
                }
            }
        }
    }
}

def sendNotification(username, serviceName, serviceUrl, status, message) {
    sh """
        curl -X POST http://localhost:5000/send_notification \\
          -H "Content-Type: application/json" \\
          -d '{
            "username": "${username}",
            "service_name": "${serviceName}",
            "service_url": "${serviceUrl}",
            "status": "${status}",
            "message": "${message}"
          }'
    """
}
```

## Рекомендации по интеграции

### Лучшие практики

1. **Используйте retry механизм** для надежности
2. **Логируйте все операции** для отладки
3. **Настройте мониторинг** самой системы уведомлений
4. **Используйте batch отправку** для множественных уведомлений
5. **Настройте rate limiting** для предотвращения спама

### Безопасность

1. **Не храните токены** в коде
2. **Используйте переменные окружения** для конфигурации
3. **Валидируйте входные данные** перед отправкой
4. **Используйте HTTPS** для всех запросов
5. **Настройте аутентификацию** для API

### Производительность

1. **Используйте асинхронную отправку** где возможно
2. **Настройте connection pooling** для HTTP клиентов
3. **Кэшируйте результаты** проверок сервисов
4. **Оптимизируйте частоту** проверок
5. **Используйте batch операции** для множественных уведомлений
