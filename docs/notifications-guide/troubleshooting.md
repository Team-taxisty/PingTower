# 🐛 Устранение неполадок

## Обзор

Данный раздел содержит решения наиболее распространенных проблем системы уведомлений PingTower.

## Проблемы с Python ботом

### Проблема: Бот не отвечает

**Симптомы:**
- HTTP 500 ошибка при обращении к боту
- Таймауты при отправке уведомлений
- Бот не отвечает в Telegram

**Диагностика:**
```bash
# Проверка статуса бота
curl http://localhost:5000/health

# Проверка логов
tail -f bot/monitoring_bot.log

# Проверка процессов
ps aux | grep python
```

**Решения:**

1. **Проверьте токен бота в `.env` файле:**
```env
TELEGRAM_BOT_TOKEN=your_bot_token_here
```

2. **Убедитесь, что пользователь зарегистрирован в боте:**
```bash
curl http://localhost:5000/users
```

3. **Перезапустите бота:**
```bash
cd bot
pkill -f monitoring_bot.py
python monitoring_bot.py
```

4. **Проверьте подключение к базе данных:**
```bash
cd bot
python -c "import sqlite3; conn = sqlite3.connect('monitoring_bot.db'); print('DB OK')"
```

### Проблема: Ошибки базы данных

**Симптомы:**
- Ошибки SQLite в логах
- Бот не может сохранить данные пользователей

**Решения:**

1. **Проверьте права доступа к файлу БД:**
```bash
ls -la bot/monitoring_bot.db
chmod 664 bot/monitoring_bot.db
```

2. **Пересоздайте базу данных:**
```bash
cd bot
rm monitoring_bot.db
python -c "
import sqlite3
conn = sqlite3.connect('monitoring_bot.db')
conn.execute('CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT UNIQUE, password TEXT, created_at TIMESTAMP)')
conn.execute('CREATE TABLE notifications (id INTEGER PRIMARY KEY, username TEXT, message TEXT, sent_at TIMESTAMP)')
conn.commit()
conn.close()
"
```

3. **Проверьте свободное место на диске:**
```bash
df -h
```

### Проблема: Telegram API ошибки

**Симптомы:**
- Ошибки 401 Unauthorized
- Ошибки 403 Forbidden
- Бот заблокирован пользователем

**Решения:**

1. **Проверьте токен бота:**
```bash
curl "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getMe"
```

2. **Проверьте, что бот не заблокирован:**
```bash
curl "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates"
```

3. **Обновите токен бота в `.env`:**
```env
TELEGRAM_BOT_TOKEN=new_bot_token_here
```

## Проблемы с Java бэкендом

### Проблема: Java бэкенд не отправляет уведомления

**Симптомы:**
- HTTP 500 ошибки при отправке уведомлений
- Уведомления не доходят до пользователей
- Ошибки в логах Java приложения

**Диагностика:**
```bash
# Проверка статуса приложения
curl http://localhost:8080/actuator/health

# Проверка логов
tail -f backend/logs/application.log

# Проверка каналов уведомлений
curl http://localhost:8080/api/v1/notifications/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Решения:**

1. **Проверьте, что канал уведомлений создан:**
```bash
curl http://localhost:8080/api/v1/notifications/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

2. **Убедитесь, что Python бот доступен с Java бэкенда:**
```bash
# С Java бэкенда
curl http://localhost:5000/health
```

3. **Проверьте конфигурацию в `application.yaml`:**
```yaml
notifications:
  python-bot:
    url: http://localhost:5000
  channels:
    default-type: PYTHON_BOT
```

4. **Проверьте логи Java приложения:**
```bash
grep -i "error\|exception" backend/logs/application.log
```

### Проблема: Ошибки подключения к базе данных

**Симптомы:**
- Ошибки подключения к PostgreSQL
- HTTP 500 ошибки при обращении к API
- Ошибки в логах Spring Boot

**Решения:**

1. **Проверьте статус PostgreSQL:**
```bash
# Docker
docker ps | grep postgres

# Systemd
systemctl status postgresql
```

2. **Проверьте подключение к БД:**
```bash
psql -h localhost -U pingtower -d pingtower -c "SELECT 1;"
```

3. **Проверьте конфигурацию БД в `application.yaml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pingtower
    username: pingtower
    password: password
```

4. **Перезапустите Java приложение:**
```bash
cd backend
./gradlew bootRun
```

### Проблема: JWT токены не работают

**Симптомы:**
- HTTP 401 Unauthorized ошибки
- Пользователи не могут авторизоваться
- Токены истекают слишком быстро

**Решения:**

1. **Проверьте конфигурацию JWT:**
```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 часа
```

2. **Проверьте токен в браузере:**
```javascript
console.log(localStorage.getItem('jwtToken'));
```

3. **Обновите токен:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "your_username", "password": "your_password"}'
```

## Проблемы с доставкой уведомлений

### Проблема: Уведомления не доходят в Telegram

**Симптомы:**
- Уведомления отправляются успешно (статус 200)
- Пользователи не получают сообщения в Telegram
- Ошибки в логах Python бота

**Диагностика:**
```bash
# Проверка регистрации пользователя
curl http://localhost:5000/users

# Проверка последних уведомлений
curl http://localhost:5000/notifications

# Проверка статуса бота
curl "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getMe"
```

**Решения:**

1. **Убедитесь, что пользователь зарегистрирован в боте:**
```bash
curl -X POST http://localhost:5000/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "testpass"}'
```

2. **Проверьте, что бот не заблокирован пользователем:**
   - Попросите пользователя отправить `/start` боту
   - Проверьте, что бот отвечает

3. **Проверьте правильность username в запросе:**
```bash
curl -X POST http://localhost:5000/send_notification \
  -H "Content-Type: application/json" \
  -d '{
    "username": "correct_username",
    "service_name": "Test Service",
    "service_url": "https://example.com",
    "status": "down",
    "message": "Test message"
  }'
```

4. **Проверьте логи Python бота:**
```bash
tail -f bot/monitoring_bot.log | grep -i "telegram\|error"
```

### Проблема: Email уведомления не доходят

**Симптомы:**
- Email канал настроен правильно
- Уведомления отправляются без ошибок
- Письма не приходят в почтовый ящик

**Решения:**

1. **Проверьте SMTP настройки:**
```json
{
  "smtpHost": "smtp.gmail.com",
  "smtpPort": 587,
  "username": "your-email@gmail.com",
  "password": "your-app-password",
  "useTls": true
}
```

2. **Проверьте App Password для Gmail:**
   - Включите 2FA в Google аккаунте
   - Создайте App Password
   - Используйте App Password вместо обычного пароля

3. **Проверьте папку "Спам":**
   - Письма могут попадать в спам
   - Добавьте отправителя в белый список

4. **Проверьте логи Java приложения:**
```bash
grep -i "email\|smtp" backend/logs/application.log
```

### Проблема: Slack уведомления не доходят

**Симптомы:**
- Slack webhook настроен правильно
- Уведомления отправляются без ошибок
- Сообщения не появляются в Slack

**Решения:**

1. **Проверьте webhook URL:**
```bash
curl -X POST "https://hooks.slack.com/services/YOUR/WEBHOOK/URL" \
  -H "Content-Type: application/json" \
  -d '{"text": "Test message"}'
```

2. **Проверьте права бота в Slack:**
   - Убедитесь, что бот добавлен в канал
   - Проверьте права на отправку сообщений

3. **Проверьте формат сообщения:**
```json
{
  "text": "Service is down",
  "channel": "#alerts",
  "username": "PingTower Bot"
}
```

## Проблемы с производительностью

### Проблема: Медленная доставка уведомлений

**Симптомы:**
- Уведомления доставляются с задержкой
- Высокое время отклика API
- Таймауты при отправке

**Решения:**

1. **Проверьте нагрузку на систему:**
```bash
# CPU и память
top
htop

# Диск
df -h
iostat

# Сеть
netstat -i
```

2. **Оптимизируйте настройки базы данных:**
```sql
-- PostgreSQL
VACUUM ANALYZE;
REINDEX DATABASE pingtower;
```

3. **Увеличьте таймауты:**
```yaml
notifications:
  timeout: 30  # секунд
  retry-attempts: 3
  retry-delay: 1000  # миллисекунд
```

4. **Используйте connection pooling:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### Проблема: Высокое потребление памяти

**Симптомы:**
- Java приложение потребляет много памяти
- OutOfMemoryError в логах
- Медленная работа системы

**Решения:**

1. **Увеличьте heap size:**
```bash
export JAVA_OPTS="-Xmx2g -Xms1g"
./gradlew bootRun
```

2. **Настройте garbage collection:**
```bash
export JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

3. **Проверьте утечки памяти:**
```bash
# Мониторинг памяти
jstat -gc <pid> 1s

# Дамп памяти при проблемах
jmap -dump:format=b,file=heap.hprof <pid>
```

## Проблемы с безопасностью

### Проблема: Неавторизованный доступ

**Симптомы:**
- HTTP 401 ошибки
- Пользователи не могут войти в систему
- Токены не работают

**Решения:**

1. **Проверьте JWT конфигурацию:**
```yaml
jwt:
  secret: strong-secret-key-here
  expiration: 86400000
```

2. **Проверьте CORS настройки:**
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

3. **Проверьте SSL/TLS настройки:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
```

## Диагностические команды

### Проверка всех компонентов

```bash
#!/bin/bash
# check_system.sh

echo "🔍 Проверка системы уведомлений PingTower"
echo "========================================"

# Python Bot
echo "📱 Python Bot:"
curl -s http://localhost:5000/health | jq '.' || echo "❌ Python Bot недоступен"

# Java Backend
echo "☕ Java Backend:"
curl -s http://localhost:8080/actuator/health | jq '.' || echo "❌ Java Backend недоступен"

# PostgreSQL
echo "🐘 PostgreSQL:"
pg_isready -h localhost -p 5432 || echo "❌ PostgreSQL недоступен"

# Каналы уведомлений
echo "📡 Каналы уведомлений:"
curl -s http://localhost:8080/api/v1/notifications/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" | jq '.' || echo "❌ Не удалось получить каналы"

# Статистика доставки
echo "📊 Статистика доставки:"
curl -s http://localhost:8080/api/v1/notifications/deliveries/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" | jq '.' || echo "❌ Не удалось получить статистику"

echo "✅ Проверка завершена"
```

### Мониторинг логов

```bash
#!/bin/bash
# monitor_logs.sh

echo "📋 Мониторинг логов системы уведомлений"
echo "======================================"

# Python Bot логи
echo "📱 Python Bot логи:"
tail -n 20 bot/monitoring_bot.log

echo ""

# Java Backend логи
echo "☕ Java Backend логи:"
tail -n 20 backend/logs/application.log

echo ""

# PostgreSQL логи
echo "🐘 PostgreSQL логи:"
tail -n 20 /var/log/postgresql/postgresql-15-main.log
```

## Полезные команды

### Очистка системы

```bash
# Очистка логов
find . -name "*.log" -type f -mtime +7 -delete

# Очистка базы данных
psql -h localhost -U pingtower -d pingtower -c "
DELETE FROM notification_deliveries WHERE sent_at < NOW() - INTERVAL '30 days';
DELETE FROM alerts WHERE triggered_at < NOW() - INTERVAL '30 days';
VACUUM ANALYZE;
"

# Очистка Docker
docker system prune -f
```

### Резервное копирование

```bash
# Бэкап базы данных
pg_dump -h localhost -U pingtower pingtower > backup_$(date +%Y%m%d_%H%M%S).sql

# Бэкап конфигурации
tar -czf config_backup_$(date +%Y%m%d_%H%M%S).tar.gz \
  bot/.env \
  backend/src/main/resources/application.yaml \
  docker/docker-compose.yml
```

## Получение помощи

### Логи для поддержки

```bash
# Сбор логов для поддержки
mkdir -p support_logs
cp bot/monitoring_bot.log support_logs/
cp backend/logs/application.log support_logs/
cp docker/docker-compose.yml support_logs/
cp bot/.env support_logs/
cp backend/src/main/resources/application.yaml support_logs/

# Создание архива
tar -czf support_logs_$(date +%Y%m%d_%H%M%S).tar.gz support_logs/
```

### Контакты

- **GitHub Issues**: [Создать issue](https://github.com/your-repo/issues)
- **Email**: support@pingtower.com
- **Telegram**: @pingtower_support
