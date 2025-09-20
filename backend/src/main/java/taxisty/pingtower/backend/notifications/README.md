## Модуль Notifications

Назначение: доставка уведомлений об инцидентах через Telegram, Email (SMTP) и HTTP Webhooks. Предоставляет сервис маршрутизации и отправители для каждого канала.

### Структура
- NotificationService: маршрутизирует сообщения в канал по типу и JSON‑конфигурации
- NotificationChannelType: поддерживаемые типы каналов (TELEGRAM, EMAIL, WEBHOOK)
- NotificationMessage: нормализованное сообщение (title, text, severity, link, attributes)
- config: записи конфигурации каналов (TelegramConfig, EmailConfig, WebhookConfig)
- sender: конкретные отправители (TelegramNotificationSender, EmailNotificationSender, WebhookNotificationSender)

Корневой пакет: `taxisty.pingtower.backend.notifications`

### Взаимодействие
Программное
- Сервис: `NotificationService`
- Метод: `send(String type, String configurationJson, NotificationMessage message)`
- Типы: нечувствительные к регистру строки: `telegram`, `email`, `webhook`

Поля сообщения
- `title`: короткий заголовок
- `text`: основное содержимое
- `severity`: например, `INFO`, `WARN`, `CRITICAL`
- `link`: необязательный URL
- `attributes`: карта ключ‑значение для дополнительного контекста

Конфигурация канала (JSON)
- Telegram (TelegramConfig)
  - `{ "botToken": "<token>", "chatId": "<chat_id>" }`
- Email (EmailConfig)
  - `{ "from": "alerts@example.com", "to": ["dev@example.com"], "subjectPrefix": "[PingTower]" }`
  - SMTP‑сервер берётся из настроек Spring `spring.mail.*`
- Webhook (WebhookConfig)
  - `{ "url": "https://example.com/hook", "secret": "optional", "headers": { "X-App": "PingTower" } }`

REST
- Тестовый эндпоинт
  - POST `/notifications/test`
  - Тело: `SendNotificationRequest { type, configuration, title, text, severity, link, attributes }`
  - Ответ: `{ "status": "sent" }`

### Конфигурация
Spring Mail (глобально), в `application.yaml`:
- `spring.mail.host`
- `spring.mail.port`
- `spring.mail.username`
- `spring.mail.password`
- `spring.mail.properties.mail.smtp.auth`
- `spring.mail.properties.mail.smtp.starttls.enable`

### Зависимости
- Spring Mail (spring-boot-starter-mail) для канала Email
- Jackson для сериализации JSON
- Java HttpClient для каналов Telegram и Webhook

### Обработка ошибок
- Исключения при отправке логируются и пробрасываются `NotificationService`
- Вызывающие должны обрабатывать сбои и/или реализовать ретраи на своей стороне
