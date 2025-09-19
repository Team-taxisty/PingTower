# Storage Package

This package contains the data model definitions and data transfer objects for the PingTower monitoring system.

## Structure

- `model/` - Entity models representing persistent data structures
- `dto/` - Data Transfer Objects for API requests and responses

## Entity Models

### Core Entities
- **MonitoredService** - Web services/resources being monitored with configuration settings
- **CheckSchedule** - Cron-based scheduling configuration for monitoring checks  
- **CheckResult** - Results and metrics from availability checks
- **ServiceMetrics** - Aggregated performance statistics for reporting

### Alert System
- **AlertRule** - Threshold-based alert configuration and conditions
- **Alert** - Triggered alert instances with resolution tracking
- **NotificationChannel** - Delivery method configuration (email, Telegram, webhooks)
- **NotificationDelivery** - Audit trail of notification attempts and status

### User Management
- **User** - System users with role-based access control

## Data Transfer Objects

### Request DTOs
- **CreateServiceRequest** - Service creation and configuration
- **CreateScheduleRequest** - Monitoring schedule setup
- **CreateAlertRuleRequest** - Alert rule configuration
- **CreateNotificationChannelRequest** - Notification channel setup
- **MetricsRequest** - Analytics and reporting queries

### Response DTOs  
- **ServiceResponse** - Service information for API responses
- **CheckResultResponse** - Check outcome data
- **AlertResponse** - Alert details and status
- **DashboardResponse** - Dashboard overview statistics
- **MetricsResponse** - Performance analytics with time series data

All models use Java records for immutability and conciseness. Timestamps use LocalDateTime for consistency.