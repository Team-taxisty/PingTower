# CI/CD Pipeline для PingTower

## Описание

Простой CI/CD pipeline для автоматизации тестирования, сборки и деплоя PingTower.

## Workflows

### 1. ci-cd.yml
- **Триггер**: Push в main/docker, Pull Request в main
- **Задачи**:
  - Тестирование Backend (Java/Spring Boot)
  - Тестирование Frontend (React)
  - Сборка Docker образов
  - Деплой в staging (только для docker ветки)

### 2. code-quality.yml
- **Триггер**: Push в main/docker, Pull Request в main
- **Задачи**:
  - Проверка качества кода Backend
  - Проверка качества кода Frontend
  - Генерация отчетов о покрытии тестами

## Использование

### Локальная разработка
```bash
# Запуск в dev режиме
cd docker
docker-compose -f docker-compose.dev.yml up -d
```

### Production деплой
```bash
# Копирование .env файла
cp .env.example .env
# Редактирование переменных окружения
nano .env

# Запуск в production режиме
docker-compose -f docker-compose.prod.yml up -d
```

## Переменные окружения

Создайте файл `.env` на основе `.env.example` и укажите:
- Пароли для баз данных
- Настройки Spring Boot профиля

## Мониторинг

- Backend: http://localhost:8080/actuator/health
- Frontend: http://localhost:80
- MailHog: http://localhost:8025 (только dev)
