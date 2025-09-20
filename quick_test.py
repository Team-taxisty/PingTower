#!/usr/bin/env python3
"""
Быстрый тест системы уведомлений PingTower
Использование: python quick_test.py
"""

import requests
import json
import time
import sys

def test_python_bot():
    """Тест Python бота"""
    print("🤖 Тестирование Python бота...")
    
    url = "http://localhost:5000/send_notification"
    data = {
        "username": "testuser",
        "service_name": "Quick Test Service",
        "service_url": "https://example.com",
        "status": "down",
        "message": "🚨 Быстрый тест - сервис недоступен!"
    }
    
    try:
        response = requests.post(url, json=data, timeout=5)
        if response.status_code == 200:
            print("✅ Python бот работает!")
            return True
        else:
            print(f"❌ Python бот ошибка: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Python бот недоступен: {e}")
        return False

def test_java_backend():
    """Тест Java бэкенда"""
    print("☕ Тестирование Java бэкенда...")
    
    url = "http://localhost:8080/notifications/send"
    data = {
        "username": "testuser",
        "serviceName": "Quick Test Service via Java",
        "serviceUrl": "https://example.com",
        "status": "down",
        "severity": "ERROR",
        "message": "🚨 Быстрый тест через Java - сервис недоступен!"
    }
    
    try:
        response = requests.post(url, json=data, timeout=10)
        if response.status_code == 200:
            result = response.json()
            if result.get('success'):
                print("✅ Java бэкенд работает!")
                return True
            else:
                print(f"❌ Java бэкенд ошибка: {result.get('error')}")
                return False
        else:
            print(f"❌ Java бэкенд HTTP ошибка: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Java бэкенд недоступен: {e}")
        return False

def check_services():
    """Проверка доступности сервисов"""
    print("🔍 Проверка доступности сервисов...")
    
    services = [
        ("Python Bot", "http://localhost:5000/health"),
        ("Java Backend", "http://localhost:8080/notifications/channels")
    ]
    
    available = []
    for name, url in services:
        try:
            response = requests.get(url, timeout=3)
            if response.status_code in [200, 201]:
                print(f"✅ {name} доступен")
                available.append(name)
            else:
                print(f"❌ {name} недоступен (HTTP {response.status_code})")
        except:
            print(f"❌ {name} недоступен")
    
    return available

def main():
    print("🧪 Быстрый тест системы уведомлений PingTower")
    print("=" * 50)
    
    # Проверка сервисов
    available = check_services()
    print()
    
    if not available:
        print("❌ Ни один сервис не доступен!")
        print("💡 Убедитесь, что:")
        print("   - Python бот запущен: python bot/monitoring_bot.py")
        print("   - Java бэкенд запущен: ./gradlew bootRun")
        sys.exit(1)
    
    # Тестирование уведомлений
    python_ok = False
    java_ok = False
    
    if "Python Bot" in available:
        python_ok = test_python_bot()
        print()
    
    if "Java Backend" in available:
        java_ok = test_java_backend()
        print()
    
    # Результаты
    print("📊 Результаты тестирования:")
    print(f"   Python Bot: {'✅' if python_ok else '❌'}")
    print(f"   Java Backend: {'✅' if java_ok else '❌'}")
    
    if python_ok or java_ok:
        print("\n🎉 Система уведомлений работает!")
        print("📱 Проверьте Telegram: https://t.me/PingTower_tax_bot")
    else:
        print("\n❌ Система уведомлений не работает")
        print("💡 Проверьте логи и конфигурацию")

if __name__ == "__main__":
    main()
