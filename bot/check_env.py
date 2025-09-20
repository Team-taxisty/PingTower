#!/usr/bin/env python3
"""
Проверка переменных окружения
"""

import os
from dotenv import load_dotenv

print("🔍 Проверка переменных окружения")
print("-" * 40)

# Загружаем .env файл
load_dotenv()

# Проверяем переменные
variables = [
    'BOT_TOKEN',
    'SMTP_HOST', 
    'SMTP_PORT',
    'EMAIL_USERNAME',
    'EMAIL_PASSWORD',
    'FROM_EMAIL'
]

for var in variables:
    value = os.getenv(var)
    if value:
        # Скрываем пароль
        if 'PASSWORD' in var:
            display_value = '*' * len(value) if value else 'НЕ УСТАНОВЛЕН'
        else:
            display_value = value
        print(f"✅ {var}: {display_value}")
    else:
        print(f"❌ {var}: НЕ УСТАНОВЛЕН")

print("\n📁 Проверка файла .env:")
if os.path.exists('.env'):
    print("✅ Файл .env существует")
    with open('.env', 'r', encoding='utf-8') as f:
        content = f.read()
        print(f"📄 Размер файла: {len(content)} символов")
        print("📄 Содержимое:")
        print(content)
else:
    print("❌ Файл .env НЕ НАЙДЕН")
