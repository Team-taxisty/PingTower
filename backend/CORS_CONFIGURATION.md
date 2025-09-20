# CORS Configuration для PingTower Backend

## Обзор

CORS (Cross-Origin Resource Sharing) политика настроена для обеспечения корректной работы фронтенда с бэкендом в различных средах развертывания.

## Конфигурация

### Основные файлы конфигурации:

1. **`CorsConfig.java`** - Основная конфигурация CORS
2. **`SecurityConfig.java`** - Интеграция CORS с Spring Security

### Разрешенные источники (Origins):

#### Локальная разработка:
- `http://localhost:*` - Любой порт localhost
- `http://127.0.0.1:*` - Альтернативный localhost
- `https://localhost:*` - HTTPS локальная разработка
- `https://127.0.0.1:*` - HTTPS альтернативный localhost

#### Docker окружение:
- `http://pingtower-frontend:*` - Docker контейнер фронтенда
- `http://nginx:*` - Nginx прокси

#### Поддомены:
- `http://*.localhost:*` - Поддомены localhost
- `https://*.localhost:*` - HTTPS поддомены

#### Продакшен (требует настройки):
- `https://yourdomain.com`
- `https://www.yourdomain.com`

### Разрешенные HTTP методы:
- GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD

### Разрешенные заголовки:
- `Authorization` - JWT токены
- `Content-Type` - Тип контента
- `Accept` - Принимаемые типы
- `Origin` - Источник запроса
- `Access-Control-Request-Method` - CORS preflight
- `Access-Control-Request-Headers` - CORS preflight
- `X-Requested-With` - AJAX запросы
- `Cache-Control` - Кэширование
- `Pragma` - Кэширование
- `X-CSRF-TOKEN` - CSRF защита
- `X-Forwarded-For` - Прокси заголовки
- `X-Forwarded-Proto` - Прокси заголовки
- `X-Real-IP` - Прокси заголовки

### Экспонируемые заголовки:
- `Authorization` - JWT токены в ответе
- `Content-Type` - Тип контента ответа
- `Access-Control-Allow-Origin` - CORS заголовки
- `Access-Control-Allow-Credentials`
- `Access-Control-Allow-Methods`
- `Access-Control-Allow-Headers`
- `X-Total-Count` - Пагинация
- `X-Page-Count` - Пагинация

## Настройка для продакшена

Для продакшена необходимо обновить список разрешенных источников в файле `CorsConfig.java`:

```java
List<String> allowedOrigins = Arrays.asList(
    // ... существующие origins ...
    
    // Продакшен домены
    "https://yourdomain.com",
    "https://www.yourdomain.com",
    "https://api.yourdomain.com"
);
```

## Тестирование CORS

### Тест простого запроса:
```bash
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X GET \
     http://localhost:8080/v1/api/health
```

### Тест preflight запроса:
```bash
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://localhost:8080/v1/api/health
```

## Особенности

1. **Credentials поддержка**: Включена поддержка cookies и авторизационных заголовков
2. **Preflight кэширование**: Максимальное время кэширования preflight запросов - 1 час
3. **Двойная конфигурация**: CORS настроен как для Spring Security, так и для Spring MVC
4. **Гибкие паттерны**: Использование паттернов для поддержки различных портов и поддоменов

## Безопасность

- CORS политика настроена для разрешения только необходимых источников
- Поддержка credentials включена только для доверенных доменов
- CSRF защита отключена для API (используется JWT аутентификация)
