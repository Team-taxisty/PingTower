package taxisty.pingtower.backend.monitoring.messaging;

import taxisty.pingtower.backend.storage.model.CheckResult;

/**
 * Interface for consuming monitoring events.
 * Will be implemented with RabbitMQ when dependencies are available.
 */
public interface MonitoringEventConsumer {
    
    /**
     * Process incoming check result events
     */
    void handleCheckResult(CheckResult checkResult);
    
    /**
     * Process alert events for notifications
     */
    void handleAlert(MonitoringAlert alert);
    
    /**
     * Process metrics calculation requests
     */
    void handleMetricsCalculation(Long serviceId);
    
    /**
     * Monitoring alert data class
     */
    record MonitoringAlert(
        Long serviceId,
        String message,
        MonitoringEventPublisher.AlertSeverity severity,
        java.time.LocalDateTime timestamp
    ) {}
}