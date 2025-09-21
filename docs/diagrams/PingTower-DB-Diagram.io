////////////////////////////////////////////////////////////
// PingTower — ERD for dbdiagram.io
// Core: PostgreSQL domain model
// TS/Analytics: ClickHouse (logical links only)
////////////////////////////////////////////////////////////

Table app_user as User {
  id               bigserial [pk, note: 'Идентификатор пользователя']
  username         varchar(100) [not null]
  email            varchar(255) [not null, unique]
  password_hash    varchar(255) [not null]
  is_active        boolean
  last_login_at    timestamp
  created_at       timestamp
  updated_at       timestamp

  Note: 'Пользователи приложения'
}

Table user_roles as UserRole {
  user_id  bigint   [not null, ref: > User.id]
  role     varchar  [not null]

  Indexes {
    (user_id, role) [unique, name: 'uq_user_role']
  }

  Note: 'Роли пользователя (многие-к-одному к User)'
}

Table monitored_service as Service {
  id                         bigserial [pk]
  name                       varchar(255) [not null]
  description                text
  url                        varchar(2000) [not null]
  http_method                varchar(10)
  headers                    json
  expected_response_code     varchar(10)  // текстовая версия, см. also expected_status_code
  expected_content           text
  ssl_certificate_check      boolean
  is_active                  boolean
  type                       varchar(50)
  service_type               varchar(10)  // PING/HTTP
  request_body               text
  query_params               json
  expected_response_body     text
  is_alive                   boolean       // кэш состояния
  check_interval_minutes     int
  timeout_seconds            int
  expected_status_code       int
  user_id                    bigint [not null, ref: > User.id]
  created_at                 timestamp
  updated_at                 timestamp

  Note: 'Объект мониторинга (владелец — User)'
}

Table check_schedule as CheckSchedule {
  id                bigserial [pk]
  service_id        bigint    [not null, unique, ref: > Service.id] // 1:1 через UNIQUE
  cron_expression   varchar(100)
  interval_seconds  int
  is_enabled        boolean
  timezone          varchar(50)
  next_run_time     timestamp
  created_at        timestamp
  updated_at        timestamp

  Note: 'Расписание проверок (1:1 с Service)'
}

Table check_result as CheckResult {
  id                bigserial [pk]
  service_id        bigint    [not null, ref: > Service.id]
  check_time        timestamp [not null]
  is_successful     boolean   [not null]
  response_code     int
  response_time_ms  bigint
  response_body     text
  error_message     text
  ssl_valid         boolean
  ssl_expiry_date   timestamp
  check_location    varchar(100)

  Indexes {
    service_id [name: 'idx_check_result_service_id']
    check_time [name: 'idx_check_result_check_time']
  }

  Note: 'Результаты последних проверок (оперативная история)'
}

Table alert_rule as AlertRule {
  id                           bigserial [pk]
  service_id                   bigint   [not null, ref: > Service.id]
  name                         varchar(255) [not null]
  description                  text
  failure_threshold            int
  warning_threshold            int
  response_time_threshold_ms   bigint
  is_enabled                   boolean
  severity                     varchar(50) // HIGH/...
  user_id                      bigint [not null, ref: > User.id]
  created_at                   timestamp
  updated_at                   timestamp

  Note: 'Правила срабатывания алертов'
}

Table alert as Alert {
  id            bigserial [pk]
  alert_rule_id bigint   [ref: > AlertRule.id] // может быть NULL
  service_id    bigint   [not null, ref: > Service.id]
  message       text     [not null]
  severity      varchar(50)
  is_resolved   boolean
  triggered_at  timestamp [not null]
  resolved_at   timestamp
  metadata      json

  Note: 'События-алерты'
}

Table notification_channel as Channel {
  id            bigserial [pk]
  user_id       bigint   [not null, ref: > User.id]
  type          varchar(50)  [not null] // EMAIL/TELEGRAM/WEBHOOK/...
  name          varchar(255) [not null]
  configuration text         [not null] // JSON/строка
  is_enabled    boolean
  is_default    boolean
  created_at    timestamp
  updated_at    timestamp

  Note: 'Каналы уведомлений пользователя'
}

Table notification_delivery as Delivery {
  id               bigserial [pk]
  alert_id         bigint   [not null, ref: > Alert.id]
  channel_id       bigint   [not null, ref: > Channel.id]
  status           varchar(50)
  delivery_method  varchar(50) // email/webhook/telegram
  attempt_count    int
  error_message    text
  sent_at          timestamp
  delivered_at     timestamp

  Note: 'Попытки доставки уведомлений'
}

Table service_metrics as ServiceMetrics {
  id                         bigserial [pk]
  service_id                 bigint    [not null, ref: > Service.id]
  period_start               timestamp [not null]
  period_end                 timestamp [not null]
  uptime_percentage          double
  average_response_time_ms   bigint
  max_response_time_ms       bigint
  min_response_time_ms       bigint
  total_checks               int
  successful_checks          int
  failed_checks              int
  aggregation_period         varchar(50) // '1h','1d'

  Note: 'Агрегированные SLA-метрики'
}

// -----------------------------
// ClickHouse (logical entities)
// -----------------------------

Table check_results_ts as CH_CheckResultsTs {
  id                bigint
  service_id        bigint
  check_time        timestamp
  is_successful     int
  response_code     int
  response_time_ms  int
  response_body     text
  error_message     text
  ssl_valid         int
  ssl_expiry_date   timestamp
  check_location    varchar(100)

  Note: 'ClickHouse. Исторические результаты. ЛОГИЧЕСКАЯ ссылка на Service'
}

Table service_metrics_ts as CH_ServiceMetricsTs {
  id                         bigint
  service_id                 bigint
  period_start               timestamp
  period_end                 timestamp
  uptime_percentage          double
  average_response_time_ms   double
  max_response_time_ms       int
  min_response_time_ms       int
  total_checks               int
  successful_checks          int
  failed_checks              int
  aggregation_period         varchar(50)

  Note: 'ClickHouse. Аналитические метрики. ЛОГИЧЕСКАЯ ссылка на Service'
}

// -----------------------------
// Relationships (explicit refs are already in columns above;
// dbdiagram will infer from [ref] annotations.
// Extra logical links for ClickHouse below:
// -----------------------------

Ref: CH_CheckResultsTs.service_id > Service.id
Ref: CH_ServiceMetricsTs.service_id > Service.id
