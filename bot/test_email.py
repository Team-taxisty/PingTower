import requests
import json

def test_email_notification():
    """Тест отправки уведомления с email"""
    
    url = "http://localhost:5000/send_notification"
    
    # Тестовые данные с email
    test_data = {
        "username": "testuser",  # Замените на ваш зарегистрированный логин
        "service_name": "Test Web Server",
        "service_url": "https://example.com",
        "status": "down", 
        "message": "Тестовое уведомление с email - сервер не отвечает",
        "email": "test@example.com"  # Замените на ваш email
    }
    
    try:
        print("🔄 Отправка тестового уведомления с email...")
        response = requests.post(url, json=test_data, timeout=5)
        
        if response.status_code == 200:
            print("✅ Уведомление отправлено успешно!")
            print(f"📱 Проверьте Telegram: https://t.me/PingTower_tax_bot")
            print(f"📧 Проверьте email: {test_data['email']}")
        else:
            print(f"❌ Ошибка: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ Ошибка подключения. Убедитесь, что бот запущен.")
    except Exception as e:
        print(f"❌ Ошибка: {e}")

def test_email_only():
    """Тест только email уведомления"""
    from email_notifier import EmailNotifier
    
    print("📧 Тестирование только email уведомления...")
    
    notifier = EmailNotifier()
    success = notifier.send_notification(
        to_email="test@example.com",  # Замените на ваш email
        username="testuser",
        service_name="Email Test Service",
        service_url="https://example.com",
        status="down",
        message="Тестовое email уведомление - сервер не отвечает"
    )
    
    if success:
        print("✅ Email уведомление работает!")
    else:
        print("❌ Email уведомление не работает. Проверьте настройки в .env файле.")

def test_telegram_only():
    """Тест только Telegram уведомления"""
    
    url = "http://localhost:5000/send_notification"
    
    # Тестовые данные БЕЗ email
    test_data = {
        "username": "testuser",  # Замените на ваш зарегистрированный логин
        "service_name": "Telegram Test Service",
        "service_url": "https://example.com",
        "status": "down", 
        "message": "Тестовое уведомление только в Telegram - сервер не отвечает"
        # НЕ добавляем поле "email"
    }
    
    try:
        print("📱 Тестирование только Telegram уведомления...")
        response = requests.post(url, json=test_data, timeout=5)
        
        if response.status_code == 200:
            print("✅ Telegram уведомление отправлено успешно!")
            print(f"📱 Проверьте Telegram: https://t.me/PingTower_tax_bot")
        else:
            print(f"❌ Ошибка: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ Ошибка подключения. Убедитесь, что бот запущен.")
    except Exception as e:
        print(f"❌ Ошибка: {e}")

if __name__ == "__main__":
    print("🧪 Тестирование уведомлений PingTower")
    print("-" * 40)
    
    print("Выберите способ тестирования:")
    print("1️⃣ Уведомление в Telegram + Email")
    print("2️⃣ Только Email уведомление")
    print("3️⃣ Только Telegram уведомление")
    
    choice = input("\nВведите номер (1-3): ").strip()
    
    if choice == "1":
        print("\n🔍 Тестирование Telegram + Email...")
        test_email_notification()
    elif choice == "2":
        print("\n🔍 Тестирование только Email...")
        test_email_only()
    elif choice == "3":
        print("\n🔍 Тестирование только Telegram...")
        test_telegram_only()
    else:
        print("❌ Неверный выбор. Запустите скрипт снова.")
