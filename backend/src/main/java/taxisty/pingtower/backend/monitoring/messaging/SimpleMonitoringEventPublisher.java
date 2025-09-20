package taxisty.pingtower.backend.monitoring.messaging;

import org.springframework.stereotype.Component;
import taxisty.pingtower.backend.storage.model.CheckResult;

/**
 * Simple implementation of monitoring event publisher.
 * This is a placeholder that will be replaced with RabbitMQ implementation.
 */
@Component
public class SimpleMonitoringEventPublisher implements MonitoringEventPublisher {
    
    @Override
    public void publishCheckResult(CheckResult checkResult) {
        // TODO: Implement with RabbitMQ
        // For now, just log the event
        System.out.println("Publishing check result for service: " + checkResult.serviceId());
    }
    
    @Override
    public void publishAlert(Long serviceId, String alertMessage, AlertSeverity severity) {
        // TODO: Implement with RabbitMQ
        // For now, just log the alert
        System.out.println("Publishing alert for service " + serviceId + ": " + alertMessage + " [" + severity + "]");
    }
    
    @Override
    public void publishMetricsCalculation(Long serviceId) {
        // TODO: Implement with RabbitMQ
        // For now, just log the metrics request
        System.out.println("Publishing metrics calculation request for service: " + serviceId);
    }
}