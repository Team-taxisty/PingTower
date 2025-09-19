# PingTower Scheduler

The scheduler package provides a comprehensive monitoring task execution system built on Spring Boot and Quartz Scheduler. It enables flexible, cron-like scheduling of service monitoring checks with support for different monitoring types and configurable intervals.

## Architecture

```
scheduler/
├── config/
│   ├── AutowiringSpringBeanJobFactory.java    # Spring DI integration for Quartz jobs
│   ├── SchedulerConfig.java                   # Main scheduler configuration
│   └── SchedulerProperties.java               # Configuration properties
├── service/
│   ├── MonitoringExecutorService.java         # Task execution coordinator
│   ├── MonitoringJob.java                     # Quartz job implementation
│   ├── SchedulerManagementService.java        # Lifecycle management
│   ├── SchedulerService.java                  # Core scheduling operations
│   └── MonitoringDataService.java             # Data access interface
└── task/
    ├── ScheduledTask.java                     # Task interface
    ├── TaskExecutionContext.java              # Execution metadata
    ├── TaskType.java                          # Task type enumeration
    ├── HttpMonitoringTask.java                # HTTP/HTTPS monitoring
    └── ApiMonitoringTask.java                 # API endpoint monitoring
```

## Core Components

### SchedulerService
Main orchestrator for monitoring task scheduling. Provides methods to:
- Schedule monitoring for services with flexible intervals (seconds to hours)
- Support both cron expressions and simple intervals
- Handle job lifecycle (create, update, delete, trigger immediate execution)
- Integrate with Quartz scheduler for persistent job storage

Key methods:
- `scheduleMonitoring(MonitoredService, CheckSchedule)` - Schedule a service for monitoring
- `unscheduleMonitoring(Long serviceId)` - Remove monitoring schedule
- `rescheduleMonitoring(MonitoredService, CheckSchedule)` - Update existing schedule
- `triggerImmediateCheck(Long serviceId)` - Force immediate execution

### MonitoringExecutorService
Coordinates task execution and result handling:
- Selects appropriate monitoring task based on service configuration
- Executes monitoring tasks with error handling and retry logic
- Stores results and triggers alerts on failures
- Manages task registry for different monitoring types

### Task Implementations

#### HttpMonitoringTask
Performs basic HTTP/HTTPS availability checks:
- Supports all HTTP methods (GET, POST, PUT, DELETE, HEAD)
- Configurable timeouts and custom headers
- Response code validation (specific codes or patterns like "2xx")
- Content validation for expected response body content
- SSL certificate validation with expiry date checking
- Response time measurement

#### ApiMonitoringTask
Enhanced monitoring for API endpoints:
- JSON/XML response parsing and validation
- Health check endpoint pattern recognition
- API-specific header handling (Accept: application/json)
- Structured response validation for common health check patterns
- Content-type aware processing

## Configuration

### Application Properties
```properties
# Scheduler instance configuration
pingtower.scheduler.instance-name=PingTowerScheduler
pingtower.scheduler.thread-count=10
pingtower.scheduler.clustered=false

# Timing configuration
pingtower.scheduler.misfire-threshold=60000
pingtower.scheduler.cluster-checkin-interval=20000

# Retry and timeout settings
pingtower.scheduler.max-retry-attempts=3
pingtower.scheduler.retry-delay-seconds=30
pingtower.scheduler.default-timeout-seconds=30

# Startup behavior
pingtower.scheduler.auto-start=true
```

### Database Integration
The scheduler uses Quartz's JDBC job store for persistence:
- Jobs and triggers are stored in PostgreSQL database
- Supports clustering for high availability
- Automatic job recovery after application restarts
- Misfire handling for delayed executions

## Monitoring Schedule Configuration

Schedules are configured using the `CheckSchedule` model with two options:

### Cron-based Scheduling
```java
CheckSchedule cronSchedule = new CheckSchedule(
    null,                    // id
    serviceId,              // serviceId
    "0 */5 * * * ?",        // every 5 minutes
    0,                      // intervalSeconds (ignored)
    true,                   // isEnabled
    "UTC",                  // timezone
    null,                   // nextRunTime
    LocalDateTime.now(),    // createdAt
    LocalDateTime.now()     // updatedAt
);
```

### Interval-based Scheduling
```java
CheckSchedule intervalSchedule = new CheckSchedule(
    null,                   // id
    serviceId,             // serviceId
    null,                  // cronExpression
    300,                   // intervalSeconds (5 minutes)
    true,                  // isEnabled
    "UTC",                 // timezone
    null,                  // nextRunTime
    LocalDateTime.now(),   // createdAt
    LocalDateTime.now()    // updatedAt
);
```

## Task Execution Flow

1. **Quartz Trigger Fires** - Based on cron expression or interval
2. **MonitoringJob.execute()** - Quartz job entry point
3. **MonitoringExecutorService.executeMonitoring()** - Task coordination
4. **Task Selection** - Choose appropriate task type (HTTP, API, SSL)
5. **Task Execution** - Perform actual monitoring check
6. **Result Processing** - Store results and handle failures
7. **Alert Handling** - Trigger alerts for failed checks (via MonitoringDataService)

## Integration Points

### Storage Layer Integration
The scheduler requires implementation of `MonitoringDataService` interface:
```java
public interface MonitoringDataService {
    MonitoredService getMonitoredService(Long serviceId);
    CheckResult saveCheckResult(CheckResult checkResult);
    void handleFailureAlert(MonitoredService service, CheckResult failedResult);
}
```

### Service Registration
Monitoring tasks are automatically discovered via Spring's component scanning:
```java
@Component
public class CustomMonitoringTask implements ScheduledTask {
    @Override
    public String getTaskType() {
        return "CUSTOM_CHECK";
    }
    
    @Override
    public CheckResult execute(MonitoredService service) {
        // Implementation
    }
    
    @Override
    public boolean canExecute(MonitoredService service) {
        // Validation logic
    }
}
```

## Usage Examples

### Basic Service Scheduling
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

### Immediate Check Execution
```java
// Trigger immediate check outside regular schedule
schedulerService.triggerImmediateCheck(serviceId);
```

### Schedule Management
```java
// Update existing schedule
schedulerService.rescheduleMonitoring(service, newSchedule);

// Remove monitoring
schedulerService.unscheduleMonitoring(serviceId);

// Check if service is scheduled
boolean isScheduled = schedulerService.isMonitoringScheduled(serviceId);
```

## Error Handling

The scheduler implements comprehensive error handling:

1. **Network Failures** - Captured with timeout handling
2. **Invalid Configurations** - Validated before scheduling
3. **Task Execution Errors** - Logged with error results stored
4. **Scheduler Failures** - Graceful degradation with retry mechanisms
5. **Database Issues** - Quartz handles job persistence failures

## Performance Considerations

### Thread Pool Sizing
- Default: 10 threads for monitoring execution
- Configurable via `pingtower.scheduler.thread-count`
- Size based on expected concurrent monitoring load

### Resource Management
- HTTP connections use timeouts (default 30 seconds)
- Response bodies are truncated (1KB for HTTP, 2KB for API)
- SSL certificate checks are cached per execution

### Database Impact
- Efficient job storage using Quartz JDBC store
- Check results stored via batch operations where possible
- Historical data cleanup should be implemented separately

## Monitoring and Observability

The scheduler provides logging at multiple levels:
- INFO: Scheduling lifecycle events
- DEBUG: Individual task executions
- ERROR: Failures and exceptions
- WARN: Configuration issues

Metrics can be collected via:
- Quartz scheduler statistics
- Custom metrics in task implementations
- Spring Boot Actuator integration (if enabled)

## Extensibility

### Adding New Task Types
1. Implement `ScheduledTask` interface
2. Add `@Component` annotation
3. Define unique task type in `TaskType` enum
4. Implement task selection logic in `MonitoringExecutorService`

### Custom Configuration
Additional configuration properties can be added to `SchedulerProperties` and accessed throughout the scheduler package.

### Integration with Other Systems
The `MonitoringDataService` interface allows integration with different storage layers and alerting systems without modifying core scheduler logic.