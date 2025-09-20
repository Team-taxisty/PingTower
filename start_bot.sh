#!/bin/bash

# Переходим в директорию бота
cd ~/vscode/bot

# Активируем виртуальное окружение
source venv/bin/activate

# Запускаем бота
python monitoring_bot.py
