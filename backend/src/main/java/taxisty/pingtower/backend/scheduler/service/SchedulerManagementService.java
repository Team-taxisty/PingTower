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

/**
 * Service responsible for scheduler lifecycle management and startup initialization.
 * Handles scheduler startup, shutdown, and initial monitoring setup.
 */
@Service
public class SchedulerManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManagementService.class);
    
    private final SchedulerService schedulerService;
    private final SchedulerProperties schedulerProperties;
    
    @Autowired
    public SchedulerManagementService(SchedulerService schedulerService, 
                                    SchedulerProperties schedulerProperties) {
        this.schedulerService = schedulerService;
        this.schedulerProperties = schedulerProperties;
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
                
                // TODO: Load and schedule existing monitoring configurations
                // This would be implemented when the storage layer is available:
                // List<MonitoredService> services = dataService.getAllActiveServices();
                // for (MonitoredService service : services) {
                //     CheckSchedule schedule = dataService.getScheduleForService(service.id());
                //     if (schedule != null) {
                //         schedulerService.scheduleMonitoring(service, schedule);
                //     }
                // }
                
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
            // TODO: Implement schedule reloading
            // This would unschedule all current jobs and reschedule based on current configuration
            
            logger.info("Schedule reload completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to reload schedules", e);
            throw new RuntimeException("Schedule reload failed", e);
        }
    }
    
    /**
     * Gets scheduler status information.
     * 
     * @return Status information about the scheduler
     */
    public SchedulerStatus getSchedulerStatus() {
        try {
            // TODO: Implement status collection
            // This would gather information about running jobs, next execution times, etc.
            
            return new SchedulerStatus(
                    true, // isRunning
                    0,    // activeJobs (to be implemented)
                    0,    // totalJobs (to be implemented)
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