import requests
import json
import time

def test_java_backend_notification():
    """Тест отправки уведомления через Java бэкенд"""
    
    # URL Java бэкенда (предполагаем, что он работает на порту 8080)
    url = "http://localhost:8080/notifications/send"
    
    # Тестовые данные
    test_data = {
        "username": "testuser",  # Замените на ваш зарегистрированный логин
        "serviceName": "Test Web Server",
        "serviceUrl": "https://example.com",
        "status": "down", 
        "severity": "ERROR",
        "message": "Тестовое уведомление через Java бэкенд - сервер не отвечает"
    }
    
    try:
        print("🔄 Отправка тестового уведомления через Java бэкенд...")
        response = requests.post(url, json=test_data, timeout=10)
        
        if response.status_code == 200:
            result = response.json()
            if result.get('success'):
                print("✅ Уведомление отправлено успешно!")
                print(f"📱 Проверьте Telegram: https://t.me/PingTower_tax_bot")
                print(f"📊 Delivery ID: {result.get('delivery_id')}")
            else:
                print(f"❌ Ошибка: {result.get('error')}")
        else:
            print(f"❌ HTTP Error {response.status_code}: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ Ошибка подключения. Убедитесь, что Java бэкенд запущен на порту 8080.")
    except Exception as e:
        print(f"❌ Ошибка: {e}")

def test_java_backend_health():
    """Проверка здоровья Java бэкенда"""
    try:
        # Попробуем несколько возможных endpoints
        endpoints = [
            "http://localhost:8080/actuator/health",
            "http://localhost:8080/notifications/channels",
            "http://localhost:8080/api/health"
        ]
        
        for endpoint in endpoints:
            try:
                response = requests.get(endpoint, timeout=5)
                if response.status_code == 200:
                    print(f"✅ Java бэкенд работает корректно")
                    print(f"📊 Endpoint: {endpoint}")
                    print(f"📊 Статус: {response.json()}")
                    return True
            except:
                continue
                
        print("❌ Java бэкенд недоступен")
        return False
        
    except Exception as e:
        print(f"❌ Ошибка проверки: {e}")
        return False

def test_create_notification_channel():
    """Создание канала уведомлений для Python бота"""
    
    url = "http://localhost:8080/notifications/channels"
    
    # Конфигурация для Python бота
    channel_data = {
        "type": "PYTHON_BOT",
        "name": "Python Bot Channel",
        "configuration": json.dumps({
            "botUrl": "http://localhost:5000"
        }),
        "isDefault": True
    }
    
    try:
        print("🔄 Создание канала уведомлений для Python бота...")
        response = requests.post(url, json=channel_data, timeout=10)
        
        if response.status_code == 201:
            print("✅ Канал уведомлений создан успешно!")
            print(f"📊 Канал: {response.json()}")
        else:
            print(f"❌ HTTP Error {response.status_code}: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ Ошибка подключения. Убедитесь, что Java бэкенд запущен.")
    except Exception as e:
        print(f"❌ Ошибка: {e}")

if __name__ == "__main__":
    print("🧪 Тестирование Java Backend Notifications")
    print("-" * 40)
    
    # Проверка здоровья
    print("1️⃣ Проверка здоровья Java бэкенда...")
    health_ok = test_java_backend_health()
    print()
    
    if health_ok:
        # Создание канала
        print("2️⃣ Создание канала уведомлений...")
        test_create_notification_channel()
        print()
        
        # Тест уведомления
        print("3️⃣ Тест отправки уведомления...")
        test_java_backend_notification()
    else:
        print("❌ Java бэкенд недоступен. Запустите его перед тестированием.")
