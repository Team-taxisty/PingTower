# Руководство по развертыванию PingTower

Краткое руководство по развертыванию системы мониторинга PingTower.

## Требования

- Сервер с Ubuntu 20.04+
- Установленный Docker и Docker Compose
- 4 ГБ ОЗУ, 20 ГБ дискового пространства
- Открытые порты 80, 443

## Скрипт развертывания

Скопируйте и выполните этот скрипт на сервере:

```bash
#!/bin/bash

set -e

# Установка Docker, если отсутствует
if ! command -v docker &> /dev/null; then
    sudo apt update
    sudo apt install -y docker.io docker-compose-plugin
    sudo usermod -aG docker $USER
    sudo systemctl enable docker
    sudo systemctl start docker
fi

# Создание директории проекта
sudo mkdir -p /opt/data/pingtower
sudo chown $USER:$USER /opt/data/pingtower
cd /opt/data/pingtower

# Клонирование или обновление репозитория
if [ ! -d ".git" ]; then
    git clone https://github.com/Team-taxisty/PingTower.git .
else
    git pull origin main
fi

# Создание файла .env
cat > .env << 'EOL'
POSTGRES_PASSWORD=dev123
BOT_TOKEN=7479905970:AAGRDlnda26Aw6Bfbq9HyaPWFmOQt1aAb3o
SPRING_PROFILES_ACTIVE=docker
EOL

# Развертывание
cd docker
docker compose down || true
docker compose up -d --build

# Ожидание запуска
sleep 30
docker compose ps

# Проверка
curl http://localhost/v1/actuator/health
curl http://localhost/bot/health
curl http://localhost -o /dev/null

echo "Развертывание завершено. Доступ по http://$(curl -s ifconfig.me)"
```

## Ручное развертывание

1. Установите Docker и Docker Compose
2. Клонируйте репозиторий: `git clone https://github.com/Team-taxisty/PingTower.git`
3. Отредактируйте файл .env для паролей
4. Выполните `cd docker; docker compose up -d --build`
5. Проверьте статус: `docker compose ps`

## Сервисы

- Frontend: http://server_ip
- Backend: http://server_ip:8080
- Bot API: http://server_ip:5000
- MailHog: http://server_ip:8025

## Диагностика

- Логи: `docker compose logs -f`
- Перезапуск: `docker compose restart`
- Пересборка: `docker compose up -d --build`

Подробности в README.md.
