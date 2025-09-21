
```bash
# ЛОКАЛЬНО: Создать краткий и понятный DEPLOYMENT.md
cat > DEPLOYMENT.md << 'EOF'
# PingTower Deployment Guide

Quick deployment script for PingTower monitoring system.

## Requirements

- Ubuntu 20.04+ server
- Docker and Docker Compose
- 4GB RAM, 20GB storage
- Open ports 80, 443

## One-Script Deployment

Copy and run this script on your server:

```
#!/bin/bash

# PingTower Auto Deploy Script
set -e

echo "Starting PingTower deployment..."

# Install Docker if not present
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    sudo apt update
    sudo apt install -y docker.io docker-compose-plugin
    sudo usermod -aG docker $USER
    sudo systemctl enable docker
    sudo systemctl start docker
fi

# Create project directory
sudo mkdir -p /opt/data/pingtower
sudo chown $USER:$USER /opt/data/pingtower
cd /opt/data/pingtower

# Clone repository
if [ ! -d ".git" ]; then
    git clone https://github.com/Team-taxisty/PingTower.git .
fi

# Update code
git pull origin main

# Create environment file
cat > .env << 'EOL'
POSTGRES_DB=monitoring
POSTGRES_USER=monitoring
POSTGRES_PASSWORD=secure_password_123
BOT_TOKEN=7479905970:AAGRDlnda26Aw6Bfbq9HyaPWFmOQt1aAb3o
SPRING_PROFILES_ACTIVE=production
EOL

# Deploy services
cd docker
docker compose down || true
docker compose up -d --build

# Wait for services to start
echo "Waiting for services to start..."
sleep 60

# Health checks
echo "Checking services..."
curl -f http://localhost/v1/actuator/health || echo "Backend check failed"
curl -f http://localhost/bot/health || echo "Bot check failed"
curl -f http://localhost/ -o /dev/null || echo "Frontend check failed"

# Show status
docker compose ps

echo "Deployment completed!"
echo "Access URLs:"
echo "  Frontend: http://$(curl -s ifconfig.me)"
echo "  Backend:  http://$(curl -s ifconfig.me)/v1/actuator/health"
echo "  Bot API:  http://$(curl -s ifconfig.me)/bot/health"
echo "  MailHog:  http://$(curl -s ifconfig.me):8025"
echo ""
echo "Telegram Bot: @PingTower_tax_bot"
echo "Send any message to bot to subscribe to notifications"
```

## Manual Commands

If you prefer step-by-step:

```
# Install Docker
sudo apt update && sudo apt install -y docker.io docker-compose-plugin

# Clone and deploy
git clone https://github.com/Team-taxisty/PingTower.git
cd PingTower/docker
docker compose up -d --build

# Check status
docker compose ps
```

## Environment Variables

Edit `.env` file before deployment:

```
POSTGRES_PASSWORD=your_secure_password
BOT_TOKEN=your_telegram_bot_token
SPRING_PROFILES_ACTIVE=production
```

## Services Overview

After deployment, these services will be running:

- Frontend (React): Port 80
- Backend (Spring Boot): Port 8080
- Bot API (Python Flask): Port 5000
- PostgreSQL: Port 5432
- ClickHouse: Port 8123
- MailHog: Port 8025
- Nginx: Load balancer

## Telegram Bot Setup

1. Message @PingTower_tax_bot
2. Auto-subscribe to notifications
3. Receive deployment alerts

## Troubleshooting

```
# View logs
docker compose logs -f

# Restart services
docker compose restart

# Check service health
curl http://localhost/v1/actuator/health
curl http://localhost/bot/health

# Update deployment
git pull origin main
docker compose up -d --build
```

## Maintenance

```
# Update system
cd /opt/data/pingtower
git pull origin main
docker compose down
docker compose up -d --build

# Backup database
docker compose exec postgres pg_dump -U monitoring monitoring > backup.sql

# View resource usage
docker stats
```

## Production Notes

- Change default passwords in .env
- Configure firewall (ports 80, 443)
- Set up SSL certificate with certbot
- Monitor disk space and logs
- Regular database backups

For advanced deployment options, see GitHub repository documentation.
EOF
```

Этот DEPLOYMENT.md содержит:

1. **Один основной скрипт** - можно скопировать и запустить целиком
2. **Краткие инструкции** - без лишней информации
3. **Практические команды** - готовые к использованию
4. **Troubleshooting секция** - для решения проблем
5. **Без эмодзи** - чистый технический текст

Скрипт автоматически:
- Устанавливает Docker если нужно
- Клонирует репозиторий
- Создает конфигурацию
- Запускает все сервисы
- Проверяет их работу
- Показывает URL для доступа

Можно просто скопировать и запустить на любом Ubuntu сервере.

[1](https://docs.docker.com/reference/compose-file/deploy/)
[2](https://forums.docker.com/t/use-script-in-docker-compose/137728)
[3](https://github.com/docker/awesome-compose)
[4](https://gitlab.com/to-be-continuous/docker-compose)
[5](https://www.deployhq.com/blog/what-is-a-deployment-script)
[6](https://docs.rapidminer.com/9.7/deployment/docker-compose)
[7](https://stackoverflow.com/questions/64283027/docker-build-deploy-using-bash-script)
[8](https://abp.io/docs/commercial/8.1/startup-templates/application/deployment-docker-compose)
[9](https://www.reddit.com/r/docker/comments/1khcdsz/automate_dockercompose_deployments/)
