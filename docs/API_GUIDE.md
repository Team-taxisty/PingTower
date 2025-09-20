# Инструкция по использованию API PingTower

**Базовый URL:** `http://localhost:8080` (при запуске через Docker Compose, как указано в `scope.txt`).

**Аутентификация:** Все защищенные эндпоинты требуют JWT-токена в заголовке `Authorization: Bearer <token>`.

## 1. Создание пользователя (Регистрация)
**Эндпоинт:** `POST /v1/api/auth/register`  
**Описание:** Регистрирует нового пользователя в системе.  
**Тело запроса (JSON):**
```json
{
  "username": "testuser",
  "email": "test@example.com", 
  "password": "password123"
}
```
**Curl команда:**
```bash
curl -X POST http://localhost:8080/v1/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```
**Ответ:** Возвращает JWT-токен, ID пользователя и роли. Сохраните токен для дальнейших запросов.

## 2. Авторизация (Логин)
**Эндпоинт:** `POST /v1/api/auth/login`  
**Описание:** Аутентифицирует пользователя и возвращает JWT-токен для доступа к защищенным эндпоинтам.  
**Тело запроса (JSON):**
```json
{
  "username": "testuser",
  "password": "password123"
}
```
**Curl команда:**
```bash
curl -X POST http://localhost:8080/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```
**Ответ:** Возвращает JWT-токен (используйте его в заголовке `Authorization` для других запросов).

## 3. Добавление сервиса для мониторинга
**Эндпоинт:** `POST /v1/api/services`  
**Описание:** Создает новый сервис для мониторинга. Сервис привязывается к текущему пользователю (по JWT-токену).  
**Заголовки:** `Authorization: Bearer <token>` (токен из регистрации/логина), `Content-Type: application/json`.

### Вариант 1: Простой пинг (PING)
Проверяет доступность URL простым HTTP-запросом (GET по умолчанию).
**Тело запроса (JSON):**
```json
{
  "name": "My Website",
  "description": "Simple ping check for website availability",
  "url": "https://example.com",
  "serviceType": "PING",
  "enabled": true,
  "checkIntervalMinutes": 5,
  "timeoutSeconds": 30
}
```
**Curl команда:**
```bash
TOKEN="your-jwt-token-here"
curl -X POST http://localhost:8080/v1/api/services \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Website",
    "description": "Simple ping check for website availability",
    "url": "https://example.com",
    "serviceType": "PING",
    "enabled": true,
    "checkIntervalMinutes": 5,
    "timeoutSeconds": 30
  }'
```

### Вариант 2: API endpoint пинг (API)
Проверяет API-эндпоинт с возможностью настройки HTTP-метода, заголовков, тела запроса и ожидаемого ответа.
**Тело запроса (JSON):**
```json
{
  "name": "My API Health Check",
  "description": "API endpoint monitoring with custom validation",
  "url": "https://api.example.com/health",
  "serviceType": "API",
  "enabled": true,
  "checkIntervalMinutes": 5,
  "timeoutSeconds": 30,
  "httpMethod": "GET",
  "headers": {
    "Authorization": "Bearer api-token",
    "Content-Type": "application/json"
  },
  "expectedStatusCode": 200,
  "expectedResponseBody": "{\"status\":\"ok\"}"
}
```
**Curl команда:**
```bash
TOKEN="your-jwt-token-here"
curl -X POST http://localhost:8080/v1/api/services \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My API Health Check",
    "description": "API endpoint monitoring with custom validation",
    "url": "https://api.example.com/health",
    "serviceType": "API",
    "enabled": true,
    "checkIntervalMinutes": 5,
    "timeoutSeconds": 30,
    "httpMethod": "GET",
    "headers": {
      "Authorization": "Bearer api-token",
      "Content-Type": "application/json"
    },
    "expectedStatusCode": 200,
    "expectedResponseBody": "{\"status\":\"ok\"}"
  }'
```

## Дополнительные полезные эндпоинты
- **Получить профиль пользователя:** `GET /v1/api/auth/profile` (требует токена)
- **Получить все сервисы:** `GET /v1/api/services` (требует токена)
- **Протестировать сервис вручную:** `POST /v1/api/services/{id}/test` (требует токена)
- **Обновить сервис:** `PUT /v1/api/services/{id}` (требует токена)
- **Удалить сервис:** `DELETE /v1/api/services/{id}` (требует токена)

## Запуск системы
Как указано в `scope.txt`, запускайте через Docker Compose:
```bash
docker compose up -d
```

## Примечания
- Все поля валидируются на backend (например, URL должен начинаться с `http://` или `https://`, тип сервиса только `PING` или `API`).
- По умолчанию: интервал проверки - 5 минут, таймаут - 30 секунд, ожидаемый статус - 200.
- Сервисы автоматически планируются для мониторинга при создании, если `enabled: true`.
- Для более сложных сценариев (POST/PUT запросы, кастомные заголовки) используйте тип `API`.