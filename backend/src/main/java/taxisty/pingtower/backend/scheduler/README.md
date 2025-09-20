# Планировщик PingTower

Пакет планировщика предоставляет полноценную систему выполнения задач мониторинга на базе Spring Boot и Quartz Scheduler. Обеспечивает гибкое расписание проверок сервисов в стиле cron с поддержкой разных типов мониторинга и настраиваемых интервалов.

## Архитектура

```
scheduler/
└── config/
    ├── AutowiringSpringBeanJobFactory.java    # Интеграция Spring DI для задач Quartz
    ├── SchedulerConfig.java                   # Основная конфигурация планировщика
    └── SchedulerProperties.java               # Конфигурационные свойства
└── service/
    ├── MonitoringExecutorService.java         # Координатор выполнения задач
    ├── MonitoringJob.java                     # Реализация задачи Quartz
    ├── SchedulerManagementService.java        # Управление жизненным циклом
    ├── SchedulerService.java                  # Основные операции планирования
    └── MonitoringDataService.java             # Интерфейс доступа к данным
└── task/
    ├── ScheduledTask.java                     # Интерфейс задачи
    ├── TaskExecutionContext.java              # Метаданные выполнения
    ├── TaskType.java                          # Перечисление типов задач
    ├── HttpMonitoringTask.java                # Мониторинг HTTP/HTTPS
    └── ApiMonitoringTask.java                 # Мониторинг API‑эндпоинтов
```

## Ключевые Компоненты

### SchedulerService
Главный оркестратор планирования задач мониторинга. Предоставляет возможности:
- Планировать мониторинг сервисов с гибкими интервалами (от секунд до часов)
- Поддерживать как cron‑выражения, так и простые интервалы
- Управлять жизненным циклом задач (создание, обновление, удаление, немедленный запуск)
- Интегрироваться с Quartz для устойчивого хранения задач

Ключевые методы:
- `scheduleMonitoring(MonitoredService, CheckSchedule)` — запланировать мониторинг сервиса
- `unscheduleMonitoring(Long serviceId)` — снять сервис с мониторинга
- `rescheduleMonitoring(MonitoredService, CheckSchedule)` — обновить существующее расписание
- `triggerImmediateCheck(Long serviceId)` — запустить проверку немедленно

### MonitoringExecutorService
Координирует выполнение задач и обработку результатов:
- Выбирает подходящую задачу мониторинга на основе конфигурации сервиса
- Выполняет задачи мониторинга с обработкой ошибок и логикой повторов
- Сохраняет результаты и инициирует оповещения при сбоях
- Управляет реестром задач для разных типов мониторинга

### Реализации Задач

#### HttpMonitoringTask
Выполняет базовые проверки доступности HTTP/HTTPS:
- Поддерживает все HTTP‑методы (GET, POST, PUT, DELETE, HEAD)
- Настраиваемые таймауты и пользовательские заголовки
- Валидация кода ответа (конкретные коды или шаблоны вроде «2xx»)
- Проверка содержимого на ожидаемый контент ответа
- Проверка SSL‑сертификата с контролем срока действия
- Измерение времени ответа

#### ApiMonitoringTask
Расширенный мониторинг для API‑эндпоинтов:
- Разбор и валидация JSON/XML ответов
- Распознавание стандартных health‑эндпоинтов
- Обработка API‑специфичных заголовков (Accept: application/json)
- Структурированная проверка ответа для типовых health‑паттернов
- Учёт типа контента при обработке

## Конфигурация

### Application Properties
```properties
# Конфигурация экземпляра планировщика
pingtower.scheduler.instance-name=PingTowerScheduler
pingtower.scheduler.thread-count=10
pingtower.scheduler.clustered=false

# Настройки таймингов
pingtower.scheduler.misfire-threshold=60000
pingtower.scheduler.cluster-checkin-interval=20000

# Настройки повторов и таймаутов
pingtower.scheduler.max-retry-attempts=3
pingtower.scheduler.retry-delay-seconds=30
pingtower.scheduler.default-timeout-seconds=30

# Поведение при запуске
pingtower.scheduler.auto-start=true
```

### Интеграция с БД
Планировщик использует JDBC‑хранилище Quartz для персистентности:
- Задачи и триггеры хранятся в базе PostgreSQL
- Поддерживается кластеризация для высокой доступности
- Автоматическое восстановление задач после перезапуска приложения
- Обработка «misfire» для отложенных запусков

## Настройка Расписания Мониторинга

Расписания задаются с помощью модели `CheckSchedule` двумя способами:

### Планирование на основе Cron
```java
CheckSchedule cronSchedule = new CheckSchedule(
    null,                    // id
    serviceId,               // serviceId
    "0 */5 * * * ?",        // каждые 5 минут
    0,                       // intervalSeconds (игнорируется)
    true,                    // isEnabled
    "UTC",                  // часовой пояс
    null,                    // nextRunTime
    LocalDateTime.now(),     // createdAt
    LocalDateTime.now()      // updatedAt
);
```

### Планирование по Интервалу
```java
CheckSchedule intervalSchedule = new CheckSchedule(
    null,                    // id
    serviceId,               // serviceId
    null,                    // cronExpression
    300,                     // intervalSeconds (5 минут)
    true,                    // isEnabled
    "UTC",                  // часовой пояс
    null,                    // nextRunTime
    LocalDateTime.now(),     // createdAt
    LocalDateTime.now()      // updatedAt
);
```

## Поток Выполнения Задачи

1. **Срабатывание триггера Quartz** — по cron‑выражению или интервалу
2. **MonitoringJob.execute()** — точка входа задачи Quartz
3. **MonitoringExecutorService.executeMonitoring()** — координация выполнения
4. **Выбор задачи** — определяется подходящий тип (HTTP, API, SSL)
5. **Выполнение задачи** — реальная проверка мониторинга
6. **Обработка результата** — сохранение результатов и обработка сбоев
7. **Оповещения** — генерация алертов для неуспешных проверок (через MonitoringDataService)

## Точки Интеграции

### Интеграция со Слоем Хранения
Планировщик требует реализацию интерфейса `MonitoringDataService`:
```java
public interface MonitoringDataService {
    MonitoredService getMonitoredService(Long serviceId);
    CheckResult saveCheckResult(CheckResult checkResult);
    void handleFailureAlert(MonitoredService service, CheckResult failedResult);
}
```

### Регистрация Сервисов
Задачи мониторинга автоматически обнаруживаются с помощью компонент‑сканирования Spring:
```java
@Component
public class CustomMonitoringTask implements ScheduledTask {
    @Override
    public String getTaskType() {
        return "CUSTOM_CHECK";
    }
    
    @Override
    public CheckResult execute(MonitoredService service) {
        // Реализация
    }
    
    @Override
    public boolean canExecute(MonitoredService service) {
        // Логика валидации
    }
}
```

## Примеры Использования

### Базовое Планирование Сервиса
```java
@Autowired
private SchedulerService schedulerService;

public void setupMonitoring() {
    MonitoredService service = new MonitoredService(
        1L, "My API", "Production API", 
        "https://api.example.com/health",
        "GET", Map.of(), "200", "status\":\"ok", 
        true, true, 1L, 
        LocalDateTime.now(), LocalDateTime.now()
    );
    
    CheckSchedule schedule = new CheckSchedule(
        null, 1L, "0 */1 * * * ?", 0, true, "UTC",
        null, LocalDateTime.now(), LocalDateTime.now()
    );
    
    schedulerService.scheduleMonitoring(service, schedule);
}
```

### Немедленное Выполнение Проверки
```java
// Запустить немедленную проверку вне расписания
schedulerService.triggerImmediateCheck(serviceId);
```

### Управление Расписанием
```java
// Обновить существующее расписание
schedulerService.rescheduleMonitoring(service, newSchedule);

// Удалить мониторинг
schedulerService.unscheduleMonitoring(serviceId);

// Проверить, что сервис запланирован
boolean isScheduled = schedulerService.isMonitoringScheduled(serviceId);
```

## Обработка Ошибок

Планировщик реализует комплексную обработку ошибок:

1. **Сетевые ошибки** — перехватываются с учётом таймаутов
2. **Неверные конфигурации** — валидируются до планирования
3. **Ошибки выполнения задач** — логируются, результаты ошибок сохраняются
4. **Сбои планировщика** — плавная деградация с механизмами повторов
5. **Проблемы БД** — Quartz обрабатывает ошибки персистентности задач

## Соображения Производительности

### Размер Пула Потоков
- По умолчанию: 10 потоков для выполнения мониторинга
- Настраивается через `pingtower.scheduler.thread-count`
- Размер выбирается исходя из ожидаемой параллельной нагрузки

### Управление Ресурсами
- HTTP‑подключения используют таймауты (по умолчанию 30 секунд)
- Тела ответов усечены (1KB для HTTP, 2KB для API)
- Проверки SSL‑сертификатов кэшируются в рамках выполнения

### Влияние на Базу Данных
- Эффективное хранение задач с использованием JDBC‑хранилища Quartz
- Результаты проверок по возможности сохраняются пакетно
- Очистку исторических данных следует реализовать отдельно

## Мониторинг и Наблюдаемость

Планировщик ведёт логирование на нескольких уровнях:
- INFO: события жизненного цикла планирования
- DEBUG: выполнение отдельных задач
- ERROR: ошибки и исключения

