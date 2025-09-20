package taxisty.pingtower.backend.monitoring.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main configuration for monitoring package.
 * Enables scheduling for background tasks and configuration properties.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(MonitoringProperties.class)
public class MonitoringConfig {
}