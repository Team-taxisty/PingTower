package taxisty.pingtower.backend.scheduler.service;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import taxisty.pingtower.backend.storage.model.CheckSchedule;
import taxisty.pingtower.backend.storage.model.MonitoredService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Core scheduler service that manages monitoring task scheduling using Quartz.
 * Handles flexible interval configuration from seconds to hours based on service criticality.
 */
@Service
public class SchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private static final String JOB_GROUP = "monitoring-jobs";
    private static final String TRIGGER_GROUP = "monitoring-triggers";
    
    private final Scheduler scheduler;
    
    @Autowired
    public SchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    /**
     * Schedules monitoring for a service based on its schedule configuration.
     * 
     * @param service The monitored service
     * @param schedule The schedule configuration
     * @throws SchedulerException If scheduling fails
     */
    public void scheduleMonitoring(MonitoredService service, CheckSchedule schedule) throws SchedulerException {
        if (!schedule.isEnabled() || !service.isActive()) {
            logger.info("Skipping scheduling for disabled service or schedule: serviceId={}", service.id());
            return;
        }
        
        JobKey jobKey = createJobKey(service.id());
        TriggerKey triggerKey = createTriggerKey(service.id());
        
        // Remove existing job if present
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            logger.info("Removed existing job for service: {}", service.id());
        }
        
        // Create job detail
        JobDetail jobDetail = JobBuilder.newJob(MonitoringJob.class)
                .withIdentity(jobKey)
                .usingJobData("serviceId", service.id())
                .usingJobData("serviceName", service.name())
                .usingJobData("serviceUrl", service.url())
                .build();
        
        // Create trigger based on schedule type
        Trigger trigger = createTrigger(schedule, triggerKey);
        
        // Schedule the job
        scheduler.scheduleJob(jobDetail, trigger);
        
        logger.info("Scheduled monitoring for service: {} with schedule: {}", 
                   service.name(), getScheduleDescription(schedule));
    }
    
    /**
     * Unschedules monitoring for a service.
     * 
     * @param serviceId The service ID
     * @throws SchedulerException If unscheduling fails
     */
    public void unscheduleMonitoring(Long serviceId) throws SchedulerException {
        JobKey jobKey = createJobKey(serviceId);
        
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            logger.info("Unscheduled monitoring for service: {}", serviceId);
        }
    }
    
    /**
     * Updates the schedule for an existing monitoring job.
     * 
     * @param service The monitored service
     * @param newSchedule The updated schedule configuration
     * @throws SchedulerException If rescheduling fails
     */
    public void rescheduleMonitoring(MonitoredService service, CheckSchedule newSchedule) throws SchedulerException {
        unscheduleMonitoring(service.id());
        scheduleMonitoring(service, newSchedule);
    }
    
    /**
     * Triggers an immediate execution of monitoring for a service.
     * 
     * @param serviceId The service ID
     * @throws SchedulerException If triggering fails
     */
    public void triggerImmediateCheck(Long serviceId) throws SchedulerException {
        JobKey jobKey = createJobKey(serviceId);
        
        if (scheduler.checkExists(jobKey)) {
            scheduler.triggerJob(jobKey);
            logger.info("Triggered immediate check for service: {}", serviceId);
        } else {
            logger.warn("Cannot trigger immediate check - no scheduled job found for service: {}", serviceId);
        }
    }
    
    /**
     * Checks if monitoring is scheduled for a service.
     * 
     * @param serviceId The service ID
     * @return true if monitoring is scheduled, false otherwise
     * @throws SchedulerException If check fails
     */
    public boolean isMonitoringScheduled(Long serviceId) throws SchedulerException {
        JobKey jobKey = createJobKey(serviceId);
        return scheduler.checkExists(jobKey);
    }
    
    /**
     * Gets the next scheduled execution time for a service.
     * 
     * @param serviceId The service ID
     * @return Next execution time or null if not scheduled
     * @throws SchedulerException If retrieval fails
     */
    public LocalDateTime getNextExecutionTime(Long serviceId) throws SchedulerException {
        TriggerKey triggerKey = createTriggerKey(serviceId);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        
        if (trigger != null) {
            Date nextFireTime = trigger.getNextFireTime();
            if (nextFireTime != null) {
                return LocalDateTime.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault());
            }
        }
        
        return null;
    }
    
    private Trigger createTrigger(CheckSchedule schedule, TriggerKey triggerKey) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey);
        
        // Use cron expression if available, otherwise use interval
        if (schedule.cronExpression() != null && !schedule.cronExpression().isBlank()) {
            return triggerBuilder
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule.cronExpression())
                            .inTimeZone(java.util.TimeZone.getTimeZone(schedule.timezone())))
                    .build();
        } else {
            return triggerBuilder
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(schedule.intervalSeconds())
                            .repeatForever())
                    .startNow()
                    .build();
        }
    }
    
    private JobKey createJobKey(Long serviceId) {
        return JobKey.jobKey("monitoring-service-" + serviceId, JOB_GROUP);
    }
    
    private TriggerKey createTriggerKey(Long serviceId) {
        return TriggerKey.triggerKey("monitoring-trigger-" + serviceId, TRIGGER_GROUP);
    }
    
    /**
     * Stops all running monitoring jobs.
     * 
     * @throws SchedulerException If stopping jobs fails
     */
    public void stopAll() throws SchedulerException {
        if (scheduler.isStarted()) {
            scheduler.pauseAll();
            logger.info("Paused all monitoring jobs");
        }
    }
    
    /**
     * Gets the number of currently active jobs.
     * 
     * @return Number of active jobs
     * @throws SchedulerException If retrieval fails
     */
    public int getActiveJobCount() throws SchedulerException {
        return scheduler.getCurrentlyExecutingJobs().size();
    }
    
    /**
     * Gets the total number of scheduled jobs.
     * 
     * @return Total number of jobs
     * @throws SchedulerException If retrieval fails
     */
    public int getTotalJobCount() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP)).size();
    }
    
    /**
     * Checks if the scheduler is running.
     * 
     * @return true if scheduler is running, false otherwise
     * @throws SchedulerException If check fails
     */
    public boolean isRunning() throws SchedulerException {
        return scheduler.isStarted() && !scheduler.isInStandbyMode();
    }
    
    private String getScheduleDescription(CheckSchedule schedule) {
        if (schedule.cronExpression() != null && !schedule.cronExpression().isBlank()) {
            return "cron: " + schedule.cronExpression();
        } else {
            return "interval: " + schedule.intervalSeconds() + "s";
        }
    }
    
    // Adapter methods for controller compatibility
    
    /**
     * Schedule monitoring for a service using its internal configuration
     */
    public void scheduleService(MonitoredService service) throws SchedulerException {
        // Create a default schedule based on the service's check interval
        CheckSchedule defaultSchedule = new CheckSchedule(
                null, // id
                service.getId(), // serviceId
                null, // cronExpression
                service.getCheckIntervalMinutes() * 60, // intervalSeconds
                true, // isEnabled
                "UTC", // timezone
                null, // nextRunTime (will be calculated)
                LocalDateTime.now(), // createdAt
                LocalDateTime.now() // updatedAt
        );
        scheduleMonitoring(service, defaultSchedule);
    }
    
    /**
     * Reschedule monitoring for a service using its internal configuration
     */
    public void rescheduleService(MonitoredService service) throws SchedulerException {
        // Create a default schedule based on the service's check interval
        CheckSchedule newSchedule = new CheckSchedule(
                null, // id
                service.getId(), // serviceId
                null, // cronExpression
                service.getCheckIntervalMinutes() * 60, // intervalSeconds
                true, // isEnabled
                "UTC", // timezone
                null, // nextRunTime (will be calculated)
                LocalDateTime.now(), // createdAt
                LocalDateTime.now() // updatedAt
        );
        rescheduleMonitoring(service, newSchedule);
    }
    
    /**
     * Unschedule monitoring for a service by ID
     */
    public void unscheduleService(Long serviceId) throws SchedulerException {
        unscheduleMonitoring(serviceId);
    }
}