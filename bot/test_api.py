import requests
import json

def test_api():
    # Проверка здоровья API
    try:
        response = requests.get("http://localhost:5000/health", timeout=5)
        if response.status_code == 200:
            print("✅ API работает:")
            print(json.dumps(response.json(), indent=2, ensure_ascii=False))
        else:
            print(f"❌ API ошибка: {response.status_code}")
    except Exception as e:
        print(f"❌ Ошибка подключения к API: {e}")
        return False
    
    # Тест отправки уведомления (сначала зарегистрируйтесь в боте!)
    username = input("\nВведите ваш логин из регистрации в боте (или Enter для пропуска): ")
    
    if username.strip():
        test_data = {
            "username": username.strip(),
            "service_name": "Test Service",
            "service_url": "https://example.com",
            "status": "down",
            "message": "Тестовое уведомление от API"
        }
        
        try:
            response = requests.post(
                "http://localhost:5000/send_notification", 
                json=test_data, 
                timeout=5
            )
            
            if response.status_code == 200:
                print("✅ Уведомление отправлено успешно!")
                print("📱 Проверьте Telegram бота")
            else:
                print(f"❌ Ошибка отправки: {response.status_code}")
                print(f"Детали: {response.text}")
        except Exception as e:
            print(f"❌ Ошибка: {e}")

if __name__ == "__main__":
    test_api()

