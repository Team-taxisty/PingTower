package taxisty.pingtower.backend.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.scheduler.task.ScheduledTask;
import taxisty.pingtower.backend.scheduler.task.TaskExecutionContext;
import taxisty.pingtower.backend.scheduler.task.TaskType;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for executing monitoring tasks and handling results.
 * Coordinates between different task implementations and result storage.
 */
@Service
public class MonitoringExecutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringExecutorService.class);
    
    private final Map<String, ScheduledTask> taskRegistry;
    private final MonitoringDataService dataService;
    
    @Autowired
    public MonitoringExecutorService(List<ScheduledTask> scheduledTasks, 
                                   MonitoringDataService dataService) {
        this.taskRegistry = scheduledTasks.stream()
                .collect(Collectors.toMap(
                        ScheduledTask::getTaskType,
                        Function.identity()
                ));
        this.dataService = dataService;
        
        logger.info("Initialized MonitoringExecutorService with {} task types: {}", 
                   taskRegistry.size(), taskRegistry.keySet());
    }
    
    /**
     * Executes monitoring for a service with the given execution context.
     * 
     * @param serviceId The service ID to monitor
     * @param context The execution context
     */
    public void executeMonitoring(Long serviceId, TaskExecutionContext context) {
        try {
            MonitoredService service = dataService.getMonitoredService(serviceId);
            
            if (service == null) {
                logger.error("Service not found: {}", serviceId);
                return;
            }
            
            if (!service.isActive()) {
                logger.debug("Skipping monitoring for inactive service: {}", serviceId);
                return;
            }
            
            // Determine appropriate task type
            String taskType = determineTaskType(service);
            ScheduledTask task = taskRegistry.get(taskType);
            
            if (task == null) {
                logger.error("No task implementation found for type: {}", taskType);
                createErrorResult(service, "No task implementation available", context);
                return;
            }
            
            if (!task.canExecute(service)) {
                logger.warn("Task {} cannot execute for service: {}", taskType, serviceId);
                createErrorResult(service, "Task execution not possible", context);
                return;
            }
            
            // Execute the monitoring task
            logger.debug("Executing {} for service: {} ({})", taskType, service.name(), serviceId);
            CheckResult result = task.execute(service);
            
            // Store the result
            dataService.saveCheckResult(result);
            
            // Handle alerts if needed
            if (!result.isSuccessful()) {
                dataService.handleFailureAlert(service, result);
            }
            
            logger.debug("Monitoring completed for service: {} - Success: {}, Response time: {}ms", 
                        service.name(), result.isSuccessful(), result.responseTimeMs());
                        
        } catch (Exception e) {
            logger.error("Failed to execute monitoring for service: {}", serviceId, e);
            
            try {
                MonitoredService service = dataService.getMonitoredService(serviceId);
                if (service != null) {
                    createErrorResult(service, "Monitoring execution failed: " + e.getMessage(), context);
                }
            } catch (Exception ex) {
                logger.error("Failed to create error result for service: {}", serviceId, ex);
            }
        }
    }
    
    /**
     * Determines the appropriate task type based on service configuration.
     * 
     * @param service The monitored service
     * @return Task type string
     */
    private String determineTaskType(MonitoredService service) {
        // Simple logic to determine task type based on service configuration
        String url = service.url().toLowerCase();
        
        if (url.contains("/api/") || url.contains("/health") || url.contains("/status")) {
            return TaskType.API_CHECK.getCode();
        } else if (service.sslCertificateCheck()) {
            return TaskType.SSL_CHECK.getCode();
        } else {
            return TaskType.HTTP_CHECK.getCode();
        }
    }
    
    /**
     * Creates an error result when monitoring cannot be executed properly.
     */
    private void createErrorResult(MonitoredService service, String errorMessage, TaskExecutionContext context) {
        CheckResult errorResult = new CheckResult(
                null, // ID will be generated by storage
                service.id(),
                context.actualExecutionTime(),
                false,
                0,
                0L,
                null,
                errorMessage,
                false,
                null,
                "scheduler"
        );
        
        try {
            dataService.saveCheckResult(errorResult);
        } catch (Exception e) {
            logger.error("Failed to save error result for service: {}", service.id(), e);
        }
    }
}