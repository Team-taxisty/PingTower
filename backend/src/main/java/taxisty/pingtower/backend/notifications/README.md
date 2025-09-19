## Notifications Module

Purpose: delivery of alert notifications via Telegram, Email (SMTP), and HTTP Webhooks. Provides a routing service and per-channel senders.

### Structure
- NotificationService: routes messages to a channel by type and JSON configuration
- NotificationChannelType: supported channel types (TELEGRAM, EMAIL, WEBHOOK)
- NotificationMessage: normalized message object (title, text, severity, link, attributes)
- config: channel configuration records (TelegramConfig, EmailConfig, WebhookConfig)
- sender: concrete senders (TelegramNotificationSender, EmailNotificationSender, WebhookNotificationSender)

Package root: `taxisty.pingtower.backend.notifications`

### Interaction
Programmatic
- Service: `NotificationService`
- Method: `send(String type, String configurationJson, NotificationMessage message)`
- Types: case-insensitive strings: `telegram`, `email`, `webhook`

Message fields
- `title`: short title
- `text`: main content
- `severity`: e.g. `INFO`, `WARN`, `CRITICAL`
- `link`: optional URL
- `attributes`: key-value map for extra context

Channel configuration (JSON)
- Telegram (TelegramConfig)
  - `{ "botToken": "<token>", "chatId": "<chat_id>" }`
- Email (EmailConfig)
  - `{ "from": "alerts@example.com", "to": ["dev@example.com"], "subjectPrefix": "[PingTower]" }`
  - SMTP server is taken from Spring `spring.mail.*` configuration
- Webhook (WebhookConfig)
  - `{ "url": "https://example.com/hook", "secret": "optional", "headers": { "X-App": "PingTower" } }`

REST
- Test endpoint
  - POST `/notifications/test`
  - Body: `SendNotificationRequest { type, configuration, title, text, severity, link, attributes }`
  - Response: `{ "status": "sent" }`

### Configuration
Spring Mail (global), in `application.yaml`:
- `spring.mail.host`
- `spring.mail.port`
- `spring.mail.username`
- `spring.mail.password`
- `spring.mail.properties.mail.smtp.auth`
- `spring.mail.properties.mail.smtp.starttls.enable`

### Dependencies
- Spring Mail (spring-boot-starter-mail) for Email channel
- Jackson for JSON serialization
- Java HttpClient for Telegram and Webhook channels

### Error Handling
- Exceptions during sending are logged and rethrown by `NotificationService`
- Callers should handle failures and/or implement retries at call site

