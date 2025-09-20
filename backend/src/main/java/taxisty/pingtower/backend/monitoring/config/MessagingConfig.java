package taxisty.pingtower.backend.monitoring.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for messaging between monitoring components.
 * This will be expanded with RabbitMQ configuration when dependencies are available.
 */
@Configuration
public class MessagingConfig {
    
    // Queue names for future RabbitMQ implementation
    public static final String MONITORING_QUEUE = "monitoring.check.queue";
    public static final String NOTIFICATION_QUEUE = "monitoring.notification.queue"; 
    public static final String ANALYTICS_QUEUE = "monitoring.analytics.queue";
    
    // Exchange names
    public static final String MONITORING_EXCHANGE = "monitoring.exchange";
    
    // Routing keys
    public static final String CHECK_RESULT_ROUTING_KEY = "monitoring.check.result";
    public static final String ALERT_ROUTING_KEY = "monitoring.alert";
    public static final String METRICS_ROUTING_KEY = "monitoring.metrics";
}