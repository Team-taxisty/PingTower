package taxisty.pingtower.backend.scheduler.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.scheduler.config.SchedulerProperties;
import taxisty.pingtower.backend.monitoring.service.MonitoringService;
import taxisty.pingtower.backend.storage.model.MonitoredService;
import taxisty.pingtower.backend.storage.model.CheckSchedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for scheduler lifecycle management and startup initialization.
 * Handles scheduler startup, shutdown, and initial monitoring setup.
 */
@Service
public class SchedulerManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManagementService.class);
    
    private final SchedulerService schedulerService;
    private final SchedulerProperties schedulerProperties;
    private final MonitoringService monitoringService;
    
    @Autowired
    public SchedulerManagementService(SchedulerService schedulerService, 
                                    SchedulerProperties schedulerProperties,
                                    MonitoringService monitoringService) {
        this.schedulerService = schedulerService;
        this.schedulerProperties = schedulerProperties;
        this.monitoringService = monitoringService;
    }
    
    /**
     * Initializes the scheduler when the application is fully started.
     * This method is called after all beans are initialized and the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeScheduler() {
        logger.info("Initializing PingTower monitoring scheduler...");
        
        try {
            if (schedulerProperties.isAutoStart()) {
                // Here you would typically load existing monitoring configurations
                // and schedule them. This requires integration with the storage layer.
                
                logger.info("Scheduler auto-start is enabled. Loading existing monitoring schedules...");
                
                // Load and schedule existing monitoring configurations
                loadAndScheduleExistingConfigurations();
                
                logger.info("Scheduler initialization completed successfully");
            } else {
                logger.info("Scheduler auto-start is disabled");
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize scheduler", e);
            throw new RuntimeException("Scheduler initialization failed", e);
        }
    }
    
    /**
     * Performs scheduler startup tasks.
     */
    @PostConstruct
    public void startup() {
        logger.info("PingTower scheduler management service starting up...");
        logger.info("Scheduler configuration: threads={}, clustered={}, autoStart={}", 
                   schedulerProperties.getThreadCount(),
                   schedulerProperties.isClustered(),
                   schedulerProperties.isAutoStart());
    }
    
    /**
     * Performs cleanup when the application is shutting down.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down PingTower scheduler management service...");
        
        try {
            // Graceful shutdown of monitoring tasks
            // The Quartz scheduler will handle job completion based on configuration
            logger.info("Scheduler shutdown completed");
            
        } catch (Exception e) {
            logger.error("Error during scheduler shutdown", e);
        }
    }
    
    /**
     * Triggers a reload of all monitoring schedules.
     * Useful for configuration changes or administrative operations.
     */
    public void reloadAllSchedules() {
        logger.info("Reloading all monitoring schedules...");
        
        try {
            // Stop all current jobs
            schedulerService.stopAll();
            
            // Reload configurations from storage
            loadAndScheduleExistingConfigurations();
            
            logger.info("Schedule reload completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to reload schedules", e);
            throw new RuntimeException("Schedule reload failed", e);
        }
    }
    
    /**
     * Loads existing monitoring configurations and schedules them
     */
    private void loadAndScheduleExistingConfigurations() {
        try {
            logger.info("Loading active monitoring services...");
            
            // Get all active services from storage
            List<MonitoredService> activeServices = monitoringService.getAllActiveServices();
            logger.info("Found {} active monitoring services", activeServices.size());
            
            for (MonitoredService service : activeServices) {
                try {
                    // Create a default schedule if none exists
                    CheckSchedule schedule = createDefaultScheduleForService(service);
                    
                    // Schedule the monitoring job
                    schedulerService.scheduleMonitoring(service, schedule);
                    
                    logger.debug("Scheduled monitoring for service: {} ({})", 
                                service.getName(), service.getId());
                    
                } catch (Exception e) {
                    logger.error("Failed to schedule monitoring for service: {} ({})", 
                                service.getName(), service.getId(), e);
                }
            }
            
            logger.info("Completed loading and scheduling monitoring configurations");
            
        } catch (Exception e) {
            logger.error("Failed to load existing monitoring configurations", e);
            throw new RuntimeException("Failed to load monitoring configurations", e);
        }
    }
    
    /**
     * Creates a default schedule configuration for a service
     */
    private CheckSchedule createDefaultScheduleForService(MonitoredService service) {
        CheckSchedule schedule = new CheckSchedule();
        schedule.setServiceId(service.getId());
        schedule.setIntervalSeconds(300); // Default 5 minutes
        schedule.setIsEnabled(true);
        schedule.setTimezone("UTC");
        schedule.setNextRunTime(LocalDateTime.now().plusSeconds(30)); // Start in 30 seconds
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());
        
        return schedule;
    }
    
    /**
     * Gets scheduler status information.
     * 
     * @return Status information about the scheduler
     */
    public SchedulerStatus getSchedulerStatus() {
        try {
            // Get actual status from the scheduler service
            int activeJobs = schedulerService.getActiveJobCount();
            int totalJobs = schedulerService.getTotalJobCount();
            boolean isRunning = schedulerService.isRunning();
            
            return new SchedulerStatus(
                    isRunning,
                    activeJobs,
                    totalJobs,
                    schedulerProperties.getThreadCount(),
                    schedulerProperties.isClustered()
            );
            
        } catch (Exception e) {
            logger.error("Failed to get scheduler status", e);
            return new SchedulerStatus(false, 0, 0, 0, false);
        }
    }
    
    /**
     * Record representing scheduler status information.
     */
    public record SchedulerStatus(
            boolean isRunning,
            int activeJobs,
            int totalJobs,
            int threadCount,
            boolean isClustered
    ) {}
}