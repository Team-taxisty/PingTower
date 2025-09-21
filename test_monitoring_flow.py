#!/usr/bin/env python3
"""
Тестовый скрипт для проверки полного цикла мониторинга PingTower.
Проверяет создание пользователя, сервиса, запуск мониторинга и получение результатов.
"""

import requests
import json
import time
import sys
from datetime import datetime

# Конфигурация
BASE_URL = "http://localhost:8080/v1"
TEST_USER = {
    "username": "test_monitor_user",
    "email": "test_monitor@example.com", 
    "password": "password123"
}
TEST_SERVICE = {
    "name": "Test HTTP Service",
    "description": "Test service for monitoring flow",
    "url": "https://httpbin.org/status/200",
    "serviceType": "PING",
    "enabled": True,
    "checkIntervalMinutes": 1,  # Каждую минуту для теста
    "timeoutSeconds": 30
}

class PingTowerTester:
    def __init__(self):
        self.token = None
        self.service_id = None
        self.session = requests.Session()
        
    def log(self, message):
        print(f"[{datetime.now()}] {message}")
        
    def register_user(self):
        """Регистрация тестового пользователя"""
        self.log("1. Регистрируем тестового пользователя...")
        
        try:
            response = self.session.post(
                f"{BASE_URL}/api/auth/register",
                json=TEST_USER,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                self.token = data["token"]
                self.log(f"✓ Пользователь создан успешно! ID: {data['id']}")
                self.log(f"✓ Токен получен: {self.token[:20]}...")
                return True
            else:
                self.log(f"✗ Ошибка регистрации: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            self.log(f"✗ Исключение при регистрации: {e}")
            return False
            
    def create_service(self):
        """Создание сервиса для мониторинга"""
        self.log("2. Создаем сервис для мониторинга...")
        
        try:
            headers = {
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json"
            }
            
            response = self.session.post(
                f"{BASE_URL}/api/services",
                json=TEST_SERVICE,
                headers=headers
            )
            
            if response.status_code == 201:
                data = response.json()
                self.service_id = data["id"]
                self.log(f"✓ Сервис создан успешно! ID: {self.service_id}")
                self.log(f"✓ URL: {data['url']}")
                self.log(f"✓ Включен: {data['enabled']}")
                return True
            else:
                self.log(f"✗ Ошибка создания сервиса: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            self.log(f"✗ Исключение при создании сервиса: {e}")
            return False
            
    def trigger_immediate_check(self):
        """Запуск немедленной проверки"""
        self.log("3. Запускаем немедленную проверку сервиса...")
        
        try:
            headers = {
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json"
            }
            
            response = self.session.post(
                f"{BASE_URL}/api/services/{self.service_id}/test",
                headers=headers
            )
            
            if response.status_code == 200:
                self.log("✓ Немедленная проверка запущена!")
                return True
            else:
                self.log(f"✗ Ошибка запуска проверки: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            self.log(f"✗ Исключение при запуске проверки: {e}")
            return False
            
    def check_monitoring_results(self, max_attempts=10):
        """Проверка результатов мониторинга"""
        self.log("4. Проверяем результаты мониторинга...")
        
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        
        for attempt in range(max_attempts):
            try:
                self.log(f"   Попытка {attempt + 1}/{max_attempts}")
                
                # Проверяем результаты для конкретного сервиса
                response = self.session.get(
                    f"{BASE_URL}/api/monitoring/services/{self.service_id}/results",
                    headers=headers,
                    params={"size": 10}
                )
                
                self.log(f"   Ответ API: {response.status_code}")
                
                if response.status_code == 200:
                    data = response.json()
                    self.log(f"   Данные получены: {json.dumps(data, indent=2)}")
                    
                    if data.get("content") and len(data["content"]) > 0:
                        results = data["content"]
                        self.log(f"✓ Найдено {len(results)} результатов мониторинга!")
                        
                        for i, result in enumerate(results[:3]):  # Показываем первые 3
                            self.log(f"   Результат {i+1}:")
                            self.log(f"     Время: {result.get('checkedAt', result.get('checkTime'))}")
                            self.log(f"     Успешно: {result.get('successful', result.get('isSuccessful'))}")
                            self.log(f"     Код ответа: {result.get('responseCode')}")
                            self.log(f"     Время отклика: {result.get('responseTimeMs')}мс")
                            
                        return True
                    else:
                        self.log(f"   Результатов пока нет, ждем...")
                        
                elif response.status_code == 404:
                    self.log(f"   Сервис не найден или нет данных")
                else:
                    self.log(f"   Ошибка API: {response.status_code} - {response.text}")
                    
            except Exception as e:
                self.log(f"   Исключение: {e}")
                
            if attempt < max_attempts - 1:
                time.sleep(10)  # Ждем 10 секунд перед следующей попыткой
                
        self.log("✗ Не удалось получить результаты мониторинга за отведенное время")
        return False
        
    def check_system_health(self):
        """Проверка общего здоровья системы"""
        self.log("5. Проверяем общее состояние системы...")
        
        try:
            headers = {
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json"
            }
            
            response = self.session.get(
                f"{BASE_URL}/api/monitoring/health",
                headers=headers
            )
            
            if response.status_code == 200:
                data = response.json()
                self.log(f"✓ Состояние системы:")
                self.log(f"   Всего сервисов: {data.get('totalServices')}")
                self.log(f"   Активных сервисов: {data.get('activeServices')}")
                self.log(f"   Недавних проверок: {data.get('recentChecks')}")
                self.log(f"   Недавних ошибок: {data.get('recentFailures')}")
                self.log(f"   Успешность: {data.get('successRate', 0):.2f}%")
                return True
            else:
                self.log(f"✗ Ошибка получения состояния: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            self.log(f"✗ Исключение при проверке состояния: {e}")
            return False
            
    def run_test(self):
        """Запуск полного теста"""
        self.log("=== ЗАПУСК ТЕСТА МОНИТОРИНГА PINGTOWER ===")
        
        # 1. Регистрация пользователя
        if not self.register_user():
            self.log("ТЕСТ ПРОВАЛЕН: Не удалось зарегистрировать пользователя")
            return False
            
        # 2. Создание сервиса
        if not self.create_service():
            self.log("ТЕСТ ПРОВАЛЕН: Не удалось создать сервис")
            return False
            
        # 3. Запуск проверки
        if not self.trigger_immediate_check():
            self.log("ТЕСТ ПРОВАЛЕН: Не удалось запустить проверку")
            return False
            
        # 4. Ждем и проверяем результаты
        self.log("Ждем несколько секунд для обработки...")
        time.sleep(5)
        
        if not self.check_monitoring_results():
            self.log("ТЕСТ ПРОВАЛЕН: Не получены результаты мониторинга")
            return False
            
        # 5. Проверяем общее состояние
        self.check_system_health()
        
        self.log("=== ТЕСТ ЗАВЕРШЕН УСПЕШНО ===")
        return True

if __name__ == "__main__":
    tester = PingTowerTester()
    
    try:
        success = tester.run_test()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nТест прерван пользователем")
        sys.exit(1)
    except Exception as e:
        print(f"\n\nНепредвиденная ошибка: {e}")
        sys.exit(1)