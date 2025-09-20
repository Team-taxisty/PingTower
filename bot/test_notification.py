import requests
import json
import time

def test_notification():
    """Тест отправки уведомления в бот"""
    
    url = "http://localhost:5000/send_notification"
    
    # Тестовые данные
    test_data = {
        "username": "testuser",  # Замените на ваш зарегистрированный логин
        "service_name": "Test Web Server",
        "service_url": "https://example.com",
        "status": "down", 
        "message": "Тестовое уведомление - сервер не отвечает"
    }
    
    try:
        print("🔄 Отправка тестового уведомления...")
        response = requests.post(url, json=test_data, timeout=5)
        
        if response.status_code == 200:
            print("✅ Уведомление отправлено успешно!")
            print(f"📱 Проверьте Telegram: https://t.me/PingTower_tax_bot")
        else:
            print(f"❌ Ошибка: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ Ошибка подключения. Убедитесь, что бот запущен.")
    except Exception as e:
        print(f"❌ Ошибка: {e}")

def test_notification_via_java():
    """Тест отправки уведомления через Java бэкенд"""
    
    url = "http://localhost:8080/notifications/send"
    
    # Тестовые данные для Java бэкенда
    test_data = {
        "username": "testuser",  # Замените на ваш зарегистрированный логин
        "serviceName": "Test Web Server via Java",
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
                print("✅ Уведомление отправлено успешно через Java!")
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

def test_health():
    """Проверка здоровья бота"""
    try:
        response = requests.get("http://localhost:5000/health", timeout=5)
        if response.status_code == 200:
            print("✅ Бот работает корректно")
            print(f"📊 Статус: {response.json()}")
        else:
            print("❌ Бот недоступен")
    except Exception as e:
        print(f"❌ Ошибка проверки: {e}")

if __name__ == "__main__":
    print("🧪 Тестирование PingTower Notifications")
    print("-" * 40)
    
    print("Выберите способ тестирования:")
    print("1️⃣ Прямое обращение к Python боту")
    print("2️⃣ Через Java бэкенд")
    print("3️⃣ Оба способа")
    print("4️⃣ Только Telegram уведомление")
    
    choice = input("\nВведите номер (1-4): ").strip()
    
    if choice == "1":
        print("\n🔍 Тестирование через Python бот...")
        test_health()
        print()
        test_notification()
    elif choice == "2":
        print("\n🔍 Тестирование через Java бэкенд...")
        test_notification_via_java()
    elif choice == "3":
        print("\n🔍 Тестирование обоих способов...")
        print("\n--- Python Bot ---")
        test_health()
        print()
        test_notification()
        print("\n--- Java Backend ---")
        test_notification_via_java()
    elif choice == "4":
        print("\n🔍 Тестирование только Telegram...")
        test_health()
        print()
        test_notification()
    else:
        print("❌ Неверный выбор. Запустите скрипт снова.")
