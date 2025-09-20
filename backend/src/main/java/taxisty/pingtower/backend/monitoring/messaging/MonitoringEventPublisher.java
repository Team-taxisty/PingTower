package taxisty.pingtower.backend.monitoring.messaging;

import taxisty.pingtower.backend.storage.model.CheckResult;

/**
 * Interface for publishing monitoring events.
 * Will be implemented with RabbitMQ when dependencies are available.
 */
public interface MonitoringEventPublisher {
    
    /**
     * Publish a check result event for processing
     */
    void publishCheckResult(CheckResult checkResult);
    
    /**
     * Publish an alert event for notification processing  
     */
    void publishAlert(Long serviceId, String alertMessage, AlertSeverity severity);
    
    /**
     * Publish a metrics calculation request
     */
    void publishMetricsCalculation(Long serviceId);
    
    /**
     * Alert severity levels
     */
    enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}