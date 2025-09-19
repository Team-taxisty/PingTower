package taxisty.pingtower.backend.scheduler.task;

import taxisty.pingtower.backend.storage.model.CheckResult;
import taxisty.pingtower.backend.storage.model.MonitoredService;

/**
 * Interface for all scheduled monitoring tasks.
 * Implementations should handle specific monitoring operations like HTTP checks.
 */
public interface ScheduledTask {
    
    /**
     * Executes the monitoring task for a given service.
     * 
     * @param service The monitored service to check
     * @return CheckResult containing the outcome of the monitoring check
     */
    CheckResult execute(MonitoredService service);
    
    /**
     * Returns the task type identifier.
     * 
     * @return String representing the task type (e.g., "HTTP_CHECK", "API_CHECK")
     */
    String getTaskType();
    
    /**
     * Validates if the task can be executed for the given service.
     * 
     * @param service The monitored service
     * @return true if the task can execute, false otherwise
     */
    boolean canExecute(MonitoredService service);
}