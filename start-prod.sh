#!/bin/bash
echo "🚀 Запуск PingTower для продакшна"

cd docker

# Остановить dev версию
docker-compose -f docker-compose.dev.yml down 2>/dev/null || true

# Запустить prod версию
docker-compose -f docker-compose.yml up -d --build

echo ""
echo "✅ Продакшн запущен!"
echo "🌐 Сайт: http://localhost"
