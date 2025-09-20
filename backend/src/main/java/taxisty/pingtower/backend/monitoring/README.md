# Monitoring Package

The monitoring package provides comprehensive web service monitoring capabilities for the PingTower system. It implements a hybrid architecture using PostgreSQL for structured data and recent results, and ClickHouse for time series analytics and historical data storage.

## Architecture

The monitoring package consists of several key components:

### Configuration (`config/`)
- **ClickHouseConfig**: Configuration for ClickHouse time series database connection
- **MonitoringProperties**: Application properties for monitoring system configuration
- **MessagingConfig**: Configuration constants for RabbitMQ messaging (to be expanded)

### Repository Layer (`repository/`)
- **ClickHouseRepository**: Interface for time series data operations
- **ClickHouseRepositoryImpl**: High-performance implementation using ClickHouse for analytics
- **MonitoredServiceRepository**: PostgreSQL JPA repository for service configuration management
- **CheckResultRepository**: PostgreSQL JPA repository for recent check results

### Service Layer (`service/`)
- **MonitoringService**: Core service coordinating data flow between PostgreSQL and ClickHouse
- **MonitoringAnalyticsService**: Service for aggregating metrics and generating reports
- **MonitoredServiceManager**: CRUD operations for monitored services configuration
- **DataSynchronizationService**: Handles data migration and synchronization between databases

### Messaging (`messaging/`)
- **MonitoringEventPublisher**: Interface for publishing monitoring events
- **MonitoringEventConsumer**: Interface for consuming monitoring events
- **SimpleMonitoringEventPublisher**: Basic implementation (to be replaced with RabbitMQ)

## Key Features

### Time Series Analytics
- High-performance storage in ClickHouse optimized for monitoring data
- Automatic table partitioning by month with TTL policies
- Efficient queries for uptime calculations, response time metrics, and trend analysis

### Real-time Monitoring
- Processing of check results from the scheduler
- Service status determination (UP, DOWN, DEGRADED, UNKNOWN)
- Dashboard data aggregation with real-time metrics

### SLA Reporting
- Automated SLA compliance calculation
- Downtime tracking and reporting
- Performance trend analysis

### Data Management
- Automatic cleanup of old monitoring data
- Batch processing for high-volume data ingestion
- Configurable retention policies

## Database Architecture

### PostgreSQL (Structured Data & Recent Results)
- **MonitoredService**: Service configurations, user associations, monitoring settings
- **CheckResult**: Recent check results (last 30 days) for fast dashboard access
- **AlertRule**: Alert configuration and thresholds
- **User**: User management and permissions

### ClickHouse (Time Series Analytics)
- **check_results_ts**: Historical monitoring data with automatic partitioning
- **service_metrics_ts**: Pre-aggregated metrics for different time periods
- Optimized for analytical queries and large data volumes
- TTL policies for automatic data cleanup

### Data Flow
1. **Check Results**: Saved immediately to PostgreSQL, then replicated to ClickHouse
2. **Historical Data**: Migrated from PostgreSQL to ClickHouse after 7 days
3. **Analytics**: Performed on ClickHouse for optimal performance
4. **Recent Data**: Retrieved from PostgreSQL for fast dashboard updates

## Configuration

Add the following properties to your `application.yaml`:

```yaml
monitoring:
  clickhouse:
    url: jdbc:clickhouse://localhost:8123/monitoring
    username: default
    password: ""
    driver-class-name: com.clickhouse.jdbc.ClickHouseDriver
  analytics:
    batch-size: 1000
    flush-interval-seconds: 60
    enable-real-time-metrics: true
    aggregation-periods: "1h,6h,1d,7d,30d"
```

## Usage

### Processing Check Results
```java
@Autowired
private MonitoringService monitoringService;

// Process a single check result
monitoringService.processCheckResult(checkResult);

// Process multiple results in batch
monitoringService.processCheckResults(checkResults);
```

### Getting Dashboard Data
```java
// Get monitoring dashboard data for a service
LocalDateTime start = LocalDateTime.now().minusHours(24);
LocalDateTime end = LocalDateTime.now();
MonitoringDashboardData dashboardData = 
    monitoringService.getDashboardData(serviceId, start, end);
```

### Generating SLA Reports
```java
@Autowired
private MonitoringAnalyticsService analyticsService;

// Generate SLA report for the last month
LocalDateTime start = LocalDateTime.now().minusDays(30);
LocalDateTime end = LocalDateTime.now();
SLAReport report = analyticsService.generateSLAReport(serviceId, start, end);
```

## Integration Points

### With Scheduler Package
The monitoring package receives check results from the scheduler package through:
- Direct method calls
- Message queue events (when RabbitMQ is implemented)

### With Storage Package
The monitoring package uses models from the storage package:
- `CheckResult`: Individual check result data
- `ServiceMetrics`: Aggregated service metrics
- `MonitoredService`: Service configuration (read-only)

### With API Package
The monitoring package can be exposed through REST endpoints for:
- Dashboard data retrieval
- SLA report generation
- Service status queries

## Future Enhancements

1. **RabbitMQ Integration**: Replace simple messaging with full RabbitMQ implementation
2. **Alert Processing**: Integrate with alert rules and notification system
3. **Real-time Streaming**: Add support for real-time data streaming
4. **Advanced Analytics**: Machine learning-based anomaly detection
5. **Multi-region Support**: Cross-region monitoring and aggregation

## Performance Considerations

- ClickHouse is optimized for analytical queries on time series data
- Batch processing reduces individual insert overhead
- Automatic data partitioning improves query performance
- TTL policies manage storage growth automatically
- Indexes on service_id and timestamp columns optimize common queries

## Dependencies

Required dependencies in `build.gradle`:
- `com.clickhouse:clickhouse-jdbc:0.6.5` - ClickHouse JDBC driver
- `org.springframework.boot:spring-boot-starter-amqp` - RabbitMQ support (for future use)
- Standard Spring Boot starters for JPA, web, and validation