# Модуль API

Назначение: слой HTTP REST для управления проверками и предоставления функциональности системы. Определяет DTO запросов/ответов, контроллеры, сопоставление ошибок и контракт OpenAPI.

## Структура
- controller: контроллеры Spring MVC (`ChecksController`, `RunsController`, `NotificationsController`)
- dto: полезные нагрузки запросов/ответов (например, `CheckDto`, `CheckCreateDto`, `SendNotificationRequest`)
- service: сервисы и вспомогательные компоненты уровня API (`CheckService`, `RunService`, `IdempotencyService`)
- error: унифицированные ответы об ошибках и обработчик исключений (`GlobalExceptionHandler`)
- openapi.yml: спецификация OpenAPI 3.1

Корневой пакет: `taxisty.pingtower.backend.api`

## Взаимодействие
REST поверх HTTP. Базовый путь настраивается через `server.servlet.context-path`.

- Список проверок
  - GET `/checks?limit={1..500}&cursor={string}`
  - Ответ: `{ items: Check[], next_cursor: string|null }`

- Создать проверку (идемпотентно)
  - POST `/checks`
  - Заголовки: `Idempotency-Key: <string>`
  - Тело: `CheckCreate`
  - Ответы: `201` (создано), `200` (повтор идемпотентной операции), `409` (конфликт)

- Получить проверку
  - GET `/checks/{id}`

- Обновить проверку (частично)
  - PATCH `/checks/{id}`

- Удалить проверку
  - DELETE `/checks/{id}`

- Список запусков
  - GET `/runs?check_id={uuid}&from={iso}&to={iso}&limit={int}`
  - Ответ: `{ items: Run[] }`

- Тестовая отправка уведомления (для модуля notifications)
  - POST `/notifications/test`
  - Тело: `SendNotificationRequest`

Модель ошибки: `ErrorResponse { code, message, details? }`

## Соглашения
- Постраничность: `limit` + `next_cursor`
- Идемпотентность: заголовок `Idempotency-Key` для операций создания
- Валидация: аннотации `jakarta.validation` на DTO
- Ошибки: сопоставляются `GlobalExceptionHandler` к `ErrorResponse`

## OpenAPI
- Файл спецификации: `api/openapi.yml`
- Используется как источник истины для генерации клиентов и ревью эндпоинтов
