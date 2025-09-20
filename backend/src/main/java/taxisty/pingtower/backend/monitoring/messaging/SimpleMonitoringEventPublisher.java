package taxisty.pingtower.backend.monitoring.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import taxisty.pingtower.backend.storage.model.CheckResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of monitoring event publisher.
 * Uses RabbitMQ when available, falls back to console logging otherwise.
 */
@Component
@Primary
public class SimpleMonitoringEventPublisher implements MonitoringEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleMonitoringEventPublisher.class);
    
    // RabbitMQ exchange and routing keys
    private static final String MONITORING_EXCHANGE = "pingtower.monitoring";
    private static final String CHECK_RESULT_ROUTING_KEY = "check.result";
    private static final String ALERT_ROUTING_KEY = "alert.triggered";
    private static final String METRICS_ROUTING_KEY = "metrics.calculate";
    
    private final RabbitTemplate rabbitTemplate;
    
    public SimpleMonitoringEventPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        if (rabbitTemplate != null) {
            logger.info("RabbitMQ messaging enabled");
        } else {
            logger.info("RabbitMQ not available, using console logging fallback");
        }
    }
    
    @Override
    public void publishCheckResult(CheckResult checkResult) {
        if (rabbitTemplate != null) {
            try {
                // Create event payload
                Map<String, Object> event = new HashMap<>();
                event.put("type", "CHECK_RESULT");
                event.put("serviceId", checkResult.serviceId());
                event.put("checkTime", checkResult.checkTime().toString());
                event.put("isSuccessful", checkResult.isSuccessful());
                event.put("responseCode", checkResult.responseCode());
                event.put("responseTimeMs", checkResult.responseTimeMs());
                event.put("errorMessage", checkResult.errorMessage());
                event.put("timestamp", LocalDateTime.now().toString());
                
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(MONITORING_EXCHANGE, CHECK_RESULT_ROUTING_KEY, event);
                
                logger.debug("Published check result for service: {}", checkResult.serviceId());
            } catch (Exception e) {
                logger.error("Failed to publish check result for service: {}", checkResult.serviceId(), e);
            }
        } else {
            // Fallback to console logging
            logger.debug("Check result for service {}: {} ({}ms)", 
                checkResult.serviceId(), 
                checkResult.isSuccessful() ? "SUCCESS" : "FAILED", 
                checkResult.responseTimeMs());
        }
    }
    
    @Override
    public void publishAlert(Long serviceId, String alertMessage, AlertSeverity severity) {
        if (rabbitTemplate != null) {
            try {
                // Create alert event payload
                Map<String, Object> event = new HashMap<>();
                event.put("type", "ALERT");
                event.put("serviceId", serviceId);
                event.put("message", alertMessage);
                event.put("severity", severity.toString());
                event.put("timestamp", LocalDateTime.now().toString());
                
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(MONITORING_EXCHANGE, ALERT_ROUTING_KEY, event);
                
                logger.warn("Published alert for service {}: {} [{}]", serviceId, alertMessage, severity);
            } catch (Exception e) {
                logger.error("Failed to publish alert for service: {}", serviceId, e);
            }
        } else {
            // Fallback to console logging
            logger.warn("Alert for service {}: {} [{}]", serviceId, alertMessage, severity);
        }
    }
    
    @Override
    public void publishMetricsCalculation(Long serviceId) {
        if (rabbitTemplate != null) {
            try {
                // Create metrics calculation event payload
                Map<String, Object> event = new HashMap<>();
                event.put("type", "METRICS_CALCULATION");
                event.put("serviceId", serviceId);
                event.put("timestamp", LocalDateTime.now().toString());
                event.put("requestedBy", "SCHEDULER");
                
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(MONITORING_EXCHANGE, METRICS_ROUTING_KEY, event);
                
                logger.debug("Published metrics calculation request for service: {}", serviceId);
            } catch (Exception e) {
                logger.error("Failed to publish metrics calculation request for service: {}", serviceId, e);
            }
        } else {
            // Fallback to console logging
            logger.debug("Metrics calculation request for service: {}", serviceId);
        }
    }
    
    /**
     * Publishes a service recovery event when a service comes back online
     */
    public void publishServiceRecovery(Long serviceId, String serviceName) {
        if (rabbitTemplate != null) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("type", "SERVICE_RECOVERY");
                event.put("serviceId", serviceId);
                event.put("serviceName", serviceName);
                event.put("timestamp", LocalDateTime.now().toString());
                
                rabbitTemplate.convertAndSend(MONITORING_EXCHANGE, "service.recovery", event);
                
                logger.info("Published service recovery event for service: {}", serviceName);
            } catch (Exception e) {
                logger.error("Failed to publish service recovery event for service: {}", serviceId, e);
            }
        } else {
            logger.info("Service recovery event for service: {}", serviceName);
        }
    }
    
    /**
     * Publishes a batch of check results for bulk processing
     */
    public void publishBatchCheckResults(java.util.List<CheckResult> checkResults) {
        if (rabbitTemplate != null) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("type", "BATCH_CHECK_RESULTS");
                event.put("count", checkResults.size());
                event.put("timestamp", LocalDateTime.now().toString());
                event.put("results", checkResults.stream()
                    .map(result -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("serviceId", result.serviceId());
                        resultMap.put("isSuccessful", result.isSuccessful());
                        resultMap.put("responseCode", result.responseCode());
                        resultMap.put("responseTimeMs", result.responseTimeMs());
                        resultMap.put("checkTime", result.checkTime().toString());
                        return resultMap;
                    })
                    .toList());
                
                rabbitTemplate.convertAndSend(MONITORING_EXCHANGE, "check.batch", event);
                
                logger.debug("Published batch check results: {} items", checkResults.size());
            } catch (Exception e) {
                logger.error("Failed to publish batch check results", e);
            }
        } else {
            logger.debug("Batch check results: {} items", checkResults.size());
        }
    }
}