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
    print("🧪 Тестирование PingTower Bot")
    print("-" * 30)
    
    # Проверка здоровья
    test_health()
    print()
    
    # Тест уведомления
    test_notification()
