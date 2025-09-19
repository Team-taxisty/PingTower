# API Module

Purpose: HTTP REST layer for managing checks and exposing system functionality. Defines request/response DTOs, controllers, error mapping, and OpenAPI contract.

## Structure
- controller: Spring MVC controllers (`ChecksController`, `RunsController`, `NotificationsController`)
- dto: Request/response payloads (e.g., `CheckDto`, `CheckCreateDto`, `SendNotificationRequest`)
- service: API-level services and helpers (`CheckService`, `RunService`, `IdempotencyService`)
- error: Unified error responses and exception handler (`GlobalExceptionHandler`)
- openapi.yml: OpenAPI 3.1 specification

Package root: `taxisty.pingtower.backend.api`

## Interaction
REST over HTTP. Base path is configured via `server.servlet.context-path`.

- List checks
  - GET `/checks?limit={1..500}&cursor={string}`
  - Response: `{ items: Check[], next_cursor: string|null }`

- Create check (idempotent)
  - POST `/checks`
  - Headers: `Idempotency-Key: <string>`
  - Body: `CheckCreate`
  - Responses: `201` (created), `200` (idempotent replay), `409` (conflict)

- Get check
  - GET `/checks/{id}`

- Update check (partial)
  - PATCH `/checks/{id}`

- Delete check
  - DELETE `/checks/{id}`

- List runs
  - GET `/runs?check_id={uuid}&from={iso}&to={iso}&limit={int}`
  - Response: `{ items: Run[] }`

- Test notification delivery (for notifications module)
  - POST `/notifications/test`
  - Body: `SendNotificationRequest`

Error model: `ErrorResponse { code, message, details? }`

## Conventions
- Pagination: `limit` + `next_cursor`
- Idempotency: `Idempotency-Key` header on create operations
- Validation: `jakarta.validation` annotations on DTOs
- Errors: mapped by `GlobalExceptionHandler` to `ErrorResponse`

## OpenAPI
- Spec file: `api/openapi.yml`
- Use as source of truth for client generation and endpoint review

