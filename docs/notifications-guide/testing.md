# Тестирование системы уведомлений

## Обзор

Данный раздел описывает различные способы тестирования системы уведомлений PingTower.

## Автоматическое тестирование

### Запуск тестового скрипта

```bash
cd bot
python test_notification.py
```

Скрипт предложит выбрать способ тестирования:
1. Прямое обращение к Python боту
2. Через Java бэкенд
3. Оба способа

### Содержимое тестового скрипта

```python
#!/usr/bin/env python3
"""
Тестовый скрипт для проверки системы уведомлений PingTower
"""

import requests
import json
import time
import sys

# Конфигурация
PYTHON_BOT_URL = "http://localhost:5000"
JAVA_BACKEND_URL = "http://localhost:8080"
JWT_TOKEN = "your_jwt_token_here"  # Замените на ваш токен

def test_python_bot():
    """Тестирование прямого обращения к Python боту"""
    print("🧪 Тестирование Python бота...")
    
    url = f"{PYTHON_BOT_URL}/send_notification"
    payload = {
        "username": "testuser",
        "service_name": "Test Service",
        "service_url": "https://example.com",
        "status": "down",
        "message": "Тестовое уведомление от Python бота"
    }
    
    try:
        response = requests.post(url, json=payload, timeout=10)
        response.raise_for_status()
        
        result = response.json()
        print(f"✅ Python бот: {result}")
        return True
    except requests.exceptions.RequestException as e:
        print(f"❌ Python бот: Ошибка - {e}")
        return False

def test_java_backend():
    """Тестирование через Java бэкенд"""
    print("🧪 Тестирование Java бэкенда...")
    
    url = f"{JAVA_BACKEND_URL}/api/v1/notifications/send"
    payload = {
        "username": "testuser",
        "serviceName": "Test Service",
        "serviceUrl": "https://example.com",
        "status": "DOWN",
        "severity": "ERROR",
        "message": "Тестовое уведомление от Java бэкенда"
    }
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {JWT_TOKEN}"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers, timeout=10)
        response.raise_for_status()
        
        result = response.json()
        print(f"✅ Java бэкенд: {result}")
        return True
    except requests.exceptions.RequestException as e:
        print(f"❌ Java бэкенд: Ошибка - {e}")
        return False

def test_health_checks():
    """Проверка здоровья компонентов"""
    print("🏥 Проверка здоровья компонентов...")
    
    # Python бот
    try:
        response = requests.get(f"{PYTHON_BOT_URL}/health", timeout=5)
        if response.status_code == 200:
            print("✅ Python бот: Здоров")
        else:
            print(f"⚠️ Python бот: Статус {response.status_code}")
    except requests.exceptions.RequestException as e:
        print(f"❌ Python бот: Недоступен - {e}")
    
    # Java бэкенд
    try:
        response = requests.get(f"{JAVA_BACKEND_URL}/actuator/health", timeout=5)
        if response.status_code == 200:
            print("✅ Java бэкенд: Здоров")
        else:
            print(f"⚠️ Java бэкенд: Статус {response.status_code}")
    except requests.exceptions.RequestException as e:
        print(f"❌ Java бэкенд: Недоступен - {e}")

def test_user_registration():
    """Тестирование регистрации пользователя"""
    print("👤 Тестирование регистрации пользователя...")
    
    url = f"{PYTHON_BOT_URL}/register"
    payload = {
        "username": "testuser",
        "password": "testpassword"
    }
    
    try:
        response = requests.post(url, json=payload, timeout=10)
        if response.status_code == 200:
            print("✅ Пользователь зарегистрирован")
        elif response.status_code == 409:
            print("ℹ️ Пользователь уже существует")
        else:
            print(f"⚠️ Статус регистрации: {response.status_code}")
    except requests.exceptions.RequestException as e:
        print(f"❌ Ошибка регистрации: {e}")

def main():
    """Основная функция тестирования"""
    print("🚀 Запуск тестирования системы уведомлений PingTower")
    print("=" * 50)
    
    # Проверка здоровья
    test_health_checks()
    print()
    
    # Регистрация пользователя
    test_user_registration()
    print()
    
    # Выбор способа тестирования
    print("Выберите способ тестирования:")
    print("1. Прямое обращение к Python боту")
    print("2. Через Java бэкенд")
    print("3. Оба способа")
    
    choice = input("Введите номер (1-3): ").strip()
    
    if choice == "1":
        test_python_bot()
    elif choice == "2":
        test_java_backend()
    elif choice == "3":
        test_python_bot()
        print()
        test_java_backend()
    else:
        print("❌ Неверный выбор")
        sys.exit(1)
    
    print("\n🎉 Тестирование завершено!")

if __name__ == "__main__":
    main()
```

## Ручное тестирование

### 1. Регистрация в боте

1. **Отправьте `/start` боту в Telegram**
2. **Пройдите регистрацию с логином и паролем**
3. **Проверьте успешную регистрацию**

### 2. Отправка тестового уведомления

#### Через Python бот
```bash
curl -X POST http://localhost:5000/send_notification \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "service_name": "Test Service",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Тестовое уведомление"
  }'
```

#### Через Java бэкенд
```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "testuser",
    "serviceName": "Test Service",
    "serviceUrl": "https://example.com",
    "status": "DOWN",
    "severity": "ERROR",
    "message": "Тестовое уведомление"
  }'
```

### 3. Проверка получения уведомления

1. **Откройте Telegram**
2. **Найдите чат с ботом**
3. **Проверьте получение уведомления**

## Unit тестирование

### Тестирование Python бота

```python
import unittest
import requests
from unittest.mock import patch, MagicMock

class TestPythonBot(unittest.TestCase):
    
    def setUp(self):
        self.base_url = "http://localhost:5000"
        self.test_payload = {
            "username": "testuser",
            "service_name": "Test Service",
            "service_url": "https://example.com",
            "status": "down",
            "message": "Test message"
        }
    
    def test_send_notification_success(self):
        """Тест успешной отправки уведомления"""
        with patch('requests.post') as mock_post:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {"success": True}
            mock_post.return_value = mock_response
            
            response = requests.post(
                f"{self.base_url}/send_notification",
                json=self.test_payload
            )
            
            self.assertEqual(response.status_code, 200)
            self.assertTrue(response.json()["success"])
    
    def test_send_notification_invalid_user(self):
        """Тест отправки уведомления несуществующему пользователю"""
        with patch('requests.post') as mock_post:
            mock_response = MagicMock()
            mock_response.status_code = 404
            mock_response.json.return_value = {"error": "User not found"}
            mock_post.return_value = mock_response
            
            response = requests.post(
                f"{self.base_url}/send_notification",
                json=self.test_payload
            )
            
            self.assertEqual(response.status_code, 404)
            self.assertIn("error", response.json())
    
    def test_health_check(self):
        """Тест проверки здоровья бота"""
        with patch('requests.get') as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {"status": "healthy"}
            mock_get.return_value = mock_response
            
            response = requests.get(f"{self.base_url}/health")
            
            self.assertEqual(response.status_code, 200)
            self.assertEqual(response.json()["status"], "healthy")

if __name__ == '__main__':
    unittest.main()
```

### Тестирование Java бэкенда

```java
@SpringBootTest
@AutoConfigureTestDatabase
class NotificationControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private NotificationChannelRepository channelRepository;
    
    @Test
    void testSendNotificationSuccess() {
        // Создание тестового канала
        NotificationChannel channel = new NotificationChannel();
        channel.setName("Test Channel");
        channel.setType("PYTHON_BOT");
        channel.setEnabled(true);
        channelRepository.save(channel);
        
        // Подготовка запроса
        SendNotificationRequest request = new SendNotificationRequest();
        request.setUsername("testuser");
        request.setServiceName("Test Service");
        request.setServiceUrl("https://example.com");
        request.setStatus("DOWN");
        request.setSeverity("ERROR");
        request.setMessage("Test notification");
        
        // Отправка запроса
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/notifications/send",
            request,
            String.class
        );
        
        // Проверка результата
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testSendNotificationInvalidUser() {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setUsername("nonexistent");
        request.setServiceName("Test Service");
        request.setServiceUrl("https://example.com");
        request.setStatus("DOWN");
        request.setMessage("Test notification");
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/notifications/send",
            request,
            String.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
```

## Интеграционное тестирование

### Тест полного потока

```python
import pytest
import requests
import time

class TestNotificationFlow:
    
    @pytest.fixture
    def setup_user(self):
        """Настройка тестового пользователя"""
        # Регистрация пользователя
        response = requests.post(
            "http://localhost:5000/register",
            json={"username": "testuser", "password": "testpass"}
        )
        return response.status_code in [200, 409]  # 409 = уже существует
    
    def test_full_notification_flow(self, setup_user):
        """Тест полного потока уведомления"""
        assert setup_user, "Пользователь должен быть зарегистрирован"
        
        # Отправка уведомления через Python бот
        response = requests.post(
            "http://localhost:5000/send_notification",
            json={
                "username": "testuser",
                "service_name": "Test Service",
                "service_url": "https://example.com",
                "status": "down",
                "message": "Integration test notification"
            }
        )
        
        assert response.status_code == 200
        result = response.json()
        assert result.get("success") is True
        
        # Проверка доставки через Java бэкенд
        time.sleep(2)  # Ждем обработки
        
        delivery_response = requests.get(
            "http://localhost:8080/api/v1/notifications/deliveries",
            headers={"Authorization": "Bearer YOUR_JWT_TOKEN"}
        )
        
        assert delivery_response.status_code == 200
        deliveries = delivery_response.json()
        assert len(deliveries) > 0
```

## Нагрузочное тестирование

### Тест производительности

```python
import asyncio
import aiohttp
import time
from concurrent.futures import ThreadPoolExecutor

async def send_notification_async(session, username, service_name, message):
    """Асинхронная отправка уведомления"""
    url = "http://localhost:5000/send_notification"
    payload = {
        "username": username,
        "service_name": service_name,
        "service_url": "https://example.com",
        "status": "down",
        "message": message
    }
    
    try:
        async with session.post(url, json=payload) as response:
            return await response.json()
    except Exception as e:
        return {"error": str(e)}

async def load_test(num_requests=100, concurrent_users=10):
    """Нагрузочный тест"""
    print(f"🚀 Запуск нагрузочного теста: {num_requests} запросов, {concurrent_users} пользователей")
    
    start_time = time.time()
    
    async with aiohttp.ClientSession() as session:
        tasks = []
        for i in range(num_requests):
            username = f"user{i % concurrent_users}"
            service_name = f"Service {i % 10}"
            message = f"Load test message {i}"
            
            task = send_notification_async(session, username, service_name, message)
            tasks.append(task)
        
        results = await asyncio.gather(*tasks, return_exceptions=True)
    
    end_time = time.time()
    duration = end_time - start_time
    
    # Анализ результатов
    successful = sum(1 for r in results if isinstance(r, dict) and r.get("success"))
    failed = len(results) - successful
    
    print(f"📊 Результаты нагрузочного теста:")
    print(f"   Всего запросов: {len(results)}")
    print(f"   Успешных: {successful}")
    print(f"   Неудачных: {failed}")
    print(f"   Время выполнения: {duration:.2f} секунд")
    print(f"   RPS: {len(results) / duration:.2f}")
    print(f"   Успешность: {(successful / len(results)) * 100:.1f}%")

# Запуск нагрузочного теста
if __name__ == "__main__":
    asyncio.run(load_test(num_requests=100, concurrent_users=10))
```

## Тестирование каналов уведомлений

### Тест различных каналов

```python
def test_notification_channels():
    """Тестирование различных каналов уведомлений"""
    
    channels = [
        {
            "name": "Python Bot Channel",
            "type": "PYTHON_BOT",
            "configuration": {"botUrl": "http://localhost:5000"}
        },
        {
            "name": "Telegram Channel",
            "type": "TELEGRAM",
            "configuration": {
                "botToken": "YOUR_BOT_TOKEN",
                "chatId": "YOUR_CHAT_ID"
            }
        },
        {
            "name": "Email Channel",
            "type": "EMAIL",
            "configuration": {
                "smtpHost": "smtp.gmail.com",
                "smtpPort": "587",
                "username": "your-email@gmail.com",
                "password": "your-password"
            }
        }
    ]
    
    for channel in channels:
        print(f"🧪 Тестирование канала: {channel['name']}")
        
        # Создание канала
        response = requests.post(
            "http://localhost:8080/api/v1/notifications/channels",
            json=channel,
            headers={"Authorization": "Bearer YOUR_JWT_TOKEN"}
        )
        
        if response.status_code == 201:
            channel_id = response.json()["id"]
            print(f"✅ Канал создан с ID: {channel_id}")
            
            # Тестирование канала
            test_response = requests.post(
                f"http://localhost:8080/api/v1/notifications/channels/{channel_id}/test",
                headers={"Authorization": "Bearer YOUR_JWT_TOKEN"}
            )
            
            if test_response.status_code == 200:
                print(f"✅ Тест канала успешен")
            else:
                print(f"❌ Тест канала неудачен: {test_response.text}")
        else:
            print(f"❌ Ошибка создания канала: {response.text}")

if __name__ == "__main__":
    test_notification_channels()
```

## Мониторинг тестов

### Логирование результатов

```python
import logging
import json
from datetime import datetime

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('notification_tests.log'),
        logging.StreamHandler()
    ]
)

def log_test_result(test_name, success, details=None):
    """Логирование результата теста"""
    result = {
        "test_name": test_name,
        "success": success,
        "timestamp": datetime.now().isoformat(),
        "details": details
    }
    
    if success:
        logging.info(f"✅ {test_name}: PASSED")
    else:
        logging.error(f"❌ {test_name}: FAILED - {details}")
    
    # Сохранение в JSON файл
    with open('test_results.json', 'a') as f:
        f.write(json.dumps(result) + '\n')

# Использование
def test_notification_system():
    try:
        # Выполнение теста
        result = send_test_notification()
        log_test_result("notification_send", result is not None, result)
    except Exception as e:
        log_test_result("notification_send", False, str(e))
```

## Автоматизация тестов

### CI/CD интеграция

```yaml
# .github/workflows/notification-tests.yml
name: Notification System Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test-notifications:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: password
          POSTGRES_DB: pingtower
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Python
      uses: actions/setup-python@v4
      with:
        python-version: '3.9'
    
    - name: Set up Java
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Install Python dependencies
      run: |
        cd bot
        pip install -r requirements.txt
    
    - name: Start Python bot
      run: |
        cd bot
        python monitoring_bot.py &
        sleep 10
    
    - name: Start Java backend
      run: |
        cd backend
        ./gradlew bootRun &
        sleep 30
    
    - name: Run notification tests
      run: |
        cd bot
        python test_notification.py
    
    - name: Run load tests
      run: |
        cd bot
        python load_test.py
```

## Рекомендации по тестированию

### Лучшие практики

1. **Всегда тестируйте в изолированной среде**
2. **Используйте тестовые данные**
3. **Проверяйте все сценарии (успех, ошибки, граничные случаи)**
4. **Логируйте результаты тестов**
5. **Автоматизируйте тестирование в CI/CD**

### Типы тестов

1. **Unit тесты** - тестирование отдельных компонентов
2. **Integration тесты** - тестирование взаимодействия компонентов
3. **End-to-end тесты** - тестирование полного потока
4. **Load тесты** - тестирование производительности
5. **Security тесты** - тестирование безопасности
