#!/bin/bash
echo "Запуск PingTower для разработки"
echo "Frontend с hot reload - изменения сразу видны!"

cd docker

# Остановить prod версию если запущена
docker-compose -f docker-compose.yml down 2>/dev/null || true

# Запустить dev версию
docker-compose -f docker-compose.dev.yml up -d --build

echo ""
echo "Теперь можно редактировать frontend код:"
echo ""
echo "Сайт: http://localhost (изменения сразу видны!)"
echo "Backend API: http://localhost/v1/actuator/health"
echo "MailHog: http://localhost:8025"
echo ""
echo "Логи: docker-compose -f docker/docker-compose.dev.yml logs -f frontend"
echo "Остановить: docker-compose -f docker/docker-compose.dev.yml down"
