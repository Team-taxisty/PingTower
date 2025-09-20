package taxisty.pingtower.backend.monitoring.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for messaging between monitoring components.
 * Configures RabbitMQ when available, otherwise provides no-op implementations.
 */
@Configuration
public class MessagingConfig {
    
    // Queue names
    public static final String MONITORING_QUEUE = "monitoring.check.queue";
    public static final String NOTIFICATION_QUEUE = "monitoring.notification.queue"; 
    public static final String ANALYTICS_QUEUE = "monitoring.analytics.queue";
    
    // Exchange names
    public static final String MONITORING_EXCHANGE = "monitoring.exchange";
    
    // Routing keys
    public static final String CHECK_RESULT_ROUTING_KEY = "monitoring.check.result";
    public static final String ALERT_ROUTING_KEY = "monitoring.alert";
    public static final String METRICS_ROUTING_KEY = "monitoring.metrics";
    
    /**
     * RabbitMQ configuration - only active when RabbitMQ is available
     */
    @Configuration
    @ConditionalOnClass(ConnectionFactory.class)
    @ConditionalOnProperty(name = "spring.rabbitmq.host", matchIfMissing = false)
    public static class RabbitMQConfig {
        
        @Bean
        public DirectExchange monitoringExchange() {
            return new DirectExchange(MONITORING_EXCHANGE);
        }
        
        @Bean
        public Queue monitoringQueue() {
            return QueueBuilder.durable(MONITORING_QUEUE).build();
        }
        
        @Bean
        public Queue notificationQueue() {
            return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
        }
        
        @Bean
        public Queue analyticsQueue() {
            return QueueBuilder.durable(ANALYTICS_QUEUE).build();
        }
        
        @Bean
        public Binding monitoringBinding() {
            return BindingBuilder.bind(monitoringQueue())
                .to(monitoringExchange())
                .with(CHECK_RESULT_ROUTING_KEY);
        }
        
        @Bean
        public Binding alertBinding() {
            return BindingBuilder.bind(notificationQueue())
                .to(monitoringExchange())
                .with(ALERT_ROUTING_KEY);
        }
        
        @Bean
        public Binding metricsBinding() {
            return BindingBuilder.bind(analyticsQueue())
                .to(monitoringExchange())
                .with(METRICS_ROUTING_KEY);
        }
    }
}