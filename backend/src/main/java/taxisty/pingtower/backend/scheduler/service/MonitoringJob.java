package taxisty.pingtower.backend.scheduler.service;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import taxisty.pingtower.backend.scheduler.task.ScheduledTask;
import taxisty.pingtower.backend.scheduler.task.TaskExecutionContext;
import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quartz job implementation that executes monitoring tasks for services.
 * Orchestrates the execution of different monitoring task types.
 */
@Component
public class MonitoringJob implements Job {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringJob.class);
    
    @Autowired
    private MonitoringExecutorService executorService;
    
    @Autowired
    private List<ScheduledTask> scheduledTasks;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        
        Long serviceId = dataMap.getLong("serviceId");
        String serviceName = dataMap.getString("serviceName");
        String serviceUrl = dataMap.getString("serviceUrl");
        
        logger.debug("Executing monitoring job for service: {} (ID: {})", serviceName, serviceId);
        
        try {
            // Create execution context
            TaskExecutionContext executionContext = TaskExecutionContext.createInitial(
                    serviceId,
                    "HTTP_CHECK", // Default task type
                    LocalDateTime.now()
            );
            
            // Execute monitoring
            executorService.executeMonitoring(serviceId, executionContext);
            
        } catch (Exception e) {
            logger.error("Failed to execute monitoring for service: {} (ID: {})", serviceName, serviceId, e);
            throw new JobExecutionException("Monitoring execution failed", e);
        }
    }
}